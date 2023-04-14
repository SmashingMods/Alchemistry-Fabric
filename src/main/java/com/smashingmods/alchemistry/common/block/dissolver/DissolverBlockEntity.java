package com.smashingmods.alchemistry.common.block.dissolver;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractInventoryBlockEntity;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DissolverBlockEntity extends AbstractInventoryBlockEntity {

    public static final int INVENTORY_SIZE = 11;

    private DissolverRecipe currentRecipe;
    protected final PropertyDelegate propertyDelegate;
    private final int maxProgress;
    private final DefaultedList<ItemStack> internalBuffer;

    public DissolverBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.DISSOLVER_BLOCK_ENTITY, pos, state, Config.Common.dissolverEnergyCapacity.get());
        this.internalBuffer = DefaultedList.ofSize(64);
        this.maxProgress = Config.Common.dissolverTicksPerOperation.get();
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
        return slot != 0;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        if (isRecipeLocked() && currentRecipe != null) {
            // there is only one input slot in recipe no matter what
            boolean validInsert = currentRecipe.getIngredients().get(0).test(stack);
            return validInsert && slot == 0;
        }

        return slot == 0;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new DissolverScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }

    @Override
    public void tick() {
        if (world != null && !world.isClient()) {
            if (!isProcessingPaused()) {
                if (!isRecipeLocked()) {
                    updateRecipe();
                }
                if (canProcessRecipe()) {
                    processRecipe();
                }
                processBuffer();
            }
        }
    }

    @Override
    public void updateRecipe() {
        if (world == null || world.isClient()) return;
        SimpleInventory inventory = new SimpleInventory(getItems().size());
        for (int i = 0; i < getItems().size(); i++) {
            inventory.setStack(i, getStack(i));
        }
        Optional<DissolverRecipe> match = world.getRecipeManager().getFirstMatch(DissolverRecipe.Type.INSTANCE, inventory, world);
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
            ItemStack input = getStackInSlot(0).copy();
            SimpleInventory inputInventory = new SimpleInventory(1);
            inputInventory.addStack(input);
            return getEnergyStorage().getAmount() >= Config.Common.dissolverEnergyPerTick.get()
                    && currentRecipe.matches(inputInventory, world)
                    && currentRecipe.getInput().getMatchingStacks().length > 0
                    && (input.getCount() >= currentRecipe.getInput().getMatchingStacks()[0].copy().getCount())
                    && internalBuffer.isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public void processRecipe() {
        if (getProgress() < maxProgress) {
            incrementProgress();
        } else {
            setProgress(0);
            decrementSlot(0, currentRecipe.getInput().getMatchingStacks()[0].copy().getCount());
            internalBuffer.addAll(currentRecipe.getProbabilityOutput().calculateOutput());
        }
        extractEnergy(Config.Common.dissolverEnergyPerTick.get());
        markDirty();
    }

    private void processBuffer() {
        for (int i = 0; i < internalBuffer.size(); i++) {
            ItemStack bufferStack = internalBuffer.get(i).copy();
            for (int j = 1; j < getItems().size(); j++) {
                ItemStack slotStack = getStackInSlot(j).copy();
                if (slotStack.isEmpty() || (ItemStack.canCombine(bufferStack, slotStack) && bufferStack.getCount() + slotStack.getCount() <= slotStack.getMaxCount())) {
                    setOrIncrement(j, bufferStack);
                    internalBuffer.remove(i);
                    break;
                }
            }
        }
        markDirty();
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T pRecipe) {
        currentRecipe = (DissolverRecipe) pRecipe;
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        return currentRecipe;
    }

}
