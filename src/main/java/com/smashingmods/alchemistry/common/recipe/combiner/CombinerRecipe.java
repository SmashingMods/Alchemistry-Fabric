package com.smashingmods.alchemistry.common.recipe.combiner;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import com.smashingmods.alchemistry.api.recipe.RecipeStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CombinerRecipe extends AbstractAlchemistryRecipe implements Comparable<CombinerRecipe> {

    private final List<RecipeStack> input;
    private final RecipeStack output;

    public CombinerRecipe(Identifier id, List<RecipeStack> input, RecipeStack output) {
        super(id);
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(RecipeInput inventory, Level level) {
        return !level.isClientSide();
    }

    @Override
    public ItemStack assemble(RecipeInput inventory) {
        return output.create();
    }

    public List<RecipeStack> getInputData() { return input; }

    public List<ItemStack> getInput() {
        return input.stream().map(RecipeStack::create).toList();
    }

    public RecipeStack getOutputData() { return output; }

    public ItemStack getOutput() {
        return output.create();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (int i = 0; i < input.size(); i++) {
            ingredients.add(Ingredient.of(input.get(i).item().value()));
        }
        return ingredients;
    }

    @Override
    public String toString(){
        return String.format("input=%s, outputs=%s", input, output);
    }


    @Override
    public RecipeSerializer<CombinerRecipe> getSerializer() {
        return CombinerRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<CombinerRecipe> getType() {
        return Type.INSTANCE;
    }

    public boolean matchInputs(List<ItemStack> pStacks) {
        return getInputConsumption(pStacks).isPresent();
    }

    /** Assign each ingredient to a distinct input slot; the last slot is output. */
    public Optional<int[]> getInputConsumption(List<ItemStack> stacks) {
        if (stacks.isEmpty()) return Optional.empty();
        int[] consumption = new int[stacks.size() - 1];
        List<ItemStack> ingredients = getInput().stream().filter(stack -> !stack.isEmpty())
                // Reserve larger stacks first when duplicate ingredients require different counts.
                .sorted(Comparator.comparingInt(ItemStack::getCount).reversed()).toList();
        int occupied = 0;
        for (int slot = 0; slot < consumption.length; slot++) {
            if (!stacks.get(slot).isEmpty()) occupied++;
        }
        if (occupied != ingredients.size()) return Optional.empty();
        for (ItemStack ingredient : ingredients) {
            boolean matched = false;
            for (int slot = 0; slot < consumption.length; slot++) {
                ItemStack stack = stacks.get(slot);
                if (consumption[slot] == 0 && ItemStack.isSameItemSameComponents(ingredient, stack)
                        && stack.getCount() >= ingredient.getCount()) {
                    consumption[slot] = ingredient.getCount();
                    matched = true;
                    break;
                }
            }
            if (!matched) return Optional.empty();
        }
        return Optional.of(consumption);
    }

    @Override
    public int compareTo(@NotNull CombinerRecipe recipe) {
        return compareNamespaced(BuiltInRegistries.ITEM.getKey(recipe.output.item().value()));
    }

    private int compareNamespaced(Identifier o) {
        Identifier outputID = BuiltInRegistries.ITEM.getKey(this.output.item().value());
        int ret = outputID.getNamespace().compareTo(o.getNamespace());
        return ret != 0 ? ret : outputID.getPath().compareTo(o.getPath());
    }

    public static class Type implements RecipeType<CombinerRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "combiner";
    }
}
