package com.smashingmods.alchemistry.common.recipe.combiner;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CombinerRecipeSerializer implements RecipeSerializer<CombinerRecipe> {

    public static final CombinerRecipeSerializer INSTANCE = new CombinerRecipeSerializer();
    public static final String ID = CombinerRecipe.Type.ID;

    @Override
    public CombinerRecipe read(Identifier id, JsonObject json) {
        JsonArray inputJson = json.getAsJsonArray("input");
        List<ItemStack> input = new ArrayList<>();
        ItemStack output;

        inputJson.forEach(element -> input.add(ShapedRecipe.outputFromJson(element.getAsJsonObject())));

        if (json.get("result").isJsonObject()) {
            output = ShapedRecipe.outputFromJson(json.getAsJsonObject("result"));
        } else {
            output = ShapedRecipe.outputFromJson(json.getAsJsonObject("item"));
        }

        return new CombinerRecipe(id, input, output);
    }

    @Override
    public CombinerRecipe read(Identifier id, PacketByteBuf buf) {
        List<ItemStack> input = Lists.newArrayList();
        for (int i = 0; i < 4; i++) {
            input.add(buf.readItemStack());
        }
        ItemStack output = buf.readItemStack();
        return new CombinerRecipe(id, input, output);
    }

    @Override
    public void write(PacketByteBuf buf, CombinerRecipe recipe) {
        for (int i = 0; i < 4; i++) {
            buf.writeItemStack(recipe.getInput().get(i));
        }
        buf.writeItemStack(recipe.getOutput());
    }
}
