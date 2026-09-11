package com.smashingmods.alchemistry.common.recipe.dissolver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import java.util.*;
import java.util.random.RandomGenerator;

public class ProbabilitySet {
    public static final com.mojang.serialization.Codec<ProbabilitySet> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.create(i -> i.group(
        ProbabilityGroup.CODEC.listOf().fieldOf("groups").forGetter(ProbabilitySet::getProbabilityGroups),
        com.mojang.serialization.Codec.BOOL.fieldOf("weighted").forGetter(ProbabilitySet::isWeighted),
        com.mojang.serialization.Codec.intRange(1, 10000).fieldOf("rolls").forGetter(ProbabilitySet::getRolls)
    ).apply(i, ProbabilitySet::new));


    private final List<ProbabilityGroup> probabilityGroups;
    private final boolean weighted;
    private final int rolls;

    public ProbabilitySet(List<ProbabilityGroup> pProbabilityGroups) {
        this(pProbabilityGroups, true, 1);
    }

    public ProbabilitySet(List<ProbabilityGroup> pProbabilityGroups, boolean pWeighted, int pRolls) {
        this.probabilityGroups = pProbabilityGroups;
        this.weighted = pWeighted;
        this.rolls = pRolls;
    }

    public JsonElement serialize() {
        JsonObject toReturn = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        toReturn.add("rolls", new JsonPrimitive(rolls));
        toReturn.add("weighted", new JsonPrimitive(weighted));

        for (ProbabilityGroup group : probabilityGroups) {
            jsonArray.add(group.serialize());
        }
        toReturn.add("groups", jsonArray);

        return toReturn;
    }



    public NonNullList<ItemStack> calculateOutput() {
        return calculateOutput(new Random());
    }

    public NonNullList<ItemStack> calculateOutput(RandomGenerator random) {
        NonNullList<ItemStack> toReturn = NonNullList.create();
        double totalProbability = weighted ? getTotalProbability() : 0;

        for (int i = 1; i <= rolls; i++) {
            if (weighted) {
                double targetProbability = random.nextDouble();
                double outputProbability = 0.0;

                for (ProbabilityGroup group : probabilityGroups) {
                    outputProbability += (group.getProbability() / totalProbability);

                    if (targetProbability < outputProbability) {
                        group.getOutput().forEach(itemStack -> populateReturnList(toReturn, itemStack));
                        break;
                    }
                }
            } else {
                for (ProbabilityGroup group : probabilityGroups) {
                    if (random.nextDouble() < group.getProbability() / 100.0) {
                        group.getOutput().forEach(itemStack -> populateReturnList(toReturn, itemStack));
                    }
                }
            }
        }
        return toReturn;
    }

    private void populateReturnList(NonNullList<ItemStack> pList, ItemStack pItemStack) {

        if (pItemStack.isEmpty()) return;
        int count = pItemStack.getCount();
        for (ItemStack stack : pList) {
            if (ItemStack.isSameItemSameComponents(pItemStack, stack)) {
                int amount = Math.min(count, Math.max(0, stack.getMaxStackSize() - stack.getCount()));
                stack.grow(amount);
                count -= amount;
                if (count == 0) return;
            }
        }
        while (count > 0) {
            int amount = Math.min(count, pItemStack.getMaxStackSize());
            pList.add(pItemStack.copyWithCount(amount));
            count -= amount;
        }
    }


    public List<ProbabilityGroup> getProbabilityGroups() {
        return probabilityGroups;
    }

    public boolean isWeighted() {
        return weighted;
    }

    public int getRolls() {
        return rolls;
    }

    private double getTotalProbability() {
        return probabilityGroups.stream()
                .mapToDouble(ProbabilityGroup::getProbability)
                .sum();
    }

    public static class Builder {
        private final List<ProbabilityGroup> groups = new ArrayList<>();
        private boolean weighted = false;
        private int rolls = 1;

        public Builder() {}

        public static Builder createSet() {
            return new Builder();
        }

        public Builder addGroup(ProbabilityGroup pGroup) {
            groups.add(pGroup);
            return this;
        }

        public Builder addGroup(List<ItemStack> itemStacks) {
            groups.add(new ProbabilityGroup(itemStacks));
            return this;
        }

        public Builder addGroup(List<ItemStack> itemStacks, double pProbability) {
            groups.add(new ProbabilityGroup(itemStacks, pProbability));
            return this;
        }

        public Builder addGroup(ItemStack... pItemStacks) {
            groups.add(new ProbabilityGroup(Arrays.asList(pItemStacks)));
            return this;
        }

        public Builder addGroup(double pProbability, ItemStack... pItemStacks) {
            if (pItemStacks.length == 0) {
                groups.add(new ProbabilityGroup(List.of(ItemStack.EMPTY), pProbability));
            } else {
                groups.add(new ProbabilityGroup(Arrays.asList(pItemStacks), pProbability));
            }
            return this;
        }

        public Builder rolls(int rolls) {
            this.rolls = rolls;
            return this;
        }

        public Builder weighted() {
            this.weighted = true;
            return this;
        }

        public ProbabilitySet build() {
            return new ProbabilitySet(groups, weighted, rolls);
        }
    }
}
