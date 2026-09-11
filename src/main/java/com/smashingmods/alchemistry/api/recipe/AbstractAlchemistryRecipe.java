package com.smashingmods.alchemistry.api.recipe;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.core.NonNullList;

public abstract class AbstractAlchemistryRecipe implements Recipe<RecipeInput> {
    private Identifier recipeId;
    protected AbstractAlchemistryRecipe(Identifier id) { recipeId = id; }
    public Identifier getId() { return recipeId; }
    public void setId(Identifier id) { recipeId = id; }
    @Override public boolean matches(RecipeInput input, Level level) { return !level.isClientSide(); }
    @Override public ItemStack assemble(RecipeInput input) { return getOutput().copy(); }
    public ItemStack getOutput() { return ItemStack.EMPTY; }
    public NonNullList<Ingredient> getIngredients() { return NonNullList.create(); }
    @Override public boolean isSpecial() { return true; }
    @Override public boolean showNotification() { return false; }
    @Override public String group() { return "alchemistry"; }
    @Override public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
}
