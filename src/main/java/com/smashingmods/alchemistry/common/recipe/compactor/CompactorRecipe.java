package com.smashingmods.alchemistry.common.recipe.compactor;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import com.smashingmods.alchemistry.api.recipe.RecipeStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

public class CompactorRecipe extends AbstractAlchemistryRecipe {

    private final RecipeStack input;
    private final RecipeStack output;

    public CompactorRecipe(Identifier id, RecipeStack input, RecipeStack output) {
        super(id);
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(RecipeInput inventory, Level level) {
        return !level.isClientSide();
    }

    @Override
    public ItemStack assemble(RecipeInput inventory) {
        return output.create();
    }

    public RecipeStack getInputData() { return input; }

    public ItemStack getInput() {
        return input.create();
    }

    public RecipeStack getOutputData() { return output; }

    public ItemStack getOutput() {
        return output.create();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.withSize(1, Ingredient.of(input.item().value()));
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", input, output);
    }


    @Override
    public RecipeSerializer<CompactorRecipe> getSerializer() {
        return CompactorRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<CompactorRecipe> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<CompactorRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "compactor";
    }
}
