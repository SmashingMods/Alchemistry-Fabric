package com.smashingmods.alchemistry.common.recipe.dissolver;


import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.world.item.ItemStack;
import com.smashingmods.alchemistry.api.recipe.RecipeStack;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.Objects;

public class ProbabilityGroup {
    public static final com.mojang.serialization.Codec<ProbabilityGroup> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.create(i -> i.group(
        com.smashingmods.alchemistry.api.recipe.RecipeCodecs.STACK.listOf().fieldOf("results").forGetter(ProbabilityGroup::getOutputData),
        com.mojang.serialization.Codec.doubleRange(0, Double.MAX_VALUE).fieldOf("probability").forGetter(ProbabilityGroup::getProbability)
    ).apply(i, ProbabilityGroup::fromData));


    private final List<RecipeStack> output;
    private final double probability;

    public ProbabilityGroup(List<ItemStack> pOutput, double pProbability) {
        this.output = pOutput.stream().map(RecipeStack::of).toList();
        this.probability = pProbability;
    }

    public ProbabilityGroup(List<ItemStack> pOutput) {
        this.output = pOutput.stream().map(RecipeStack::of).toList();
        this.probability = 1;
    }

    private ProbabilityGroup(List<RecipeStack> data, double probability, boolean deferred) { this.output = data; this.probability = probability; }
    public static ProbabilityGroup fromData(List<RecipeStack> data, double probability) { return new ProbabilityGroup(data, probability, true); }
    public List<RecipeStack> getOutputData() { return output; }
    public List<ItemStack> getOutput() {
        return this.output.stream().map(RecipeStack::create).toList();
    }

    public double getProbability() {
        return this.probability;
    }

    public JsonElement serialize() {
        JsonObject output = new JsonObject();
        output.add("probability", new JsonPrimitive(probability));
        JsonArray results = new JsonArray();

        for (ItemStack itemStack : getOutput()) {
            Objects.requireNonNull(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("item", new JsonPrimitive(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString()));

            if (itemStack.getCount() > 1) {
                jsonObject.add("count", new JsonPrimitive(itemStack.getCount()));
            }
            results.add(jsonObject);
        }
        output.add("results", results);
        return output;
    }


}
