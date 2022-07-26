package com.smashingmods.alchemistry.client.rei;

import com.smashingmods.alchemistry.client.rei.category.atomizer.AtomizerRecipeCategory;
import com.smashingmods.alchemistry.client.rei.category.atomizer.AtomizerRecipeDisplay;
import com.smashingmods.alchemistry.client.rei.category.combiner.CombinerRecipeCategory;
import com.smashingmods.alchemistry.client.rei.category.compactor.CompactorRecipeCategory;
import com.smashingmods.alchemistry.client.rei.category.combiner.CombinerRecipeDisplay;
import com.smashingmods.alchemistry.client.rei.category.compactor.CompactorRecipeDisplay;
import com.smashingmods.alchemistry.client.rei.category.dissolver.DissolverRecipeCategory;
import com.smashingmods.alchemistry.client.rei.category.dissolver.DissolverRecipeDisplay;
import com.smashingmods.alchemistry.client.rei.category.fission.FissionRecipeCategory;
import com.smashingmods.alchemistry.client.rei.category.fission.FissionRecipeDisplay;
import com.smashingmods.alchemistry.client.rei.category.fusion.FusionRecipeCategory;
import com.smashingmods.alchemistry.client.rei.category.fusion.FusionRecipeDisplay;
import com.smashingmods.alchemistry.client.rei.category.liquifier.LiquifierRecipeCategory;
import com.smashingmods.alchemistry.client.rei.category.liquifier.LiquifierRecipeDisplay;
import com.smashingmods.alchemistry.common.block.atomizer.AtomizerScreen;
import com.smashingmods.alchemistry.common.block.combiner.CombinerScreen;
import com.smashingmods.alchemistry.common.block.compactor.CompactorScreen;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverScreen;
import com.smashingmods.alchemistry.common.block.fission.FissionControllerScreen;
import com.smashingmods.alchemistry.common.block.fusion.FusionControllerScreen;
import com.smashingmods.alchemistry.common.block.liquifier.LiquifierScreen;
import com.smashingmods.alchemistry.common.recipe.atomizer.AtomizerRecipe;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
import com.smashingmods.alchemistry.common.recipe.fission.FissionRecipe;
import com.smashingmods.alchemistry.common.recipe.fusion.FusionRecipe;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Arrow;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
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
        registry.add(new DissolverRecipeCategory());
        registry.add(new CombinerRecipeCategory());
        registry.add(new CompactorRecipeCategory());
        registry.add(new LiquifierRecipeCategory());
        registry.add(new AtomizerRecipeCategory());
        registry.add(new FissionRecipeCategory());
        registry.add(new FusionRecipeCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(DissolverRecipe.class, DissolverRecipeDisplay::new);
        registry.registerFiller(CombinerRecipe.class, CombinerRecipeDisplay::new);
        registry.registerFiller(CompactorRecipe.class, CompactorRecipeDisplay::new);
        registry.registerFiller(LiquifierRecipe.class, LiquifierRecipeDisplay::new);
        registry.registerFiller(AtomizerRecipe.class, AtomizerRecipeDisplay::new);
        registry.registerFiller(FissionRecipe.class, FissionRecipeDisplay::new);
        registry.registerFiller(FusionRecipe.class, FusionRecipeDisplay::new);
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerContainerClickArea(new Rectangle(89, 34, 5, 27), DissolverScreen.class, DissolverRecipeDisplay.ID);
        registry.registerContainerClickArea(new Rectangle(58, 41, 27, 5), AtomizerScreen.class, AtomizerRecipeDisplay.ID);
        registry.registerContainerClickArea(new Rectangle(90, 41, 27, 5), LiquifierScreen.class, LiquifierRecipeDisplay.ID);
        registry.registerContainerClickArea(new Rectangle(75, 41, 27, 5), CompactorScreen.class, CompactorRecipeDisplay.ID);
        registry.registerContainerClickArea(new Rectangle(64, 86, 27, 5), CombinerScreen.class, CombinerRecipeDisplay.ID);
        registry.registerContainerClickArea(new Rectangle(74, 41, 27, 5), FissionControllerScreen.class, FissionRecipeDisplay.ID);
        registry.registerContainerClickArea(new Rectangle(91, 41, 27, 5), FusionControllerScreen.class, FusionRecipeDisplay.ID);
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
    public static Slot createInputSlot(BasicDisplay display, int index, int x, int y) {
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
    public static Slot createOutputSlot(BasicDisplay display, int index, int x, int y) {
        if (index >= display.getOutputEntries().size()) {
            return Widgets.createSlot(new Point(x, y));
        }
        EntryIngredient outputIngredient = display.getOutputEntries().get(index);
        return Widgets.createSlot(new Point(x, y)).entries(outputIngredient).disableBackground().markOutput();
    }

    /**
     * Creates an animated progress bar starting at point x,y.
     *
     * @param x the x-coordinate of the starting point.
     * @param y the y-coordinate of the starting point.
     * @return arrow widget object to be added to display widget list.
     */
    public static Arrow createAnimatedArrow(int x, int y) {
        return Widgets.createArrow(new Point(x, y)).animationDurationTicks(20*3);
    }
}
