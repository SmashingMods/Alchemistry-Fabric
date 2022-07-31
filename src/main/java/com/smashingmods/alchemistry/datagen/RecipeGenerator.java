package com.smashingmods.alchemistry.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.chemlib.api.MatterState;
import com.smashingmods.chemlib.registry.ItemRegistry;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates custom recipes for each of the processing blocks.
 * Used in RecipeManagerMixin during registration of recipes.
 *
 * @author TechnoVision
 */
public class RecipeGenerator {

    public static final Map<Identifier, JsonElement> RECIPES = new HashMap<>();

    /**
     * Fills the recipe map of identifiers to JSON elements for each recipe.
     */
    public static void generateRecipes() {
        // Generate Liquifier and Atomizer Recipes
        ItemRegistry.getElements().stream()
                .filter(element -> element.getMatterState().equals(MatterState.LIQUID) || element.getMatterState().equals(MatterState.GAS) && !element.isArtificial())
                .forEach(filteredElement -> {
                    createLiquifierRecipe(filteredElement.getChemicalName());
                    createAtomizerRecipe(filteredElement.getChemicalName());
                });
        ItemRegistry.getCompounds().stream()
                .filter(compound -> compound.getMatterState().equals(MatterState.LIQUID) || compound.getMatterState().equals(MatterState.GAS))
                .forEach(filteredElement -> {
                    createLiquifierRecipe(filteredElement.getChemicalName());
                    createAtomizerRecipe(filteredElement.getChemicalName());
                });
    }

    /**
     * Generates a recipe for the liquifier processing block and adds it to recipe map.
     * @param chemical the name of the chemical to process into fluid.
     */
    private static void createLiquifierRecipe(String chemical) {
        JsonObject json = new JsonObject();
        json.addProperty("type", Alchemistry.MOD_ID + ":" + "liquifier");
        json.addProperty("group", Alchemistry.MOD_ID + ":" + "liquifier");

        JsonObject input = new JsonObject();
        input.addProperty("item", "chemlib:"+chemical);
        input.addProperty("count", 8);
        json.add("input", input);

        JsonObject result = new JsonObject();
        if (chemical.equals("water")) {
            result.addProperty("fluid", Registry.FLUID.getId(Fluids.WATER).toString());
        } else {
            result.addProperty("fluid", "chemlib:" + chemical + "_source");
        }
        result.addProperty("amount", "500");
        json.add("result", result);

        RECIPES.put(new Identifier(Alchemistry.MOD_ID, "liquifier/"+chemical), json);
    }

    /**
     * Generates a recipe for the atomizer processing block and adds it to recipe map.
     * @param chemical the name of the chemical fluid to process into an element.
     */
    private static void createAtomizerRecipe(String chemical) {
        JsonObject json = new JsonObject();
        json.addProperty("type", Alchemistry.MOD_ID + ":" + "atomizer");
        json.addProperty("group", Alchemistry.MOD_ID + ":" + "atomizer");

        JsonObject input = new JsonObject();
        if (chemical.equals("water")) {
            input.addProperty("fluid", Registry.FLUID.getId(Fluids.WATER).toString());
        } else {
            input.addProperty("fluid", "chemlib:" + chemical + "_source");
        }
        input.addProperty("amount", "500");
        json.add("input", input);

        JsonObject result = new JsonObject();
        result.addProperty("item", "chemlib:"+chemical);
        result.addProperty("count", 8);
        json.add("result", result);

        RECIPES.put(new Identifier(Alchemistry.MOD_ID, "atomizer/"+chemical), json);
    }
}
