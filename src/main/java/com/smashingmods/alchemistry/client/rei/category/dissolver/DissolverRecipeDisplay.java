package com.smashingmods.alchemistry.client.rei.category.dissolver;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
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

public class DissolverRecipeDisplay extends BasicDisplay {
    public static final me.shedaniel.rei.api.common.display.DisplaySerializer<DissolverRecipeDisplay> SERIALIZER = com.smashingmods.alchemistry.client.rei.ReiDisplayCodecs.create(DissolverRecipeDisplay::new);
    @Override public me.shedaniel.rei.api.common.display.DisplaySerializer<DissolverRecipeDisplay> getSerializer() { return SERIALIZER; }


    public static final CategoryIdentifier<DissolverRecipeDisplay> ID = CategoryIdentifier.of(Alchemistry.MOD_ID, "dissolver");

    public DissolverRecipeDisplay(DissolverRecipe recipe) {
        this(EntryIngredients.ofIngredients(recipe.getIngredients()),
                Collections.singletonList(EntryIngredients.ofItemStacks(recipe.getAllResults())),
                Optional.ofNullable(recipe.getId())
        );
    }

    public DissolverRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ID;
    }

}
