package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.ImplementedInventory;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

public class ReactorInputBlockEntity extends BlockEntity implements ImplementedInventory {

    @Nullable
    private AbstractReactorBlockEntity controller;
    private final DefaultedList<ItemStack> tempInv;

    public ReactorInputBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.REACTOR_INPUT_BLOCK_ENTITY, pos, state);
        tempInv = DefaultedList.ofSize(1, ItemStack.EMPTY);
    }

    @Nullable
    public AbstractReactorBlockEntity getController() {
        return controller;
    }

    public void setController(@Nullable AbstractReactorBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @org.jetbrains.annotations.Nullable Direction side) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return (controller != null) ? controller.getItems() : tempInv;
    }
}
