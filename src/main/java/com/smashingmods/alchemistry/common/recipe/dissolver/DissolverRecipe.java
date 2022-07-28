package com.smashingmods.alchemistry.common.recipe.dissolver;

import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DissolverRecipe extends AbstractAlchemistryRecipe {

    private final Identifier id;
    private final ProbabilitySet  output;
    private final Ingredient input;

    public DissolverRecipe(Identifier id, ProbabilitySet output, Ingredient input) {
        super(id);
        this.id = id;
        this.output = output;
        this.input = input;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        if (world.isClient()) return false;
        return input.test(inventory.getStack(0));
    }

    @Override
    public ItemStack craft(SimpleInventory inventory) {
        return output.calculateOutput().get(0);
    }

    public Ingredient getInput() {
        return input;
    }

    public ProbabilitySet getProbabilityOutput() {
        return output;
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", input, output);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DissolverRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<DissolverRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "dissolver";
    }
}
