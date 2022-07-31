package com.smashingmods.alchemistry.common.block.compactor;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractInventoryBlockEntity;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CompactorBlockEntity extends AbstractInventoryBlockEntity {

    public static final int INVENTORY_SIZE = 3;

    private DissolverRecipe currentRecipe;
    protected final PropertyDelegate propertyDelegate;
    private final int maxProgress;
    private ItemStack target;

    public CompactorBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.COMPACTOR_BLOCK_ENTITY, pos, state, Config.Common.compactorEnergyCapacity.get());
        this.target = ItemStack.EMPTY;
        this.maxProgress = Config.Common.compactorTicksPerOperation.get();
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
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new CompactorScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }

    @Override
    public void tick() {
        // TODO: Implement
        if (world != null && !world.isClient()) {
            if (!isProcessingPaused()) {
                if (!isRecipeLocked()) {
                    updateRecipe();
                }
                if (canProcessRecipe()) {
                    processRecipe();
                }
            }
        }
    }

    @Override
    public void updateRecipe() {
        // TODO: Implement
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
        // TODO: Implement
        if (currentRecipe != null) {
            ItemStack input = getStackInSlot(0).copy();
            SimpleInventory inputInventory = new SimpleInventory(1);
            inputInventory.addStack(input);
            return getEnergyStorage().getAmount() >= Config.Common.compactorEnergyPerTick.get()
                    && currentRecipe.matches(inputInventory, world)
                    && currentRecipe.getInput().getMatchingStacks().length > 0
                    && (input.getCount() >= currentRecipe.getInput().getMatchingStacks()[0].copy().getCount());
        } else {
            return false;
        }
    }

    @Override
    public void processRecipe() {
        // TODO: Implement
        if (getProgress() < maxProgress) {
            incrementProgress();
        } else {
            setProgress(0);
            decrementSlot(0, currentRecipe.getInput().getMatchingStacks()[0].copy().getCount());
        }
        extractEnergy(100);
        markDirty();
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T pRecipe) {
        // TODO: Implement
        currentRecipe = (DissolverRecipe) pRecipe;
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        return currentRecipe;
    }

    public ItemStack getTarget() {
        return target;
    }

}
