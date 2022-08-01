package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ReactorEnergyBlockEntity extends BlockEntity {

    public ReactorEnergyBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.REACTOR_ENERGY_BLOCK_ENTITY, pos, state);
    }
}
