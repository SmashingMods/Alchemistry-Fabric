package com.technovision.alchemistry.common.block.dissolver;

import com.technovision.alchemistry.api.blockentity.AbstractInventoryBlockEntity;
import com.technovision.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class DissolverBlockEntity extends AbstractInventoryBlockEntity {

    public static final int INVENTORY_SIZE = 11;

    public DissolverBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.DISSOLVER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new DissolverScreenHandler(syncId, inv, this, this);
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
                //processBuffer();
            }
        }
    }

    @Override
    public void updateRecipe() {
        // TODO: Implement
    }

    @Override
    public boolean canProcessRecipe() {
        // TODO: Implement
        return false;
    }

    @Override
    public void processRecipe() {
        // TODO: Implement
    }

    @Override
    public <T extends Recipe<Inventory>> void setRecipe(@Nullable T pRecipe) {
        // TODO: Implement
    }

    @Override
    public <T extends Recipe<Inventory>> Recipe<Inventory> getRecipe() {
        // TODO: Implement
        return null;
    }

    @Override
    public boolean isProcessingPaused() {
        // TODO: Implement
        return false;
    }

    @Override
    public void setPaused(boolean pPaused) {
        // TODO: Implement
    }
}
