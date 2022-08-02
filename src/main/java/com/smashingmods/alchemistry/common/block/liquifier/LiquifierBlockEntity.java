package com.smashingmods.alchemistry.common.block.liquifier;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractFluidBlockEntity;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LiquifierBlockEntity extends AbstractFluidBlockEntity {

    public static final int INVENTORY_SIZE = 1;

    private LiquifierRecipe currentRecipe;
    protected final PropertyDelegate propertyDelegate;
    private final int maxProgress;

    public LiquifierBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.LIQUIFIER_BLOCK_ENTITY, pos, state, Config.Common.liquifierEnergyCapacity.get(), Config.Common.liquifierFluidCapacity.get());
        this.maxProgress = Config.Common.liquifierTicksPerOperation.get();
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> getProgress();
                    case 1 -> maxProgress;
                    case 2 -> (int) getEnergyStorage().getAmount();
                    case 3 -> (int) getEnergyStorage().getCapacity();
                    case 4 -> (int) getFluidStorage().getAmount();
                    case 5 -> (int) getFluidStorage().getCapacity();
                    case 6 -> Registry.FLUID.getRawId(getFluidStorage().getResource().getFluid());
                    default -> 0;
                };
            }
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> setProgress(value);
                    case 2 -> insertEnergy(value);
                    case 4 -> getFluidStorage().amount = value;
                    case 6 -> getFluidStorage().variant = FluidVariant.of(Registry.FLUID.get(value));
                }
            }
            public int size() {
                return 7;
            }
        };
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return true;
    }

    @Override
    public void updateRecipe() {
        if (world == null || world.isClient()) return;
        SimpleInventory inventory = new SimpleInventory(getItems().size());
        for (int i = 0; i < getItems().size(); i++) {
            inventory.setStack(i, getStack(i));
        }
        Optional<LiquifierRecipe> match = world.getRecipeManager().getFirstMatch(LiquifierRecipe.Type.INSTANCE, inventory, world);
        if (match.isPresent()) {
            if (currentRecipe == null || !currentRecipe.equals(match.get())) {
                setProgress(0);
                currentRecipe = match.get();
            }
        }
    }

    @Override
    public boolean canProcessRecipe() {
        if (currentRecipe != null) {
            return getEnergyStorage().getAmount() >= Config.Common.liquifierEnergyPerTick.get()
                    && (getFluidStorage().getResource().equals(currentRecipe.getFluidOutput()) || getFluidStorage().getAmount() == 0)
                    && getFluidStorage().getAmount() <= (getFluidStorage().getAmount() + currentRecipe.getFluidAmount())
                    && (ItemStack.canCombine(currentRecipe.getInput().getMatchingStacks()[0], getStackInSlot(0)))
                    && getStackInSlot(0).getCount() >= currentRecipe.getInputAmount();
        }
        return false;
    }

    @Override
    public void processRecipe() {
        if (getProgress() < maxProgress) {
            incrementProgress();
        } else {
            setProgress(0);
            decrementSlot(0, currentRecipe.getInputAmount());
            insertFluid(currentRecipe.getFluidOutput(), currentRecipe.getFluidAmount());
        }
        extractEnergy(Config.Common.liquifierEnergyPerTick.get());
        markDirty();
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T pRecipe) {
        currentRecipe = (LiquifierRecipe) pRecipe;
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        return currentRecipe;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new LiquifierScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }
}
