package com.smashingmods.alchemistry.common.block.dissolver;

import net.minecraft.world.item.crafting.RecipeInput;
import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractInventoryBlockEntity;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
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

import java.util.Optional;

public class DissolverBlockEntity extends AbstractInventoryBlockEntity {

    public static final int INVENTORY_SIZE = 11;

    private DissolverRecipe currentRecipe;
    protected final ContainerData propertyDelegate;
    private final int maxProgress;
    private final NonNullList<ItemStack> internalBuffer;

    public DissolverBlockEntity(BlockPos worldPosition, BlockState state) {
        super(NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.DISSOLVER_BLOCK_ENTITY, worldPosition, state, Config.Common.dissolverEnergyCapacity.get());
        this.internalBuffer = NonNullList.createWithCapacity(64);
        this.maxProgress = Config.Common.dissolverTicksPerOperation.get();
        this.propertyDelegate = new ContainerData() {
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
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot != 0;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot == 0;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new DissolverScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }

    @Override
    public void tick() {
        if (level != null && !level.isClientSide()) {
            refreshRecipe();
            if (!isProcessingPaused()) {
                if (!isRecipeLocked()) {
                    updateRecipe();
                }
                if (canProcessRecipe()) {
                    processRecipe();
                }
                processBuffer();
            }
        }
    }

    @Override
    public void updateRecipe() {
        if (level == null || level.isClientSide()) return;
        SimpleContainer inventory = new SimpleContainer(getItems().size());
        for (int i = 0; i < getItems().size(); i++) {
            inventory.setItem(i, getItem(i));
        }
        Optional<DissolverRecipe> match = com.smashingmods.alchemistry.api.recipe.MachineRecipes.all(level, DissolverRecipe.Type.INSTANCE).stream().filter(recipe -> recipe.getInput().test(getItem(0))).findFirst();
        if (match.isPresent()) {
            if (currentRecipe == null || !currentRecipe.equals(match.get())) {
                setProgress(0);
                currentRecipe = match.get();
            }
        }
    }

    @Override
    public boolean canProcessRecipe() {
        if (currentRecipe != null) {
            ItemStack input = getStackInSlot(0).copy();
            SimpleContainer inputInventory = new SimpleContainer(1);
            inputInventory.addItem(input);
            return getEnergyStorage().getAmount() >= Config.Common.dissolverEnergyPerTick.get()
                    && currentRecipe.getInput().test(input)
                    && !currentRecipe.getInput().isEmpty()
                    && (input.getCount() >= 1)
                    && internalBuffer.isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public void processRecipe() {
        if (getProgress() < maxProgress) {
            incrementProgress();
        } else {
            setProgress(0);
            decrementSlot(0, 1);
            internalBuffer.addAll(currentRecipe.getProbabilityOutput().calculateOutput());
        }
        extractEnergy(Config.Common.dissolverEnergyPerTick.get());
        setChanged();
    }

    private void processBuffer() {
        for (int i = 0; i < internalBuffer.size(); i++) {
            ItemStack bufferStack = internalBuffer.get(i).copy();
            for (int j = 1; j < getItems().size(); j++) {
                ItemStack slotStack = getStackInSlot(j).copy();
                if (slotStack.isEmpty() || (ItemStack.isSameItemSameComponents(bufferStack, slotStack) && bufferStack.getCount() + slotStack.getCount() <= slotStack.getMaxStackSize())) {
                    setOrIncrement(j, bufferStack);
                    internalBuffer.remove(i);
                    break;
                }
            }
        }
        setChanged();
    }

    @Override
    public <T extends Recipe<RecipeInput>> void setRecipe(@Nullable T pRecipe) {
        currentRecipe = (DissolverRecipe) pRecipe;
    }

    @Override
    public Recipe<RecipeInput> getRecipe() {
        return currentRecipe;
    }

    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        output.store("internalBuffer", ItemStack.CODEC.listOf(), internalBuffer);
    }
    @Override public void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        internalBuffer.clear();
        internalBuffer.addAll(input.read("internalBuffer", ItemStack.CODEC.listOf()).orElse(java.util.List.of()));
    }
    @Override public void dropContents() {
        super.dropContents();
        if (level != null) for (ItemStack stack : internalBuffer) net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
        internalBuffer.clear();
    }
}
