package com.smashingmods.alchemistry.client.rei.category.fission;

import com.google.common.collect.Lists;
import com.smashingmods.alchemistry.client.rei.category.combiner.CombinerRecipeDisplay;
import com.smashingmods.alchemistry.registry.ItemRegistry;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.List;

import static com.smashingmods.alchemistry.client.rei.ReiPlugin.*;

@Environment(EnvType.CLIENT)
public class FissionRecipeCategory implements DisplayCategory<FissionRecipeDisplay> {

    @Override
    public CategoryIdentifier<FissionRecipeDisplay> getCategoryIdentifier() {
        return FissionRecipeDisplay.ID;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("alchemistry.jei."+FissionRecipeDisplay.ID.getPath());
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ItemRegistry.COMBINER);
    }

    @Override
    public List<Widget> setupDisplay(FissionRecipeDisplay display, Rectangle bounds) {
        // Setup background
        Point startPoint = new Point(bounds.getCenterX() - 36, bounds.getCenterY() - 13);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));

        // Add arrow
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 27, startPoint.y + 4)));

        //Add output slots
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y - 5))
                .entries(display.getOutputEntries().get(0)).markOutput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y - 5 + SLOT_SIZE))
                .entries(display.getOutputEntries().get(1)).markOutput());

        // Add input slots
        widgets.add(createInputSlot(display, 0, startPoint.x + 4, startPoint.y + 5));
        return widgets;
    }
}
