package com.smashingmods.alchemistry.common.recipe.fission;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipeSerializer;
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

public class FissionRecipe extends AbstractAlchemistryRecipe {

    private final RecipeStack input;
    private final RecipeStack output1;
    private final RecipeStack output2;

    public FissionRecipe(Identifier id, RecipeStack input, RecipeStack output1, RecipeStack output2) {
        super(id);
        this.input = input;
        this.output1 = output1;
        this.output2 = output2;
    }

    @Override
    public boolean matches(RecipeInput inventory, Level level) {
        return !level.isClientSide();
    }

    @Override
    public ItemStack assemble(RecipeInput inventory) {
        return output1.create();
    }

    public RecipeStack getInputData() { return input; }

    public ItemStack getInput() {
        return input.create();
    }

    public RecipeStack getOutput1Data() { return output1; }

    public ItemStack getOutput1() {
        return output1.create();
    }

    public RecipeStack getOutput2Data() { return output2; }

    public ItemStack getOutput2() {
        return output2.create();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.withSize(1, Ingredient.of(input.item().value()));
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", input, List.of(output1, output2));
    }


    @Override
    public RecipeSerializer<FissionRecipe> getSerializer() {
        return FissionRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<FissionRecipe> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<FissionRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "fission";
    }
}
