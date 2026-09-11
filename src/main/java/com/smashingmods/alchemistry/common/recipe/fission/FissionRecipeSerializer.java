package com.smashingmods.alchemistry.common.recipe.fission;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smashingmods.alchemistry.api.recipe.RecipeCodecs;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilitySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class FissionRecipeSerializer {
    public static final String ID = "fission";
    public static final RecipeSerializer<FissionRecipe> INSTANCE = RecipeCodecs.serializer(RecordCodecBuilder.mapCodec(i -> i.group(
        RecipeCodecs.STACK.fieldOf("input").forGetter(FissionRecipe::getInputData), RecipeCodecs.STACK.fieldOf("output1").forGetter(FissionRecipe::getOutput1Data), RecipeCodecs.STACK.fieldOf("output2").forGetter(FissionRecipe::getOutput2Data)
    ).apply(i, (a, b, c) -> new FissionRecipe(RecipeCodecs.UNASSIGNED, a, b, c))));
}
