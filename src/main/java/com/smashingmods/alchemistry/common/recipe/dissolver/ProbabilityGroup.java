package com.smashingmods.alchemistry.common.recipe.dissolver;


import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

import java.util.List;
import java.util.Objects;

public class ProbabilityGroup {

    private final List<ItemStack> output;
    private final double probability;

    public ProbabilityGroup(List<ItemStack> pOutput, double pProbability) {
        this.output = pOutput;
        this.probability = pProbability;
    }

    public ProbabilityGroup(List<ItemStack> pOutput) {
        this.output = pOutput;
        this.probability = 1;
    }

    public List<ItemStack> getOutput() {
        return this.output;
    }

    public double getProbability() {
        return this.probability;
    }

    public JsonElement serialize() {
        JsonObject output = new JsonObject();
        output.add("probability", new JsonPrimitive(probability));
        JsonArray results = new JsonArray();

        for (ItemStack itemStack : this.output) {
            Objects.requireNonNull(itemStack.getItem().getName());
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("item", new JsonPrimitive(itemStack.getItem().getName().toString()));

            if (itemStack.getCount() > 1) {
                jsonObject.add("count", new JsonPrimitive(itemStack.getCount()));
            }
            results.add(jsonObject);
        }
        output.add("results", results);
        return output;
    }

    public void write(PacketByteBuf buf) {
        buf.writeInt(output.size());
        for (ItemStack stack : output) {
            buf.writeItemStack(stack);
        }
        buf.writeDouble(probability);
    }

    public static ProbabilityGroup read(PacketByteBuf buf) {
        List<ItemStack> stacks = Lists.newArrayList();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            stacks.add(buf.readItemStack());
        }
        double probability = buf.readDouble();
        return new ProbabilityGroup(stacks, probability);
    }
}
