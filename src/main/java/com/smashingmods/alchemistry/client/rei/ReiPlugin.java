package com.smashingmods.alchemistry.client.rei;

import com.smashingmods.alchemistry.client.rei.category.CombinerRecipeCategory;
import com.smashingmods.alchemistry.client.rei.display.CombinerRecipeDisplay;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.registry.BlockRegistry;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * REI support showing custom recipe types.
 *
 * @author TechnoVision
 */
@Environment(EnvType.CLIENT)
public class ReiPlugin implements REIClientPlugin {

    public static final int SLOT_SIZE = 19;

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new CombinerRecipeCategory());
        registry.addWorkstations(CombinerRecipeDisplay.ID, EntryStacks.of(BlockRegistry.COMBINER));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(CombinerRecipe.class, CombinerRecipeDisplay::new);
    }

    /**
     * Creates an input slot at a specified point with the specified ingredient index.
     *
     * @param display the display to get recipe data from.
     * @param index the index of the input ingredient to display.
     * @param x the x-coordinate of the top left corner of the slot.
     * @param y the y-coordinate of the top left corner of the slot.
     * @return a widget object to be added to display widget list.
     */
    public static Slot createInputSlot(CombinerRecipeDisplay display, int index, int x, int y) {
        if (index >= display.getInputEntries().size()) {
            return Widgets.createSlot(new Point(x, y));
        }
        EntryIngredient ingredient = display.getInputEntries().get(index);
        return Widgets.createSlot(new Point(x, y)).entries(ingredient).markInput();
    }

    /**
     * Creates an output slot at a specified point with the specified ingredient index.
     *
     * @param display the display to get recipe data from.
     * @param index the index of the output ingredient to display.
     * @param x the x-coordinate of the top left corner of the slot.
     * @param y the y-coordinate of the top left corner of the slot.
     * @return a widget object to be added to display widget list.
     */
    public static Slot createOutputSlot(CombinerRecipeDisplay display, int index, int x, int y) {
        if (index >= display.getOutputEntries().size()) {
            return Widgets.createSlot(new Point(x, y));
        }
        EntryIngredient outputIngredient = display.getOutputEntries().get(index);
        return Widgets.createSlot(new Point(x, y)).entries(outputIngredient).disableBackground().markOutput();
    }
}
