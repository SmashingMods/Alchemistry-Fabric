package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.recipe.atomizer.AtomizerRecipe;
import com.smashingmods.alchemistry.common.recipe.atomizer.AtomizerRecipeSerializer;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipeSerializer;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipeSerializer;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipeSerializer;
import com.smashingmods.alchemistry.common.recipe.fission.FissionRecipe;
import com.smashingmods.alchemistry.common.recipe.fission.FissionRecipeSerializer;
import com.smashingmods.alchemistry.common.recipe.fusion.FusionRecipe;
import com.smashingmods.alchemistry.common.recipe.fusion.FusionRecipeSerializer;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipeSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class RecipeRegistry {

    public static void registerRecipes() {
        // Dissolver
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, DissolverRecipeSerializer.ID), DissolverRecipeSerializer.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, DissolverRecipe.Type.ID), DissolverRecipe.Type.INSTANCE);

        // Liquifier
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, LiquifierRecipeSerializer.ID), LiquifierRecipeSerializer.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, LiquifierRecipe.Type.ID), LiquifierRecipe.Type.INSTANCE);

        // Atomizer
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, AtomizerRecipeSerializer.ID), AtomizerRecipeSerializer.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, AtomizerRecipe.Type.ID), AtomizerRecipe.Type.INSTANCE);

        // Compactor
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, CompactorRecipeSerializer.ID), CompactorRecipeSerializer.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, CompactorRecipe.Type.ID), CompactorRecipe.Type.INSTANCE);

        // Combiner
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, CombinerRecipeSerializer.ID), CombinerRecipeSerializer.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, CombinerRecipe.Type.ID), CombinerRecipe.Type.INSTANCE);

        // Fission
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, FissionRecipeSerializer.ID), FissionRecipeSerializer.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, FissionRecipe.Type.ID), FissionRecipe.Type.INSTANCE);

        // Fusion
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, FusionRecipeSerializer.ID), FusionRecipeSerializer.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, FusionRecipe.Type.ID), FusionRecipe.Type.INSTANCE);
    }
}
