package com.smashingmods.alchemistry.api.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractFluidBlockEntity extends AbstractInventoryBlockEntity {

    public AbstractFluidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long energyCapacity) {
        super(DefaultedList.ofSize(1), type, pos, state, energyCapacity);
    }
}
