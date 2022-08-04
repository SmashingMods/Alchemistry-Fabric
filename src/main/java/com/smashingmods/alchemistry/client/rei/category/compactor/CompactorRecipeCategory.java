package com.smashingmods.alchemistry.client.rei.category.compactor;

import com.google.common.collect.Lists;
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
public class CompactorRecipeCategory implements DisplayCategory<CompactorRecipeDisplay> {

    @Override
    public CategoryIdentifier<CompactorRecipeDisplay> getCategoryIdentifier() {
        return CompactorRecipeDisplay.ID;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("alchemistry.jei."+CompactorRecipeDisplay.ID.getPath());
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ItemRegistry.COMPACTOR);
    }

    @Override
    public List<Widget> setupDisplay(CompactorRecipeDisplay display, Rectangle bounds) {
        // Setup background
        Point startPoint = new Point(bounds.getCenterX() - 36, bounds.getCenterY() - 13);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));

        // Add arrow
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 27, startPoint.y + 4)));

        //Add output slot
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 61, startPoint.y + 5)));
        widgets.add(createOutputSlot(display, 0, startPoint.x + 61, startPoint.y + 5));

        // Add input slot
        widgets.add(createInputSlot(display, 0, startPoint.x + 4, startPoint.y + 5));
        return widgets;
    }
}
