package com.smashingmods.alchemistry.test;

import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;

/** Scoped recipe-map replacement, matching resource reload without changing another test's recipes. */
final class RecipeTestScope implements AutoCloseable {
    private final RecipeManager manager;
    private final Field field;
    private final Object original;

    private RecipeTestScope(ServerLevel level, Collection<RecipeHolder<?>> recipes) {
        manager = level.getServer().getRecipeManager();
        try {
            field = RecipeManager.class.getDeclaredField("recipes");
            field.setAccessible(true);
            original = field.get(manager);
            field.set(manager, RecipeMap.create(recipes));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to replace test recipes", e);
        }
    }

    static RecipeTestScope replace(ServerLevel level, Collection<RecipeHolder<?>> recipes) {
        return new RecipeTestScope(level, recipes);
    }

    static RecipeTestScope withRecipes(ServerLevel level, AbstractAlchemistryRecipe... recipes) {
        var combined = new LinkedHashMap<ResourceKey<Recipe<?>>, RecipeHolder<?>>();
        level.getServer().getRecipeManager().getRecipes().forEach(holder -> combined.put(holder.id(), holder));
        for (var recipe : recipes) {
            var key = ResourceKey.create(Registries.RECIPE, recipe.getId());
            combined.put(key, new RecipeHolder<>(key, recipe));
        }
        return replace(level, combined.values());
    }

    @Override
    public void close() {
        try {
            field.set(manager, original);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Unable to restore test recipes", e);
        }
    }
}
