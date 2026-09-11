package com.smashingmods.alchemistry.api.blockentity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;

public abstract class AbstractInventoryBlockEntity extends AbstractProcessingBlockEntity implements ImplementedInventory {

    private final NonNullList<ItemStack> inventory;

    public AbstractInventoryBlockEntity(NonNullList<ItemStack> inventory, BlockEntityType<?> type, BlockPos worldPosition, BlockState state, long energyCapacity) {
        super(type, worldPosition, state, energyCapacity);
        this.inventory = inventory;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput nbt) {
        super.saveAdditional(nbt);
        ContainerHelper.saveAllItems(nbt, inventory);
    }

    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput nbt) {
        super.loadAdditional(nbt);
        ContainerHelper.loadAllItems(nbt, inventory);
    }

    @Override
    public void dropContents() {
        Containers.dropContents(level, worldPosition, this);
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.get(slot);
    }

    public ItemStack setStackInSlot(int slot, ItemStack stack) {
        ItemStack previous = inventory.set(slot, stack);
        setChanged();
        return previous;
    }

    public void incrementSlot(int pSlot, int pAmount) {
        ItemStack temp = this.getStackInSlot(pSlot);
        if (temp.getCount() + pAmount <= temp.getMaxStackSize()) {
            temp.setCount(temp.getCount() + pAmount);
        }
        this.setStackInSlot(pSlot, temp);
    }

    public void setOrIncrement(int slot, ItemStack stackToSet) {
        if (!stackToSet.isEmpty()) {
            if (getStackInSlot(slot).isEmpty()) {
                setStackInSlot(slot, stackToSet);
            } else {
                incrementSlot(slot, stackToSet.getCount());
            }
        }
    }

    public void decrementSlot(int slot, int pAmount) {
        ItemStack temp = this.getStackInSlot(slot);
        if (temp.isEmpty()) return;
        if (temp.getCount() - pAmount < 0) return;

        temp.shrink(pAmount);
        if (temp.getCount() <= 0) {
            this.setStackInSlot(slot, ItemStack.EMPTY);
        } else {
            this.setStackInSlot(slot, temp);
        }
    }
}
