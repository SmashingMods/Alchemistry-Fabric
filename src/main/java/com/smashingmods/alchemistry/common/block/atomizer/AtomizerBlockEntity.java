package com.smashingmods.alchemistry.common.block.atomizer;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractFluidBlockEntity;
import com.smashingmods.alchemistry.common.recipe.atomizer.AtomizerRecipe;
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

public class AtomizerBlockEntity extends AbstractFluidBlockEntity {

    public static final int INVENTORY_SIZE = 1;

    private AtomizerRecipe currentRecipe;
    protected final PropertyDelegate propertyDelegate;
    private final int maxProgress;

    public AtomizerBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.ATOMIZER_BLOCK_ENTITY, pos, state, Config.Common.atomizerEnergyCapacity.get(), Config.Common.atomizerFluidCapacity.get());
        this.maxProgress = Config.Common.atomizerTicksPerOperation.get();
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
        return true;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public void updateRecipe() {
        if (world == null || world.isClient() || getFluidStorage().getAmount() == 0) return;
        world.getRecipeManager().getAllMatches(AtomizerRecipe.Type.INSTANCE, new SimpleInventory(1), world).stream()
                .filter(recipe -> recipe.getFluidInput().equals(getFluidStorage().getResource()))
                .findFirst()
                .ifPresentOrElse(recipe -> {
                    if (currentRecipe == null || !currentRecipe.equals(recipe)) {
                        setProgress(0);
                        currentRecipe = recipe;
                    }
                }, () -> currentRecipe = null);
    }

    @Override
    public boolean canProcessRecipe() {
        if (currentRecipe != null) {
            return getEnergyStorage().getAmount() >= Config.Common.atomizerEnergyPerTick.get()
                    && getFluidStorage().getAmount() >= currentRecipe.getFluidAmount()
                    && ((ItemStack.canCombine(getStackInSlot(0), currentRecipe.getOutput())) || getStackInSlot(0).isEmpty())
                    && (getStackInSlot(0).getCount() + currentRecipe.getOutput().getCount()) <= currentRecipe.getOutput().getMaxCount();
        }
        return false;
    }

    @Override
    public void processRecipe() {
        if (getProgress() < maxProgress) {
            incrementProgress();
        } else {
            setProgress(0);
            setOrIncrement(0, currentRecipe.getOutput().copy());
            extractFluid(currentRecipe.getFluidInput(), currentRecipe.getFluidAmount());
        }
        extractEnergy(Config.Common.atomizerEnergyPerTick.get());
        markDirty();
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T recipe) {
        currentRecipe = (AtomizerRecipe) recipe;
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        return currentRecipe;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new AtomizerScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }
}
