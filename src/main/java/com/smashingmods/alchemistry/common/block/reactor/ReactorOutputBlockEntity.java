package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class ReactorOutputBlockEntity extends BlockEntity {

    @Nullable
    private AbstractReactorBlockEntity controller;

    public ReactorOutputBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.REACTOR_OUTPUT_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    public AbstractReactorBlockEntity getController() {
        return controller;
    }

    public void setController(@Nullable AbstractReactorBlockEntity controller) {
        this.controller = controller;
        //noinspection ConstantConditions
        //this.lazyOutputHandler = LazyOptional.of(() -> controller.getOutputHandler());
    }
}
