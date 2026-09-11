package com.smashingmods.alchemistry.common.block.fission;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.PowerState;
import com.smashingmods.alchemistry.api.blockentity.ReactorType;
import com.smashingmods.alchemistry.common.recipe.fission.FissionRecipe;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
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
import org.jetbrains.annotations.Nullable;

public class FissionControllerBlockEntity extends AbstractReactorBlockEntity {

    public static final int INVENTORY_SIZE = 3;

    private FissionRecipe currentRecipe;
    protected final ContainerData propertyDelegate;
    private final int maxProgress;

    public FissionControllerBlockEntity(BlockPos worldPosition, BlockState state) {
        super(NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.FISSION_CONTROLLER_BLOCK_ENTITY, worldPosition, state, Config.Common.fissionEnergyCapacity.get());
        setReactorType(ReactorType.FISSION);
        this.maxProgress = Config.Common.fissionTicksPerOperation.get();
        this.propertyDelegate = new ContainerData() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> getProgress();
                    case 1 -> maxProgress;
                    case 2 -> (int) getEnergyStorage().getAmount();
                    case 3 -> (int) getEnergyStorage().getCapacity();
                    default -> 0;
                };
            }
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> setProgress(value);
                    case 2 -> insertEnergy(value);
                }
            }
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot > 0;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot == 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide() || !isValidMultiblock()) return;
        if (!isProcessingPaused()) {
            if (!isRecipeLocked()) {
                updateRecipe();
            }
            if (canProcessRecipe()) {
                setPowerState(PowerState.ON);
                processRecipe();
            } else {
                if (getEnergyStorage().getAmount() > Config.Common.fissionEnergyPerTick.get()) {
                    setPowerState(PowerState.STANDBY);
                } else {
                    setPowerState(PowerState.OFF);
                }
            }
        }
    }

    @Override
    public void updateRecipe() {
        if (level != null && !level.isClientSide()) {
            if (!getStackInSlot(0).isEmpty()) {
                com.smashingmods.alchemistry.api.recipe.MachineRecipes.all(level, FissionRecipe.Type.INSTANCE).stream()
                        .filter(recipe -> ItemStack.isSameItemSameComponents(recipe.getInput(), getStackInSlot(0)))
                        .findFirst()
                        .ifPresent(recipe -> {
                            if (currentRecipe == null || !currentRecipe.equals(recipe)) {
                                setProgress(0);
                                currentRecipe = recipe;
                            }
                        });
            } else {
                setProgress(0);
                currentRecipe = null;
            }
        }
    }

    @Override
    public boolean canProcessRecipe() {
        if (currentRecipe != null) {
            ItemStack input = getStackInSlot(0);
            ItemStack output1 = getStackInSlot(1);
            ItemStack output2 = getStackInSlot(2);
            return getEnergyStorage().getAmount() >= Config.Common.fissionEnergyPerTick.get()
                    && (ItemStack.isSameItemSameComponents(input, currentRecipe.getInput()) && input.getCount() >= currentRecipe.getInput().getCount())
                    && ((ItemStack.isSameItemSameComponents(output1, currentRecipe.getOutput1()) || output1.isEmpty()) && (currentRecipe.getOutput1().getCount() + output1.getCount()) <= currentRecipe.getOutput1().getMaxStackSize())
                    && ((ItemStack.isSameItemSameComponents(output2, currentRecipe.getOutput2()) || output2.isEmpty()) && (currentRecipe.getOutput2().getCount() + output2.getCount()) <= currentRecipe.getOutput2().getMaxStackSize());
        }
        return false;
    }

    @Override
    public void processRecipe() {
        if (getProgress() < maxProgress) {
            incrementProgress();
        } else {
            setProgress(0);
            decrementSlot(0, currentRecipe.getInput().getCount());
            setOrIncrement(1, currentRecipe.getOutput1().copy());
            setOrIncrement(2, currentRecipe.getOutput2().copy());
        }
        extractEnergy(Config.Common.fissionEnergyPerTick.get());
        setChanged();
    }

    @Override
    public <T extends Recipe<RecipeInput>> void setRecipe(@Nullable T recipe) {
        currentRecipe = (FissionRecipe) recipe;
    }

    @Override
    public Recipe<RecipeInput> getRecipe() {
        return currentRecipe;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new FissionControllerScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }
}
