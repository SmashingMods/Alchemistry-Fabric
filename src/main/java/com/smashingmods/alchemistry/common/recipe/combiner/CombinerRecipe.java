package com.smashingmods.alchemistry.common.recipe.combiner;

import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CombinerRecipe extends AbstractAlchemistryRecipe implements Comparable<CombinerRecipe> {

    private final Identifier id;
    private final List<ItemStack> input;
    private final ItemStack output;

    public CombinerRecipe(Identifier id, List<ItemStack> input, ItemStack output) {
        super(id);
        this.id = id;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        return !world.isClient();
    }

    @Override
    public ItemStack craft(SimpleInventory inventory) {
        return output;
    }

    public List<ItemStack> getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(4, Ingredient.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ingredients.set(i, Ingredient.ofStacks(input.get(i)));
        }
        return ingredients;
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", input, output);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CombinerRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public boolean matchInputs(List<ItemStack> pStacks) {
        int matchingStacks = 0;
        List<ItemStack> handlerStacks = new ArrayList<>();
        for (int i = 0; i < pStacks.size()-1; i++) {
            if (!pStacks.get(i).isEmpty()) handlerStacks.add(pStacks.get(i));
        }
        List<ItemStack> recipeStacks = input.stream().filter(itemStack -> !itemStack.isEmpty()).toList();

        if (recipeStacks.size() == handlerStacks.size()) {
            for (ItemStack recipeStack : recipeStacks) {
                for (ItemStack handlerStack : handlerStacks) {
                    if (ItemStack.canCombine(recipeStack, handlerStack) && handlerStack.getCount() >= recipeStack.getCount()) {
                        matchingStacks++;
                        break;
                    }
                }
            }
            return matchingStacks == recipeStacks.size();
        }
        return false;
    }

    @Override
    public int compareTo(@NotNull CombinerRecipe recipe) {
        Objects.requireNonNull(this.output.getItem().getName());
        Objects.requireNonNull(recipe.output.getItem().getName());
        return compareNamespaced(Registry.ITEM.getId(recipe.output.getItem()));
    }

    private int compareNamespaced(Identifier o) {
        Identifier outputID = Registry.ITEM.getId(this.output.getItem());
        int ret = outputID.getNamespace().compareTo(o.getNamespace());
        return ret != 0 ? ret : outputID.getPath().compareTo(o.getPath());
    }

    public static class Type implements RecipeType<CombinerRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "combiner";
    }
}
