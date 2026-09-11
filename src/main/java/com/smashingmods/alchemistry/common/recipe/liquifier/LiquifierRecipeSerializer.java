package com.smashingmods.alchemistry.common.recipe.liquifier;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smashingmods.alchemistry.api.recipe.RecipeCodecs;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilitySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class LiquifierRecipeSerializer {
    public static final String ID = "liquifier";
    public static final RecipeSerializer<LiquifierRecipe> INSTANCE = RecipeCodecs.serializer(RecordCodecBuilder.mapCodec(i -> i.group(
        RecipeCodecs.STACK.fieldOf("input").forGetter(r -> new com.smashingmods.alchemistry.api.recipe.RecipeStack(r.getInput().items().findFirst().orElseThrow(), r.getInputAmount())), RecipeCodecs.FLUID.fieldOf("result").forGetter(r -> new RecipeCodecs.FluidAmount(r.getFluidOutput(), r.getFluidAmount()))
    ).apply(i, (a, b) -> new LiquifierRecipe(RecipeCodecs.UNASSIGNED, Ingredient.of(a.item().value()), b.fluid(), b.amount(), a.count()))));
}
