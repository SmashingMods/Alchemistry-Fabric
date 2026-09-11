package com.smashingmods.alchemistry.client.rei.category.liquifier;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
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

public class LiquifierRecipeDisplay extends BasicDisplay {
    public static final me.shedaniel.rei.api.common.display.DisplaySerializer<LiquifierRecipeDisplay> SERIALIZER = com.smashingmods.alchemistry.client.rei.ReiDisplayCodecs.create(LiquifierRecipeDisplay::new);
    @Override public me.shedaniel.rei.api.common.display.DisplaySerializer<LiquifierRecipeDisplay> getSerializer() { return SERIALIZER; }


    public static final CategoryIdentifier<LiquifierRecipeDisplay> ID = CategoryIdentifier.of(Alchemistry.MOD_ID, "liquifier");

    public LiquifierRecipeDisplay(LiquifierRecipe recipe) {
        this(List.of(EntryIngredients.ofItemStacks(recipe.getInput().items().map(item -> new net.minecraft.world.item.ItemStack(item, recipe.getInputAmount())).toList())), Collections.singletonList(EntryIngredients.of(recipe.getFluidOutput().getFluid(), recipe.getFluidAmount() * 81L)), Optional.ofNullable(recipe.getId()));
    }

    public LiquifierRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ID;
    }

}
