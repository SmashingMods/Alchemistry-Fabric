package com.smashingmods.alchemistry.common.recipe.atomizer;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import com.smashingmods.alchemistry.api.recipe.RecipeStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

public class AtomizerRecipe extends AbstractAlchemistryRecipe {

    private final FluidVariant input;
    private final RecipeStack output;
    private final long fluidAmount;

    public AtomizerRecipe(Identifier id, FluidVariant input, RecipeStack output, long fluidAmount) {
        super(id);
        this.output = output;
        this.input = input;
        this.fluidAmount = fluidAmount;
    }

    @Override
    public boolean matches(RecipeInput inventory, Level level) {
        if (level.isClientSide()) return false;
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput inventory) {
        return output.create();
    }

    public FluidVariant getFluidInput() {
        return input;
    }

    public RecipeStack getOutputData() { return output; }

    public ItemStack getOutput() {
        return output.create();
    }

    public long getFluidAmount() {
        return fluidAmount;
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", input, output);
    }


    @Override
    public RecipeSerializer<AtomizerRecipe> getSerializer() {
        return AtomizerRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<AtomizerRecipe> getType() {
        return AtomizerRecipe.Type.INSTANCE;
    }

    public static class Type implements RecipeType<AtomizerRecipe> {
        private Type() { }
        public static final AtomizerRecipe.Type INSTANCE = new AtomizerRecipe.Type();
        public static final String ID = "atomizer";
    }
}
