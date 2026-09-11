package com.smashingmods.alchemistry.client.rei;
import com.smashingmods.alchemistry.client.rei.category.fusion.FusionRecipeDisplay;
import com.smashingmods.alchemistry.common.recipe.fusion.FusionRecipe;
import com.smashingmods.alchemistry.client.rei.category.fission.FissionRecipeDisplay;
import com.smashingmods.alchemistry.common.recipe.fission.FissionRecipe;
import com.smashingmods.alchemistry.client.rei.category.combiner.CombinerRecipeDisplay;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.client.rei.category.liquifier.LiquifierRecipeDisplay;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
import com.smashingmods.alchemistry.client.rei.category.compactor.CompactorRecipeDisplay;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
import com.smashingmods.alchemistry.client.rei.category.dissolver.DissolverRecipeDisplay;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
import com.smashingmods.alchemistry.client.rei.category.atomizer.AtomizerRecipeDisplay;
import com.smashingmods.alchemistry.common.recipe.atomizer.AtomizerRecipe;
public class ReiCommonPlugin implements me.shedaniel.rei.api.common.plugins.REICommonPlugin {
    @Override public void registerDisplays(me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry registry) {
        registry.<FusionRecipe, FusionRecipeDisplay>beginRecipeFiller(FusionRecipe.class).fill(holder -> new FusionRecipeDisplay(holder.value()));
        registry.<FissionRecipe, FissionRecipeDisplay>beginRecipeFiller(FissionRecipe.class).fill(holder -> new FissionRecipeDisplay(holder.value()));
        registry.<CombinerRecipe, CombinerRecipeDisplay>beginRecipeFiller(CombinerRecipe.class).fill(holder -> new CombinerRecipeDisplay(holder.value()));
        registry.<LiquifierRecipe, LiquifierRecipeDisplay>beginRecipeFiller(LiquifierRecipe.class).fill(holder -> new LiquifierRecipeDisplay(holder.value()));
        registry.<CompactorRecipe, CompactorRecipeDisplay>beginRecipeFiller(CompactorRecipe.class).fill(holder -> new CompactorRecipeDisplay(holder.value()));
        registry.<DissolverRecipe, DissolverRecipeDisplay>beginRecipeFiller(DissolverRecipe.class).fill(holder -> new DissolverRecipeDisplay(holder.value()));
        registry.<AtomizerRecipe, AtomizerRecipeDisplay>beginRecipeFiller(AtomizerRecipe.class).fill(holder -> new AtomizerRecipeDisplay(holder.value()));
    }
    @Override public void registerDisplaySerializer(me.shedaniel.rei.api.common.display.DisplaySerializerRegistry registry) {
        registry.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("alchemistry", "fusion"), FusionRecipeDisplay.SERIALIZER);
        registry.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("alchemistry", "fission"), FissionRecipeDisplay.SERIALIZER);
        registry.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("alchemistry", "combiner"), CombinerRecipeDisplay.SERIALIZER);
        registry.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("alchemistry", "liquifier"), LiquifierRecipeDisplay.SERIALIZER);
        registry.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("alchemistry", "compactor"), CompactorRecipeDisplay.SERIALIZER);
        registry.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("alchemistry", "dissolver"), DissolverRecipeDisplay.SERIALIZER);
        registry.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("alchemistry", "atomizer"), AtomizerRecipeDisplay.SERIALIZER);
    }
}
