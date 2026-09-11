package com.smashingmods.alchemistry.client.rei.category.fusion;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.recipe.fusion.FusionRecipe;
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

public class FusionRecipeDisplay extends BasicDisplay {
    public static final me.shedaniel.rei.api.common.display.DisplaySerializer<FusionRecipeDisplay> SERIALIZER = com.smashingmods.alchemistry.client.rei.ReiDisplayCodecs.create(FusionRecipeDisplay::new);
    @Override public me.shedaniel.rei.api.common.display.DisplaySerializer<FusionRecipeDisplay> getSerializer() { return SERIALIZER; }


    public static final CategoryIdentifier<FusionRecipeDisplay> ID = CategoryIdentifier.of(Alchemistry.MOD_ID, "fusion_controller");

    public FusionRecipeDisplay(FusionRecipe recipe) {
        this(List.of(EntryIngredients.of(recipe.getInput1()), EntryIngredients.of(recipe.getInput2())), Collections.singletonList(EntryIngredients.of(recipe.getOutput())), Optional.ofNullable(recipe.getId()));
    }

    public FusionRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ID;
    }

}
