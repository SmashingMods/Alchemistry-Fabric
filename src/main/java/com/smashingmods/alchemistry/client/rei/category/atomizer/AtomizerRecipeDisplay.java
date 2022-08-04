package com.smashingmods.alchemistry.client.rei.category.atomizer;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.recipe.atomizer.AtomizerRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class AtomizerRecipeDisplay extends BasicDisplay {

    public static final CategoryIdentifier<AtomizerRecipeDisplay> ID = CategoryIdentifier.of(Alchemistry.MOD_ID, "atomizer");

    public AtomizerRecipeDisplay(AtomizerRecipe recipe) {
        this(Collections.singletonList(EntryIngredients.of(recipe.getFluidInput().getFluid())), Collections.singletonList(EntryIngredients.of(recipe.getOutput())), Optional.ofNullable(recipe.getId()));
    }

    public AtomizerRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ID;
    }

}
