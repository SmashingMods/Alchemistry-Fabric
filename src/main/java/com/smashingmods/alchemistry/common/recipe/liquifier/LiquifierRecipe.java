package com.smashingmods.alchemistry.common.recipe.liquifier;

import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class LiquifierRecipe extends AbstractAlchemistryRecipe {

    private final Identifier id;
    private final Ingredient input;
    private final FluidVariant output;
    private final long fluidAmount;
    private final int inputAmount;

    public LiquifierRecipe(Identifier id, Ingredient input, FluidVariant output, long fluidAmount, int inputAmount) {
        super(id);
        this.id = id;
        this.output = output;
        this.input = input;
        this.fluidAmount = fluidAmount;
        this.inputAmount = inputAmount;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        if (world.isClient()) return false;
        return input.test(inventory.getStack(0));
    }

    public Ingredient getInput() {
        return input;
    }

    public FluidVariant getFluidOutput() {
        return output;
    }

    public long getFluidAmount() {
        return fluidAmount;
    }

    public int getInputAmount() {
        return inputAmount;
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
        return LiquifierRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return LiquifierRecipe.Type.INSTANCE;
    }

    public static class Type implements RecipeType<LiquifierRecipe> {
        private Type() { }
        public static final LiquifierRecipe.Type INSTANCE = new LiquifierRecipe.Type();
        public static final String ID = "liquifier";
    }
}
