package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipeSerializer;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RecipeRegistry {

    public static void registerRecipes() {
        // Dissolver
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(Alchemistry.MOD_ID, DissolverRecipeSerializer.ID), DissolverRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, new Identifier(Alchemistry.MOD_ID, DissolverRecipe.Type.ID), DissolverRecipe.Type.INSTANCE);

        // Liquifier
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(Alchemistry.MOD_ID, LiquifierRecipeSerializer.ID), LiquifierRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, new Identifier(Alchemistry.MOD_ID, LiquifierRecipe.Type.ID), LiquifierRecipe.Type.INSTANCE);
    }
}
