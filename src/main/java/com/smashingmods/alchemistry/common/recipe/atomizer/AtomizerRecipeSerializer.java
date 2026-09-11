package com.smashingmods.alchemistry.common.recipe.atomizer;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smashingmods.alchemistry.api.recipe.RecipeCodecs;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilitySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class AtomizerRecipeSerializer {
    public static final String ID = "atomizer";
    public static final RecipeSerializer<AtomizerRecipe> INSTANCE = RecipeCodecs.serializer(RecordCodecBuilder.mapCodec(i -> i.group(
        RecipeCodecs.FLUID.fieldOf("input").forGetter(r -> new RecipeCodecs.FluidAmount(r.getFluidInput(), r.getFluidAmount())), RecipeCodecs.STACK.fieldOf("result").forGetter(AtomizerRecipe::getOutputData)
    ).apply(i, (a, b) -> new AtomizerRecipe(RecipeCodecs.UNASSIGNED, a.fluid(), b, a.amount()))));
}
