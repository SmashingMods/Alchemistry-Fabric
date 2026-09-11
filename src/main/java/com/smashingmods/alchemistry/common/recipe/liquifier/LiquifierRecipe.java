package com.smashingmods.alchemistry.common.recipe.liquifier;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

public class LiquifierRecipe extends AbstractAlchemistryRecipe {

    private final Ingredient input;
    private final FluidVariant output;
    private final long fluidAmount;
    private final int inputAmount;

    public LiquifierRecipe(Identifier id, Ingredient input, FluidVariant output, long fluidAmount, int inputAmount) {
        super(id);
        this.output = output;
        this.input = input;
        this.fluidAmount = fluidAmount;
        this.inputAmount = inputAmount;
    }

    @Override
    public boolean matches(RecipeInput inventory, Level level) {
        if (level.isClientSide()) return false;
        return input.test(inventory.getItem(0));
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
    public NonNullList<Ingredient> getIngredients() {
        ItemStack stack = new ItemStack(input.items().findFirst().orElseThrow().value(), inputAmount);
        return NonNullList.withSize(1, Ingredient.of(stack.getItem()));
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", input, output);
    }


    @Override
    public RecipeSerializer<LiquifierRecipe> getSerializer() {
        return LiquifierRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<LiquifierRecipe> getType() {
        return LiquifierRecipe.Type.INSTANCE;
    }

    public static class Type implements RecipeType<LiquifierRecipe> {
        private Type() { }
        public static final LiquifierRecipe.Type INSTANCE = new LiquifierRecipe.Type();
        public static final String ID = "liquifier";
    }
}
