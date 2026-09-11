package com.smashingmods.alchemistry.api.recipe;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
/** Recipe quantities are data until item components are bound after resource loading. */
public record RecipeStack(Holder<Item> item, int count) {
    public static RecipeStack of(Item item, int count) { return new RecipeStack(item.builtInRegistryHolder(), count); }
    public static RecipeStack of(ItemStack stack) { return new RecipeStack(stack.typeHolder(), stack.getCount()); }
    public ItemStack create() { return new ItemStack(item, count); }
}
