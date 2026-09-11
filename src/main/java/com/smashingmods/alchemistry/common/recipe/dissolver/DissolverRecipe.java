package com.smashingmods.alchemistry.common.recipe.dissolver;

import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class DissolverRecipe extends AbstractAlchemistryRecipe {

    private final ProbabilitySet  output;
    private final Ingredient input;

    public DissolverRecipe(Identifier id, ProbabilitySet output, Ingredient input) {
        super(id);
        this.output = output;
        this.input = input;
    }

    @Override
    public boolean matches(RecipeInput inventory, Level level) {
        if (level.isClientSide()) return false;
        return input.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(RecipeInput inventory) {
        return output.calculateOutput().get(0);
    }

    public Ingredient getInput() {
        return input;
    }

    public ProbabilitySet getProbabilityOutput() {
        return output;
    }

    public List<ItemStack> getAllResults() {
        List<ItemStack> results = new ArrayList<>();
        for (ProbabilityGroup group : output.getProbabilityGroups()) {
            results.addAll(group.getOutput());
        }
        return results;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.withSize(1, input);
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", input, output);
    }


    @Override
    public RecipeSerializer<DissolverRecipe> getSerializer() {
        return DissolverRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<DissolverRecipe> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<DissolverRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "dissolver";
    }
}
