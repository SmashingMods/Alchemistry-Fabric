package com.smashingmods.alchemistry.common.recipe.atomizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AtomizerRecipeSerializer implements RecipeSerializer<AtomizerRecipe>  {

    public static final AtomizerRecipeSerializer INSTANCE = new AtomizerRecipeSerializer();
    public static final String ID = AtomizerRecipe.Type.ID;

    @Override
    public AtomizerRecipe read(Identifier id, JsonObject json) {
        if (!json.has("input")) {
            throw new JsonSyntaxException("Missing input, expected to find an object.");
        }
        JsonObject inputObject = json.getAsJsonObject("input");
        Identifier fluidLocation = new Identifier(inputObject.get("fluid").getAsString());
        int fluidAmount = inputObject.has("amount") ? inputObject.get("amount").getAsInt() : 1000;
        FluidVariant input = FluidVariant.of(Registry.FLUID.get(fluidLocation));

        if (!json.has("result")) {
            throw new JsonSyntaxException("Missing result, expected to find a string or object.");
        }
        ItemStack output = ShapedRecipe.outputFromJson(json.getAsJsonObject("result"));
        return new AtomizerRecipe(id, input, output, fluidAmount);
    }

    @Override
    public AtomizerRecipe read(Identifier id, PacketByteBuf buf) {
        FluidVariant input = FluidVariant.fromNbt(buf.readNbt());
        ItemStack output = buf.readItemStack();
        long fluidAmount = buf.readLong();
        return new AtomizerRecipe(id, input, output, fluidAmount);
    }

    @Override
    public void write(PacketByteBuf buf, AtomizerRecipe recipe) {
        buf.writeItemStack(recipe.getOutput());
        buf.writeNbt(recipe.getFluidInput().toNbt());
        buf.writeLong(recipe.getFluidAmount());
    }
}
