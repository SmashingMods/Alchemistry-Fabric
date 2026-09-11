package com.smashingmods.alchemistry.common.recipe.dissolver;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smashingmods.alchemistry.api.recipe.RecipeCodecs;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilitySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class DissolverRecipeSerializer {
    public static final String ID = "dissolver";
    public static final RecipeSerializer<DissolverRecipe> INSTANCE = RecipeCodecs.serializer(RecordCodecBuilder.mapCodec(i -> i.group(
        Ingredient.CODEC.fieldOf("input").forGetter(DissolverRecipe::getInput), ProbabilitySet.CODEC.fieldOf("output").forGetter(DissolverRecipe::getProbabilityOutput)
    ).apply(i, (a, b) -> new DissolverRecipe(RecipeCodecs.UNASSIGNED, b, a))));
}
