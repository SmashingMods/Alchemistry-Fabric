package com.smashingmods.alchemistry.common.recipe.liquifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class LiquifierRecipeSerializer implements RecipeSerializer<LiquifierRecipe>  {

    public static final LiquifierRecipeSerializer INSTANCE = new LiquifierRecipeSerializer();
    public static final String ID = LiquifierRecipe.Type.ID;

    @Override
    public LiquifierRecipe read(Identifier id, JsonObject json) {
        if (!json.has("input")) {
            throw new JsonSyntaxException("Missing input, expected to find an object.");
        }
        Ingredient input = Ingredient.fromJson(JsonHelper.getObject(json, "input"));
        int inputCount = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "input")).getCount();

        if (!json.has("result")) {
            throw new JsonSyntaxException("Missing result, expected to find an object.");
        }
        JsonObject outputObject = json.getAsJsonObject("result");
        Identifier fluidLocation = new Identifier(outputObject.get("fluid").getAsString());
        int fluidAmount = outputObject.has("amount") ? outputObject.get("amount").getAsInt() : 1000;

        FluidVariant output = FluidVariant.of(Registry.FLUID.get(fluidLocation));
        return new LiquifierRecipe(id, input, output, fluidAmount, inputCount);
    }

    @Override
    public LiquifierRecipe read(Identifier id, PacketByteBuf buf) {
        Ingredient input = Ingredient.fromPacket(buf);
        FluidVariant output = FluidVariant.fromNbt(buf.readNbt());
        long fluidAmount = buf.readLong();
        int inputAmount = buf.readInt();
        return new LiquifierRecipe(id, input, output, fluidAmount, inputAmount);
    }

    @Override
    public void write(PacketByteBuf buf, LiquifierRecipe recipe) {
        recipe.getInput().write(buf);
        buf.writeNbt(recipe.getFluidOutput().toNbt());
        buf.writeLong(recipe.getFluidAmount());
        buf.writeInt(recipe.getInputAmount());
    }
}
