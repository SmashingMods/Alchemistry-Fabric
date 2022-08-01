package com.smashingmods.alchemistry.common.block.combiner;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractInventoryBlockEntity;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CombinerBlockEntity extends AbstractInventoryBlockEntity {

    public static final int INVENTORY_SIZE = 5;
    public static final int OUTPUT_SLOT_INDEX = 4;

    protected final PropertyDelegate propertyDelegate;
    private final int maxProgress;
    private List<CombinerRecipe> recipes;
    private CombinerRecipe currentRecipe;
    private int selectedRecipe;
    private String editBoxText;


    public CombinerBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.COMBINER_BLOCK_ENTITY, pos, state, Config.Common.combinerEnergyCapacity.get());
        this.maxProgress = Config.Common.combinerTicksPerOperation.get();
        this.recipes = new ArrayList<>();
        this.selectedRecipe = -1;
        this.editBoxText = "";
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> getProgress();
                    case 1 -> maxProgress;
                    case 2 -> (int) getEnergyStorage().getAmount();
                    case 3 -> (int) getEnergyStorage().getCapacity();
                    case 4 -> selectedRecipe;
                    default -> 0;
                };
            }
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> setProgress(value);
                    case 2 -> insertEnergy(value);
                    case 4 -> selectedRecipe = value;
                }
            }
            public int size() {
                return 5;
            }
        };
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new CombinerScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }

    @Override
    public void updateRecipe() {
        if (world != null && !world.isClient()) {
            if (currentRecipe == null) {
                world.getRecipeManager().getAllMatches(CombinerRecipe.Type.INSTANCE, new SimpleInventory(1), world).stream()
                        .filter(recipe -> recipe.matchInputs(getItems()))
                        .findFirst()
                        .ifPresent(recipe ->  {
                            if (currentRecipe == null || !currentRecipe.equals(recipe)) {
                                setProgress(0);
                                setRecipe(recipe);
                            }
                        });
            }
        }
    }

    @Override
    public boolean canProcessRecipe() {
        if (currentRecipe != null) {
            ItemStack output = getStackInSlot(OUTPUT_SLOT_INDEX);
            return getEnergyStorage().getAmount() >= Config.Common.combinerEnergyPerTick.get()
                    && (currentRecipe.getOutput().copy().getCount() + output.copy().getCount()) <= currentRecipe.getOutput().copy().getMaxCount()
                    && (ItemStack.canCombine(output.copy(), currentRecipe.getOutput().copy()) || output.isEmpty())
                    && currentRecipe.matchInputs(getItems());
        }
        return false;
    }

    @Override
    public void processRecipe() {
        if (getProgress() < maxProgress) {
            incrementProgress();
        } else {
            setProgress(0);
            setOrIncrement(OUTPUT_SLOT_INDEX, currentRecipe.getOutput().copy());
            for (int i = 0; i < currentRecipe.getInput().size(); i++) {
                for (int j = 0; j < 4; j++) {
                    if (ItemStack.canCombine(currentRecipe.getInput().get(i), getItems().get(j))) {
                        decrementSlot(j, currentRecipe.getInput().get(i).getCount());
                        break;
                    }
                }
            }
        }
        extractEnergy(Config.Common.combinerEnergyPerTick.get());
        markDirty();
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T recipe) {
        currentRecipe = (CombinerRecipe) recipe;
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        return this.currentRecipe;
    }

    public List<CombinerRecipe> getRecipes() {
        return this.recipes;
    }

    public void setRecipes(List<CombinerRecipe> recipes) {
        this.recipes = recipes;
    }

    protected String getEditBoxText() {
        return editBoxText;
    }

    protected void setEditBoxText(String text) {
        editBoxText = text;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putString("editBoxText", editBoxText);
        nbt.putInt("selectedRecipe", selectedRecipe);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        editBoxText = nbt.getString("editBoxText");
        selectedRecipe = nbt.getInt("selectedRecipe");
    }
}
