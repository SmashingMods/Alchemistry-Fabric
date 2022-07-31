package com.smashingmods.alchemistry.common.recipe.compactor;

import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CompactorRecipe extends AbstractAlchemistryRecipe {

    private final Identifier id;
    private final ItemStack input;
    private final ItemStack output;

    public CompactorRecipe(Identifier id, ItemStack input, ItemStack output) {
        super(id);
        this.id = id;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        if (world.isClient()) return false;
        ItemStack invStack = inventory.getStack(0);
        return ItemStack.areItemsEqual(input, invStack) && invStack.getCount() >= input.getCount();
    }

    @Override
    public ItemStack craft(SimpleInventory inventory) {
        return output;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
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
        return CompactorRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<CompactorRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "compactor";
    }
}
