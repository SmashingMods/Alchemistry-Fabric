package com.smashingmods.alchemistry.api.recipe;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.*;
public final class MachineRecipes {
    public static <T extends Recipe<?>> List<T> all(Level level, RecipeType<T> type) {
        if (!(level instanceof ServerLevel server)) return List.of();
        return server.getServer().getRecipeManager().getRecipes().stream()
            .filter(holder -> holder.value().getType() == type)
            .map(holder -> { @SuppressWarnings("unchecked") T value = (T) holder.value(); return value; }).toList();
    }
}
