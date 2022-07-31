package com.smashingmods.alchemistry.common.recipe.atomizer;

import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class AtomizerRecipe extends AbstractAlchemistryRecipe {

    private final Identifier id;
    private final FluidVariant input;
    private final ItemStack output;
    private final long fluidAmount;

    public AtomizerRecipe(Identifier id, FluidVariant input, ItemStack output, long fluidAmount) {
        super(id);
        this.id = id;
        this.output = output;
        this.input = input;
        this.fluidAmount = fluidAmount;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        if (world.isClient()) return false;
        return true;
    }

    @Override
    public ItemStack craft(SimpleInventory inventory) {
        return output;
    }

    public FluidVariant getFluidInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public long getFluidAmount() {
        return fluidAmount;
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
        return AtomizerRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return AtomizerRecipe.Type.INSTANCE;
    }

    public static class Type implements RecipeType<AtomizerRecipe> {
        private Type() { }
        public static final AtomizerRecipe.Type INSTANCE = new AtomizerRecipe.Type();
        public static final String ID = "atomizer";
    }
}
