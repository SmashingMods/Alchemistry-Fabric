package com.smashingmods.alchemistry.common.recipe.fusion;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import com.smashingmods.alchemistry.common.recipe.fission.FissionRecipeSerializer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import com.smashingmods.alchemistry.api.recipe.RecipeStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

import java.util.List;

public class FusionRecipe extends AbstractAlchemistryRecipe {

    private final RecipeStack input1;
    private final RecipeStack input2;
    private final RecipeStack output;

    public FusionRecipe(Identifier id, RecipeStack input1, RecipeStack input2, RecipeStack output) {
        super(id);
        this.input1 = input1;
        this.input2 = input2;
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

    public RecipeStack getInput1Data() { return input1; }

    public ItemStack getInput1() {
        return input1.create();
    }

    public RecipeStack getInput2Data() { return input2; }

    public ItemStack getInput2() {
        return input2.create();
    }

    public RecipeStack getOutputData() { return output; }

    public ItemStack getOutput() {
        return output.create();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(0, Ingredient.of(input1.item().value()));
        ingredients.add(1, Ingredient.of(input2.item().value()));
        return ingredients;
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", List.of(input1, input2), output);
    }


    @Override
    public RecipeSerializer<FusionRecipe> getSerializer() {
        return FusionRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<FusionRecipe> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<FusionRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "fusion";
    }
}
