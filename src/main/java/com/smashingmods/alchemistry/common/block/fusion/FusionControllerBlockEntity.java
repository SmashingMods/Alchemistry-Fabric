package com.smashingmods.alchemistry.common.block.fusion;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.PowerState;
import com.smashingmods.alchemistry.api.blockentity.ReactorType;
import com.smashingmods.alchemistry.common.recipe.fusion.FusionRecipe;
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

public class FusionControllerBlockEntity extends AbstractReactorBlockEntity {

    public static final int INVENTORY_SIZE = 3;

    private FusionRecipe currentRecipe;
    protected final ContainerData propertyDelegate;
    private final int maxProgress;

    public FusionControllerBlockEntity(BlockPos worldPosition, BlockState state) {
        super(NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.FUSION_CONTROLLER_BLOCK_ENTITY, worldPosition, state, Config.Common.fusionEnergyCapacity.get());
        setReactorType(ReactorType.FUSION);
        this.maxProgress = Config.Common.fusionTicksPerOperation.get();
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
        return slot == 2;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot < 2;
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
                if (getEnergyStorage().getAmount() > Config.Common.fusionEnergyPerTick.get()) {
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
                com.smashingmods.alchemistry.api.recipe.MachineRecipes.all(level, FusionRecipe.Type.INSTANCE).stream()
                        .filter(recipe -> {
                            ItemStack input1 = getStackInSlot(0);
                            ItemStack input2 = getStackInSlot(1);
                            return ItemStack.isSameItemSameComponents(recipe.getInput1(), input1) && ItemStack.isSameItemSameComponents(recipe.getInput2(), input2)
                                    || ItemStack.isSameItemSameComponents(recipe.getInput2(), input1) && ItemStack.isSameItemSameComponents(recipe.getInput1(), input2);
                        })
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
            ItemStack input1 = getStackInSlot(0);
            ItemStack input2 = getStackInSlot(1);
            ItemStack output = getStackInSlot(2);
            return getEnergyStorage().getAmount() >= Config.Common.fusionEnergyPerTick.get()
                    && (((ItemStack.isSameItemSameComponents(input1, currentRecipe.getInput1()) && input1.getCount() >= currentRecipe.getInput1().getCount())
                    && (ItemStack.isSameItemSameComponents(input2, currentRecipe.getInput2()) && input2.getCount() >= currentRecipe.getInput2().getCount()))
                    || ((ItemStack.isSameItemSameComponents(input1, currentRecipe.getInput2()) && input1.getCount() >= currentRecipe.getInput2().getCount())
                    && (ItemStack.isSameItemSameComponents(input2, currentRecipe.getInput1()) && input2.getCount() >= currentRecipe.getInput1().getCount())))
                    && ((ItemStack.isSameItemSameComponents(output, currentRecipe.getOutput()) || output.isEmpty()) && (currentRecipe.getOutput().getCount() + output.getCount()) <= currentRecipe.getOutput().getMaxStackSize());
        }
        return false;
    }

    @Override
    public void processRecipe() {
        if (getProgress() < maxProgress) {
            incrementProgress();
        } else {
            setProgress(0);
            decrementSlot(0, currentRecipe.getInput1().getCount());
            decrementSlot(1, currentRecipe.getInput2().getCount());
            setOrIncrement(2, currentRecipe.getOutput().copy());
        }
        extractEnergy(Config.Common.fusionEnergyPerTick.get());
        setChanged();
    }

    @Override
    public <T extends Recipe<RecipeInput>> void setRecipe(@Nullable T recipe) {
        currentRecipe = (FusionRecipe) recipe;
    }

    @Override
    public Recipe<RecipeInput> getRecipe() {
        return currentRecipe;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new FusionControllerScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }
}
