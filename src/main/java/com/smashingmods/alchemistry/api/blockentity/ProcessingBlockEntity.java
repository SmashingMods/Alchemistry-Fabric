package com.smashingmods.alchemistry.api.blockentity;

import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.Recipe;

import org.jetbrains.annotations.Nullable;

public interface ProcessingBlockEntity {

    void tick();

    void updateRecipe();

    boolean canProcessRecipe();

    void processRecipe();

    <T extends Recipe<RecipeInput>> void setRecipe(@Nullable T pRecipe);

    Recipe<RecipeInput> getRecipe();

    int getProgress();

    void setProgress(int pProgress);

    void incrementProgress();

    boolean isRecipeLocked();

    void setRecipeLocked(boolean pRecipeLocked);

    boolean isProcessingPaused();

    void setPaused(boolean pPaused);

    void dropContents();
}
