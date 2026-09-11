package com.smashingmods.alchemistry.common.recipe.compactor;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smashingmods.alchemistry.api.recipe.RecipeCodecs;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilitySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class CompactorRecipeSerializer {
    public static final String ID = "compactor";
    public static final RecipeSerializer<CompactorRecipe> INSTANCE = RecipeCodecs.serializer(RecordCodecBuilder.mapCodec(i -> i.group(
        RecipeCodecs.STACK.fieldOf("input").forGetter(CompactorRecipe::getInputData), RecipeCodecs.STACK.fieldOf("result").forGetter(CompactorRecipe::getOutputData)
    ).apply(i, (a, b) -> new CompactorRecipe(RecipeCodecs.UNASSIGNED, a, b))));
}
