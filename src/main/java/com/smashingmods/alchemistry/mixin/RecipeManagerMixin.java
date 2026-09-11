package com.smashingmods.alchemistry.mixin;
import com.smashingmods.alchemistry.datagen.RecipeGenerator;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @ModifyVariable(method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"), argsOnly = true)
    private RecipeMap alchemistry$generate(RecipeMap recipes) { return RecipeGenerator.addGeneratedRecipes(recipes); }
}
