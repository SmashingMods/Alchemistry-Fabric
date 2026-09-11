package com.smashingmods.alchemistry.common.block.combiner;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractInventoryBlockEntity;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CombinerBlockEntity extends AbstractInventoryBlockEntity {

    public static final int INVENTORY_SIZE = 5;
    public static final int OUTPUT_SLOT_INDEX = 4;

    protected final ContainerData propertyDelegate;
    private final int maxProgress;
    private final List<CombinerRecipe> recipes;
    private CombinerRecipe currentRecipe;
    private int selectedRecipe;
    private String editBoxText;
    private boolean recipesSynced;

    public CombinerBlockEntity(BlockPos worldPosition, BlockState state) {
        super(NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.COMBINER_BLOCK_ENTITY, worldPosition, state, Config.Common.combinerEnergyCapacity.get());
        this.maxProgress = Config.Common.combinerTicksPerOperation.get();
        this.recipes = new ArrayList<>();
        this.selectedRecipe = -1;
        this.editBoxText = "";
        this.recipesSynced = false;
        this.propertyDelegate = new ContainerData() {
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
            public int getCount() {
                return 5;
            }
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == OUTPUT_SLOT_INDEX;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot < OUTPUT_SLOT_INDEX;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new CombinerScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }

    @Override
    public void updateRecipe() {
        if (level != null && !level.isClientSide()) {
            if (currentRecipe == null) {
                com.smashingmods.alchemistry.api.recipe.MachineRecipes.all(level, CombinerRecipe.Type.INSTANCE).stream()
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
                    && (currentRecipe.getOutput().copy().getCount() + output.copy().getCount()) <= currentRecipe.getOutput().copy().getMaxStackSize()
                    && (ItemStack.isSameItemSameComponents(output.copy(), currentRecipe.getOutput().copy()) || output.isEmpty())
                    && currentRecipe.matchInputs(getItems());
        }
        return false;
    }

    @Override
    public void processRecipe() {
        if (getProgress() < maxProgress) {
            incrementProgress();
        } else {
            var consumption = currentRecipe.getInputConsumption(getItems());
            if (consumption.isEmpty()) return;
            setProgress(0);
            int[] amounts = consumption.get();
            for (int slot = 0; slot < amounts.length; slot++) {
                if (amounts[slot] > 0) decrementSlot(slot, amounts[slot]);
            }
            setOrIncrement(OUTPUT_SLOT_INDEX, currentRecipe.getOutput().copy());
        }
        extractEnergy(Config.Common.combinerEnergyPerTick.get());
        setChanged();
    }

    @Override
    public <T extends Recipe<RecipeInput>> void setRecipe(@Nullable T recipe) {
        currentRecipe = (CombinerRecipe) recipe;
    }

    @Override
    public Recipe<RecipeInput> getRecipe() {
        return this.currentRecipe;
    }

    public List<CombinerRecipe> getRecipes() {
        return this.recipes;
    }

    public void addRecipe(CombinerRecipe recipe) {
        this.recipes.add(recipe);
    }

    public boolean isRecipesSynced() {
        return recipesSynced;
    }

    public void markRecipesSynced() {
        recipesSynced = true;
    }

    protected String getEditBoxText() {
        return editBoxText;
    }

    protected void setEditBoxText(String text) {
        editBoxText = text;
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput nbt) {
        nbt.putString("editBoxText", editBoxText);
        nbt.putInt("selectedRecipe", selectedRecipe);
        super.saveAdditional(nbt);
    }

    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput nbt) {
        super.loadAdditional(nbt);
        editBoxText = nbt.getStringOr("editBoxText", "");
        selectedRecipe = nbt.getIntOr("selectedRecipe", 0);
    }
}
