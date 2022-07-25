package com.technovision.alchemistry.common.block.dissolver;

import com.technovision.alchemistry.api.blockentity.AbstractInventoryBlockEntity;
import com.technovision.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class DissolverBlockEntity extends AbstractInventoryBlockEntity {

    public DissolverBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.DISSOLVER_BLOCK_ENTITY, pos, state);
    }
}
