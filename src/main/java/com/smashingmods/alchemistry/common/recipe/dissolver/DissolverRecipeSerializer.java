package com.smashingmods.alchemistry.common.recipe.dissolver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class DissolverRecipeSerializer implements RecipeSerializer<DissolverRecipe> {

    public static final DissolverRecipeSerializer INSTANCE = new DissolverRecipeSerializer();
    public static final String ID = DissolverRecipe.Type.ID;

    @Override
    public DissolverRecipe read(Identifier id, JsonObject json) {
        Ingredient input = Ingredient.fromJson(JsonHelper.getObject(json, "input"));

        JsonObject outputJson = json.getAsJsonObject("output");
        int rolls = outputJson.get("rolls").getAsInt();
        boolean weighted = outputJson.get("weighted").getAsBoolean();
        List<ProbabilityGroup> groups = new ArrayList<>();
        JsonArray groupArray = outputJson.getAsJsonArray("groups");

        for (JsonElement element : groupArray) {
            List<ItemStack> output = new ArrayList<>();
            JsonObject jsonObject = element.getAsJsonObject();

            for (JsonElement stack : jsonObject.getAsJsonArray("results")) {
                try {
                    output.add(getItemStack(stack.getAsJsonObject()));
                } catch (JsonSyntaxException exception) {
                    exception.printStackTrace();
                }
            }
            double probability = jsonObject.get("probability").getAsFloat();
            groups.add(new ProbabilityGroup(output, probability));
        }
        ProbabilitySet output = new ProbabilitySet(groups, weighted, rolls);

        return new DissolverRecipe(id, output, input);
    }

    @Override
    public DissolverRecipe read(Identifier id, PacketByteBuf buf) {
        Ingredient input = Ingredient.fromPacket(buf);
        ProbabilitySet output = ProbabilitySet.read(buf);
        return new DissolverRecipe(id, output, input);
    }

    @Override
    public void write(PacketByteBuf buf, DissolverRecipe recipe) {
        recipe.getInput().write(buf);
        recipe.getProbabilityOutput().write(buf);
    }

    public static ItemStack getItemStack(JsonObject json) {
        String itemName = JsonHelper.getString(json, "item");
        Identifier itemKey = new Identifier(itemName);
        if (!Registry.ITEM.containsId(itemKey)) {
            throw new JsonSyntaxException("Unknown item '" + itemName + "'");
        }
        Item item = Registry.ITEM.get(itemKey);
        return new ItemStack(item, JsonHelper.getInt(json, "count", 1));
    }
}
