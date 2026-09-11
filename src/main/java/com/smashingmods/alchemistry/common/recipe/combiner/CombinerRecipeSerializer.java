package com.smashingmods.alchemistry.common.recipe.combiner;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smashingmods.alchemistry.api.recipe.RecipeCodecs;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilitySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class CombinerRecipeSerializer {
    public static final String ID = "combiner";
    public static final RecipeSerializer<CombinerRecipe> INSTANCE = RecipeCodecs.serializer(RecordCodecBuilder.mapCodec(i -> i.group(
        RecipeCodecs.STACK.listOf().fieldOf("input").forGetter(CombinerRecipe::getInputData), RecipeCodecs.STACK.fieldOf("result").forGetter(CombinerRecipe::getOutputData)
    ).apply(i, (a, b) -> new CombinerRecipe(RecipeCodecs.UNASSIGNED, a, b))));
}
