package com.smashingmods.alchemistry.client.rei.category.compactor;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
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

public class CompactorRecipeDisplay extends BasicDisplay {
    public static final me.shedaniel.rei.api.common.display.DisplaySerializer<CompactorRecipeDisplay> SERIALIZER = com.smashingmods.alchemistry.client.rei.ReiDisplayCodecs.create(CompactorRecipeDisplay::new);
    @Override public me.shedaniel.rei.api.common.display.DisplaySerializer<CompactorRecipeDisplay> getSerializer() { return SERIALIZER; }


    public static final CategoryIdentifier<CompactorRecipeDisplay> ID = CategoryIdentifier.of(Alchemistry.MOD_ID, "compactor");

    public CompactorRecipeDisplay(CompactorRecipe recipe) {
        this(List.of(EntryIngredients.of(recipe.getInput())), Collections.singletonList(EntryIngredients.of(recipe.getOutput())), Optional.ofNullable(recipe.getId()));
    }

    public CompactorRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ID;
    }

}
