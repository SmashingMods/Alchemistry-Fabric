package com.technovision.alchemistry.api.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class AbstractProcessingBlockEntity extends BlockEntity implements ProcessingBlockEntity, NamedScreenHandlerFactory {

    public AbstractProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return null;
    }

    @Override
    public void tick() {

    }

    @Override
    public void updateRecipe() {

    }

    @Override
    public boolean canProcessRecipe() {
        return false;
    }

    @Override
    public void processRecipe() {

    }

    @Override
    public <T extends Recipe<Inventory>> void setRecipe(@Nullable T pRecipe) {

    }

    @Override
    public <T extends Recipe<Inventory>> Recipe<Inventory> getRecipe() {
        return null;
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public void setProgress(int pProgress) {

    }

    @Override
    public void incrementProgress() {

    }

    @Override
    public boolean isRecipeLocked() {
        return false;
    }

    @Override
    public void setRecipeLocked(boolean pRecipeLocked) {

    }

    @Override
    public boolean isProcessingPaused() {
        return false;
    }

    @Override
    public void setPaused(boolean pPaused) {

    }

    @Override
    public void dropContents() {

    }
}
