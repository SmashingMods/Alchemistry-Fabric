package com.smashingmods.alchemistry.client.rei;

import com.smashingmods.alchemistry.client.rei.category.combiner.CombinerRecipeCategory;
import com.smashingmods.alchemistry.client.rei.category.compactor.CompactorRecipeCategory;
import com.smashingmods.alchemistry.client.rei.category.combiner.CombinerRecipeDisplay;
import com.smashingmods.alchemistry.client.rei.category.compactor.CompactorRecipeDisplay;
import com.smashingmods.alchemistry.client.rei.category.liquifier.LiquifierRecipeCategory;
import com.smashingmods.alchemistry.client.rei.category.liquifier.LiquifierRecipeDisplay;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
import com.smashingmods.chemlib.common.fluids.ChemicalFluid;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

import java.util.stream.Stream;

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
        registry.add(new CompactorRecipeCategory());
        registry.add(new LiquifierRecipeCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(CombinerRecipe.class, CombinerRecipeDisplay::new);
        registry.registerFiller(CompactorRecipe.class, CompactorRecipeDisplay::new);
        registry.registerFiller(LiquifierRecipe.class, LiquifierRecipeDisplay::new);
    }

    @Override
    public void registerFluidSupport(FluidSupportProvider support) {
        support.register(new FluidSupportProvider.Provider() {
            @Override
            public CompoundEventResult<Stream<EntryStack<FluidStack>>> itemToFluid(EntryStack<? extends ItemStack> stack) {
                /**
                ItemStack itemStack = stack.getValue();
                if (itemStack.getItem() instanceof Flowab) {
                    Fluid fluid = ((ChemicalFluid) itemStack.getItem()).getFluid(itemStack);
                    if (fluid != null)
                        return CompoundEventResult.interruptTrue(Stream.of(EntryStacks.of(fluid)));
                }
                 */
                return CompoundEventResult.pass();
            }
        });
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
}
