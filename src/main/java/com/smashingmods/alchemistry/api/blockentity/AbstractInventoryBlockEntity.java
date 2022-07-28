package com.smashingmods.alchemistry.api.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractInventoryBlockEntity extends AbstractProcessingBlockEntity implements ImplementedInventory {

    private final DefaultedList<ItemStack> inventory;

    public AbstractInventoryBlockEntity(DefaultedList<ItemStack> inventory, BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = inventory;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
    }

    @Override
    public void dropContents() {
        ItemScatterer.spawn(world, pos, this);
    }
}
