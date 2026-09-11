package com.smashingmods.alchemistry.common.recipe.fusion;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smashingmods.alchemistry.api.recipe.RecipeCodecs;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilitySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class FusionRecipeSerializer {
    public static final String ID = "fusion";
    public static final RecipeSerializer<FusionRecipe> INSTANCE = RecipeCodecs.serializer(RecordCodecBuilder.mapCodec(i -> i.group(
        RecipeCodecs.STACK.fieldOf("input1").forGetter(FusionRecipe::getInput1Data), RecipeCodecs.STACK.fieldOf("input2").forGetter(FusionRecipe::getInput2Data), RecipeCodecs.STACK.fieldOf("output").forGetter(FusionRecipe::getOutputData)
    ).apply(i, (a, b, c) -> new FusionRecipe(RecipeCodecs.UNASSIGNED, a, b, c))));
}
