package com.smashingmods.alchemistry.common.block.compactor;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractInventoryBlockEntity;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
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
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CompactorBlockEntity extends AbstractInventoryBlockEntity {

    public static final int INVENTORY_SIZE = 3;
    public static final int INPUT_SLOT_INDEX = 0;
    public static final int TARGET_SLOT_INDEX = 1;
    public static final int OUTPUT_SLOT_INDEX = 2;

    private CompactorRecipe currentRecipe;
    protected final PropertyDelegate propertyDelegate;
    private final int maxProgress;
    private ItemStack target;

    public CompactorBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.COMPACTOR_BLOCK_ENTITY, pos, state, Config.Common.compactorEnergyCapacity.get());
        this.target = ItemStack.EMPTY;
        this.maxProgress = Config.Common.compactorTicksPerOperation.get();
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> getProgress();
                    case 1 -> maxProgress;
                    case 2 -> (int) getEnergyStorage().getAmount();
                    case 3 -> (int) getEnergyStorage().getCapacity();
                    default -> 0;
                };
            }
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> setProgress(value);
                    case 2 -> insertEnergy(value);
                }
            }
            public int size() {
                return 4;
            }
        };
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return slot == OUTPUT_SLOT_INDEX;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        if (isRecipeLocked() && currentRecipe != null) {
            // there is only one input slot in recipe no matter what
            boolean validInsert = currentRecipe.getIngredients().get(0).test(stack);
            return validInsert && slot == INPUT_SLOT_INDEX;
        }
        return slot == INPUT_SLOT_INDEX;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new CompactorScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }

    @Override
    public void updateRecipe() {
        if (world == null || world.isClient() || isRecipeLocked()) return;
        if (!getStackInSlot(INPUT_SLOT_INDEX).isEmpty()) {
            if (target.isEmpty()) {
                // Find recipe without target
                List<CompactorRecipe> recipes = world.getRecipeManager().getAllMatches(CompactorRecipe.Type.INSTANCE, new SimpleInventory(1), world).stream()
                        .filter(recipe -> ItemStack.canCombine(getStackInSlot(0), recipe.getInput()))
                        .toList();
                if (recipes.size() == 1) {
                    if (currentRecipe == null || !currentRecipe.equals(recipes.get(0))) {
                        setProgress(0);
                        currentRecipe = recipes.get(0);
                        setTarget(recipes.get(0).getOutput().copy());
                    }
                } else {
                    // Several recipes found, do nothing
                    setProgress(0);
                    setRecipe(null);
                }
            } else {
                // Find recipe with target
                world.getRecipeManager().getAllMatches(CompactorRecipe.Type.INSTANCE, new SimpleInventory(1), world).stream()
                        .filter(recipe -> ItemStack.canCombine(target, recipe.getOutput()))
                        .findFirst()
                        .ifPresent(recipe -> {
                            if (currentRecipe == null || !currentRecipe.equals(recipe)) {
                                setProgress(0);
                                currentRecipe = recipe;
                            }
                        });
            }
        }
    }

    @Override
    public boolean canProcessRecipe() {
        if (currentRecipe != null) {
            ItemStack input = getStackInSlot(INPUT_SLOT_INDEX);
            ItemStack output = getStackInSlot(OUTPUT_SLOT_INDEX);
            return getEnergyStorage().getAmount() >= Config.Common.compactorEnergyPerTick.get()
                    && (ItemStack.canCombine(input, currentRecipe.getInput()) && input.getCount() >= currentRecipe.getInput().getCount())
                    && (currentRecipe.getOutput().getCount() + output.getCount()) <= currentRecipe.getOutput().getMaxCount()
                    && (ItemStack.canCombine(output, currentRecipe.getOutput()) || output.isEmpty());
        }
        return false;
    }

    @Override
    public void processRecipe() {
        if (getProgress() < maxProgress) {
            incrementProgress();
        } else {
            setProgress(0);
            decrementSlot(INPUT_SLOT_INDEX, currentRecipe.getInput().getCount());
            setOrIncrement(OUTPUT_SLOT_INDEX, currentRecipe.getOutput().copy());
        }
        extractEnergy(Config.Common.compactorEnergyPerTick.get());
        markDirty();
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T recipe) {
        if (recipe == null) {
            currentRecipe = null;
        } else {
            currentRecipe = (CompactorRecipe) recipe;
        }
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        return currentRecipe;
    }

    public ItemStack getTarget() {
        return target;
    }

    public void setTarget(ItemStack targetStack) {
        if (world != null && !world.isClient() && !isRecipeLocked()) {
            if (targetStack == ItemStack.EMPTY || isTargetValid(targetStack)) {
                this.target = targetStack;
                forceSync();
            }
        }
    }

    private boolean isTargetValid(ItemStack itemStack) {
        if (world != null && !world.isClient()) {
            Optional<CompactorRecipe> match = world.getRecipeManager().getAllMatches(CompactorRecipe.Type.INSTANCE, new SimpleInventory(1), world).stream()
                    .filter(recipe -> ItemStack.canCombine(recipe.getOutput().copy(), itemStack.copy()))
                    .findFirst();
            return match.isPresent();
        }
        return false;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("target", target.writeNbt(new NbtCompound()));
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("target")) {
            target = ItemStack.fromNbt(nbt.getCompound("target"));
        }
    }
}
