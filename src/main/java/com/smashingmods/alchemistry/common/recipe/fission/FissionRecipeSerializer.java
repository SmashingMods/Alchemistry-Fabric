package com.smashingmods.alchemistry.common.recipe.fission;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

public class FissionRecipeSerializer implements RecipeSerializer<FissionRecipe> {

    public static final FissionRecipeSerializer INSTANCE = new FissionRecipeSerializer();
    public static final String ID = FissionRecipe.Type.ID;

    @Override
    public FissionRecipe read(Identifier id, JsonObject json) {
        ItemStack input = ShapedRecipe.outputFromJson(json.getAsJsonObject("input"));
        ItemStack output1 = ShapedRecipe.outputFromJson(json.getAsJsonObject("output1"));
        ItemStack output2 = ShapedRecipe.outputFromJson(json.getAsJsonObject("output2"));
        return new FissionRecipe(id, input, output1, output2);
    }

    @Override
    public FissionRecipe read(Identifier id, PacketByteBuf buf) {
        ItemStack input = buf.readItemStack();
        ItemStack output1 = buf.readItemStack();
        ItemStack output2 = buf.readItemStack();
        return new FissionRecipe(id, input, output1, output2);
    }

    @Override
    public void write(PacketByteBuf buf, FissionRecipe recipe) {
        buf.writeItemStack(recipe.getInput());
        buf.writeItemStack(recipe.getOutput1());
        buf.writeItemStack(recipe.getOutput2());
    }
}
