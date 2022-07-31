package com.smashingmods.alchemistry.common.recipe.compactor;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

public class CompactorRecipeSerializer implements RecipeSerializer<CompactorRecipe> {

    public static final CompactorRecipeSerializer INSTANCE = new CompactorRecipeSerializer();
    public static final String ID = CompactorRecipe.Type.ID;

    @Override
    public CompactorRecipe read(Identifier id, JsonObject json) {
        ItemStack input = ShapedRecipe.outputFromJson(json.getAsJsonObject("input"));
        ItemStack output = ShapedRecipe.outputFromJson(json.getAsJsonObject("result"));
        return new CompactorRecipe(id, input, output);
    }

    @Override
    public CompactorRecipe read(Identifier id, PacketByteBuf buf) {
        ItemStack input = buf.readItemStack();
        ItemStack output = buf.readItemStack();
        return new CompactorRecipe(id, output, input);
    }

    @Override
    public void write(PacketByteBuf buf, CompactorRecipe recipe) {
        buf.writeItemStack(recipe.getInput());
        buf.writeItemStack(recipe.getOutput());
    }
}
