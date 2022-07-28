package com.smashingmods.alchemistry.common.block.dissolver;

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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DissolverBlockEntity extends AbstractInventoryBlockEntity {

    public static final int INVENTORY_SIZE = 11;

    private DissolverRecipe currentRecipe;
    protected final PropertyDelegate propertyDelegate;
    private int maxProgress = 72;
    private final DefaultedList<ItemStack> internalBuffer = DefaultedList.ofSize(64);

    public DissolverBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.DISSOLVER_BLOCK_ENTITY, pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> getProgress();
                    case 1 -> DissolverBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> setProgress(value);
                    case 1 -> DissolverBlockEntity.this.maxProgress = value;
                }
            }
            public int size() {
                return 2;
            }
        };
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
            // TODO: Implement when energy is added (replace return statement below)
            /**
            return getEnergyHandler().getEnergyStored() >= Config.Common.dissolverEnergyPerTick.get()
                    && currentRecipe.matches(input)
                    && (input.getCount() >= currentRecipe.getInput().getMatchingStacks()[0].copy().getCount())
                    && internalBuffer.isEmpty();
             */
            SimpleInventory inputInventory = new SimpleInventory(1);
            inputInventory.addStack(input);
            return currentRecipe.matches(inputInventory, world)
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
        // TODO: Implement when energy is added
        //getEnergyHandler().extractEnergy(Config.Common.dissolverEnergyPerTick.get(), false);
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

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("dissolver.progress", getProgress());
        // TODO: Add energy here when implemented
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        setProgress(nbt.getInt("dissolver.progress"));
        // TODO: Add energy here when implemented
    }
}
