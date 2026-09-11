package com.smashingmods.alchemistry.common.block.atomizer;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractFluidBlockEntity;
import com.smashingmods.alchemistry.common.recipe.atomizer.AtomizerRecipe;
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

public class AtomizerBlockEntity extends AbstractFluidBlockEntity {

    public static final int INVENTORY_SIZE = 1;

    private AtomizerRecipe currentRecipe;
    protected final ContainerData propertyDelegate;
    private final int maxProgress;

    public AtomizerBlockEntity(BlockPos worldPosition, BlockState state) {
        super(NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.ATOMIZER_BLOCK_ENTITY, worldPosition, state, Config.Common.atomizerEnergyCapacity.get(), 81L * Config.Common.atomizerFluidCapacity.get());
        this.maxProgress = Config.Common.atomizerTicksPerOperation.get();
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
        return true;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public void updateRecipe() {
        if (level == null || level.isClientSide() || getFluidStorage().getAmount() == 0) return;
        com.smashingmods.alchemistry.api.recipe.MachineRecipes.all(level, AtomizerRecipe.Type.INSTANCE).stream()
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
                    && getFluidStorage().getAmount() >= currentRecipe.getFluidAmount() * 81L
                    && getFluidStorage().getResource().equals(currentRecipe.getFluidInput())
                    && ((ItemStack.isSameItemSameComponents(getStackInSlot(0), currentRecipe.getOutput())) || getStackInSlot(0).isEmpty())
                    && (getStackInSlot(0).getCount() + currentRecipe.getOutput().getCount()) <= currentRecipe.getOutput().getMaxStackSize();
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
            extractFluid(currentRecipe.getFluidInput(), currentRecipe.getFluidAmount() * 81);
        }
        extractEnergy(Config.Common.atomizerEnergyPerTick.get());
        setChanged();
    }

    @Override
    public <T extends Recipe<RecipeInput>> void setRecipe(@Nullable T recipe) {
        currentRecipe = (AtomizerRecipe) recipe;
    }

    @Override
    public Recipe<RecipeInput> getRecipe() {
        return currentRecipe;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new AtomizerScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }
}
