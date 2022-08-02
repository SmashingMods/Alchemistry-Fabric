package com.smashingmods.alchemistry.common.recipe.fusion;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

public class FusionRecipeSerializer implements RecipeSerializer<FusionRecipe> {

    public static final FusionRecipeSerializer INSTANCE = new FusionRecipeSerializer();
    public static final String ID = FusionRecipe.Type.ID;

    @Override
    public FusionRecipe read(Identifier id, JsonObject json) {
        ItemStack input1 = ShapedRecipe.outputFromJson(json.getAsJsonObject("input1"));
        ItemStack input2 = ShapedRecipe.outputFromJson(json.getAsJsonObject("input2"));
        ItemStack output = ShapedRecipe.outputFromJson(json.getAsJsonObject("output"));
        return new FusionRecipe(id, input1, input2, output);
    }

    @Override
    public FusionRecipe read(Identifier id, PacketByteBuf buf) {
        ItemStack input1 = buf.readItemStack();
        ItemStack input2 = buf.readItemStack();
        ItemStack output = buf.readItemStack();
        return new FusionRecipe(id, input1, input2, output);
    }

    @Override
    public void write(PacketByteBuf buf, FusionRecipe recipe) {
        buf.writeItemStack(recipe.getInput1());
        buf.writeItemStack(recipe.getInput2());
        buf.writeItemStack(recipe.getOutput());
    }
}
