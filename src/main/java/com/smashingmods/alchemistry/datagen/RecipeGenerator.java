package com.smashingmods.alchemistry.datagen;
import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import com.smashingmods.alchemistry.common.recipe.atomizer.AtomizerRecipe;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
import com.smashingmods.chemlib.registry.ItemRegistry;
import com.smashingmods.chemlib.api.MatterState;
import java.util.*;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
public final class RecipeGenerator {
    public static RecipeMap addGeneratedRecipes(RecipeMap recipes) {
        Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> result = new LinkedHashMap<>();
        recipes.values().forEach(holder -> result.put(holder.id(), holder));
        Set<String> chemicals = new LinkedHashSet<>();
        ItemRegistry.getElements().stream().filter(e -> e.getMatterState() == MatterState.LIQUID || e.getMatterState() == MatterState.GAS && !e.isArtificial()).forEach(e -> chemicals.add(e.getChemicalName()));
        ItemRegistry.getCompounds().stream().filter(e -> e.getMatterState() == MatterState.LIQUID || e.getMatterState() == MatterState.GAS).forEach(e -> chemicals.add(e.getChemicalName()));
        for (String chemical : chemicals) {
            Identifier fluidId = Identifier.parse(chemical.equals("water") ? "minecraft:water" : "chemlib:" + chemical + "_source");
            Identifier itemId = Identifier.parse("chemlib:" + chemical);
            if (!BuiltInRegistries.FLUID.containsKey(fluidId) || !BuiltInRegistries.ITEM.containsKey(itemId)) continue;
            FluidVariant fluid = FluidVariant.of(BuiltInRegistries.FLUID.getValue(fluidId));
            Item item = BuiltInRegistries.ITEM.getValue(itemId);
            Identifier atomizer = Identifier.parse("alchemistry:atomizer/" + chemical);
            Identifier liquifier = Identifier.parse("alchemistry:liquifier/" + chemical);
            add(result, new AtomizerRecipe(atomizer, fluid, com.smashingmods.alchemistry.api.recipe.RecipeStack.of(item, 8), 500));
            add(result, new LiquifierRecipe(liquifier, Ingredient.of(item), fluid, 500, 8));
        }
        result.values().forEach(holder -> { if (holder.value() instanceof AbstractAlchemistryRecipe recipe) recipe.setId(holder.id().identifier()); });
        return RecipeMap.create(result.values());
    }
    private static void add(Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> recipes, AbstractAlchemistryRecipe recipe) {
        ResourceKey<Recipe<?>> id = ResourceKey.create(Registries.RECIPE, recipe.getId());
        recipes.putIfAbsent(id, new RecipeHolder<>(id, recipe));
    }
}
