package com.smashingmods.alchemistry.common.block.fission;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.PowerState;
import com.smashingmods.alchemistry.api.blockentity.ReactorType;
import com.smashingmods.alchemistry.common.recipe.fission.FissionRecipe;
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

public class FissionControllerBlockEntity extends AbstractReactorBlockEntity {

    public static final int INVENTORY_SIZE = 3;

    private FissionRecipe currentRecipe;
    protected final PropertyDelegate propertyDelegate;
    private final int maxProgress;

    public FissionControllerBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.FISSION_CONTROLLER_BLOCK_ENTITY, pos, state, Config.Common.fissionEnergyCapacity.get());
        setReactorType(ReactorType.FISSION);
        this.maxProgress = Config.Common.fissionTicksPerOperation.get();
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
        return slot > 0;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return slot == 0;
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
                if (getEnergyStorage().getAmount() > Config.Common.fissionEnergyPerTick.get()) {
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
                world.getRecipeManager().getAllMatches(FissionRecipe.Type.INSTANCE, new SimpleInventory(1), world).stream()
                        .filter(recipe -> ItemStack.canCombine(recipe.getInput(), getStackInSlot(0)))
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
                    && (ItemStack.canCombine(input, currentRecipe.getInput()) && input.getCount() >= currentRecipe.getInput().getCount())
                    && ((ItemStack.canCombine(output1, currentRecipe.getOutput1()) || output1.isEmpty()) && (currentRecipe.getOutput1().getCount() + output1.getCount()) <= currentRecipe.getOutput1().getMaxCount())
                    && ((ItemStack.canCombine(output2, currentRecipe.getOutput2()) || output2.isEmpty()) && (currentRecipe.getOutput2().getCount() + output2.getCount()) <= currentRecipe.getOutput2().getMaxCount());
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
        markDirty();
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T recipe) {
        currentRecipe = (FissionRecipe) recipe;
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        return currentRecipe;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new FissionControllerScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }
}
