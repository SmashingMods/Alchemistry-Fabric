package com.smashingmods.alchemistry.common.block.liquifier;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractFluidBlockEntity;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LiquifierBlockEntity extends AbstractFluidBlockEntity {

    public static final int INVENTORY_SIZE = 1;

    private LiquifierRecipe currentRecipe;
    protected final ContainerData propertyDelegate;
    private final int maxProgress;

    public LiquifierBlockEntity(BlockPos worldPosition, BlockState state) {
        super(NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.LIQUIFIER_BLOCK_ENTITY, worldPosition, state, Config.Common.liquifierEnergyCapacity.get(), 81L * Config.Common.liquifierFluidCapacity.get());
        this.maxProgress = Config.Common.liquifierTicksPerOperation.get();
        this.propertyDelegate = new ContainerData() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> getProgress();
                    case 1 -> maxProgress;
                    case 2 -> (int) getEnergyStorage().getAmount();
                    case 3 -> (int) getEnergyStorage().getCapacity();
                    case 4 -> (int) getFluidStorage().getAmount();
                    case 5 -> (int) getFluidStorage().getCapacity();
                    case 6 -> BuiltInRegistries.FLUID.getId(getFluidStorage().getResource().getFluid());
                    default -> 0;
                };
            }
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> setProgress(value);
                    case 2 -> insertEnergy(value);
                    case 4 -> getFluidStorage().amount = value;
                    case 6 -> getFluidStorage().variant = FluidVariant.of(BuiltInRegistries.FLUID.byId(value));
                }
            }
            public int getCount() {
                return 7;
            }
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return true;
    }

    @Override
    public void updateRecipe() {
        if (level == null || level.isClientSide()) return;
        SimpleContainer inventory = new SimpleContainer(getItems().size());
        for (int i = 0; i < getItems().size(); i++) {
            inventory.setItem(i, getItem(i));
        }
        Optional<LiquifierRecipe> match = com.smashingmods.alchemistry.api.recipe.MachineRecipes.all(level, LiquifierRecipe.Type.INSTANCE).stream().filter(recipe -> recipe.getInput().test(getItem(0))).findFirst();
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
                    && currentRecipe.getFluidAmount() * 81L <= getFluidStorage().getCapacity() - getFluidStorage().getAmount()
                    && (currentRecipe.getInput().test(getStackInSlot(0)))
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
            insertFluid(currentRecipe.getFluidOutput(), currentRecipe.getFluidAmount() * 81);
        }
        extractEnergy(Config.Common.liquifierEnergyPerTick.get());
        setChanged();
    }

    @Override
    public <T extends Recipe<RecipeInput>> void setRecipe(@Nullable T pRecipe) {
        currentRecipe = (LiquifierRecipe) pRecipe;
    }

    @Override
    public Recipe<RecipeInput> getRecipe() {
        return currentRecipe;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new LiquifierScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }
}
