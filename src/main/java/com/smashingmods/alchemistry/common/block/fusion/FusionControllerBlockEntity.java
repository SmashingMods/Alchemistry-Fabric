package com.smashingmods.alchemistry.common.block.fusion;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.PowerState;
import com.smashingmods.alchemistry.api.blockentity.ReactorType;
import com.smashingmods.alchemistry.common.recipe.fusion.FusionRecipe;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
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
import org.jetbrains.annotations.Nullable;

public class FusionControllerBlockEntity extends AbstractReactorBlockEntity {

    public static final int INVENTORY_SIZE = 3;

    private FusionRecipe currentRecipe;
    protected final PropertyDelegate propertyDelegate;
    private final int maxProgress;

    public FusionControllerBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.FUSION_CONTROLLER_BLOCK_ENTITY, pos, state, Config.Common.fusionEnergyCapacity.get());
        setReactorType(ReactorType.FUSION);
        this.maxProgress = Config.Common.fusionTicksPerOperation.get();
        this.propertyDelegate = new PropertyDelegate() {
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
            public int size() {
                return 4;
            }
        };
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return slot == 2;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return slot < 2;
    }

    @Override
    public void tick() {
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
        super.tick();
    }

    @Override
    public void updateRecipe() {
        if (world != null && !world.isClient()) {
            if (!getStackInSlot(0).isEmpty()) {
                world.getRecipeManager().getAllMatches(FusionRecipe.Type.INSTANCE, new SimpleInventory(1), world).stream()
                        .filter(recipe -> {
                            ItemStack input1 = getStackInSlot(0);
                            ItemStack input2 = getStackInSlot(1);
                            return ItemStack.canCombine(recipe.getInput1(), input1) && ItemStack.canCombine(recipe.getInput2(), input2)
                                    || ItemStack.canCombine(recipe.getInput2(), input1) && ItemStack.canCombine(recipe.getInput1(), input2);
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
                    && (((ItemStack.canCombine(input1, currentRecipe.getInput1()) && input1.getCount() >= currentRecipe.getInput1().getCount())
                    && (ItemStack.canCombine(input2, currentRecipe.getInput2()) && input2.getCount() >= currentRecipe.getInput2().getCount()))
                    || ((ItemStack.canCombine(input1, currentRecipe.getInput2()) && input1.getCount() >= currentRecipe.getInput2().getCount())
                    && (ItemStack.canCombine(input2, currentRecipe.getInput1()) && input2.getCount() >= currentRecipe.getInput1().getCount())))
                    && ((ItemStack.canCombine(output, currentRecipe.getOutput()) || output.isEmpty()) && (currentRecipe.getOutput().getCount() + output.getCount()) <= currentRecipe.getOutput().getMaxCount());
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
        markDirty();
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T recipe) {
        currentRecipe = (FusionRecipe) recipe;
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        return currentRecipe;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new FusionControllerScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }
}
