package com.smashingmods.alchemistry.common.recipe.fission;

import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipeSerializer;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

public class FissionRecipe extends AbstractAlchemistryRecipe {

    private final Identifier id;
    private final ItemStack input;
    private final ItemStack output1;
    private final ItemStack output2;

    public FissionRecipe(Identifier id, ItemStack input, ItemStack output1, ItemStack output2) {
        super(id);
        this.id = id;
        this.input = input;
        this.output1 = output1;
        this.output2 = output2;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        return !world.isClient();
    }

    @Override
    public ItemStack craft(SimpleInventory inventory) {
        return output1;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput1() {
        return output1;
    }

    public ItemStack getOutput2() {
        return output2;
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", input, List.of(output1, output2));
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FissionRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<FissionRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "fission";
    }
}
