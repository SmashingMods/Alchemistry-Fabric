package com.smashingmods.alchemistry.client.rei.category.fission;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.recipe.fission.FissionRecipe;
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
public class FissionRecipeDisplay extends BasicDisplay {

    public static final CategoryIdentifier<FissionRecipeDisplay> ID = CategoryIdentifier.of(Alchemistry.MOD_ID, "fission_controller");

    public FissionRecipeDisplay(FissionRecipe recipe) {
        this(EntryIngredients.ofIngredients(recipe.getIngredients()), List.of(EntryIngredients.of(recipe.getOutput1()), EntryIngredients.of(recipe.getOutput2())), Optional.ofNullable(recipe.getId()));
    }

    public FissionRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ID;
    }

}
