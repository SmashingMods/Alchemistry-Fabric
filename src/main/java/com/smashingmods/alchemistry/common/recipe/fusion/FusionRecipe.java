package com.smashingmods.alchemistry.common.recipe.fusion;

import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import com.smashingmods.alchemistry.common.recipe.fission.FissionRecipeSerializer;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

public class FusionRecipe extends AbstractAlchemistryRecipe {

    private final Identifier id;
    private final ItemStack input1;
    private final ItemStack input2;
    private final ItemStack output;

    public FusionRecipe(Identifier id, ItemStack input1, ItemStack input2, ItemStack output) {
        super(id);
        this.id = id;
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        return !world.isClient();
    }

    @Override
    public ItemStack craft(SimpleInventory inventory) {
        return output;
    }

    public ItemStack getInput1() {
        return input1;
    }

    public ItemStack getInput2() {
        return input2;
    }

    public ItemStack getOutput() {
        return output;
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", List.of(input1, input2), output);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FusionRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<FusionRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "fusion";
    }
}
