package com.smashingmods.alchemistry.client.rei.category.combiner;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CombinerRecipeDisplay extends BasicDisplay {
    public static final me.shedaniel.rei.api.common.display.DisplaySerializer<CombinerRecipeDisplay> SERIALIZER = com.smashingmods.alchemistry.client.rei.ReiDisplayCodecs.create(CombinerRecipeDisplay::new);
    @Override public me.shedaniel.rei.api.common.display.DisplaySerializer<CombinerRecipeDisplay> getSerializer() { return SERIALIZER; }


    public static final CategoryIdentifier<CombinerRecipeDisplay> ID = CategoryIdentifier.of(Alchemistry.MOD_ID, "combiner");

    public CombinerRecipeDisplay(CombinerRecipe recipe) {
        this(recipe.getInput().stream().map(EntryIngredients::of).toList(), Collections.singletonList(EntryIngredients.of(recipe.getOutput())), Optional.ofNullable(recipe.getId()));
    }

    public CombinerRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ID;
    }

}
