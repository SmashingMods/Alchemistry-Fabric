package com.smashingmods.alchemistry.mixin;

import com.google.gson.JsonElement;
import com.smashingmods.alchemistry.datagen.RecipeGenerator;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Registers recipes for recipe datagen.
 * See RecipeGenerator for details.
 *
 * @author TechnoVision
 */
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "apply", at = @At("HEAD"))
    public void interceptApply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
        RecipeGenerator.generateRecipes();
        map.putAll(RecipeGenerator.RECIPES);
    }
}
