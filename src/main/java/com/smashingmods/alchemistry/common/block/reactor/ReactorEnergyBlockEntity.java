package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class ReactorEnergyBlockEntity extends BlockEntity {

    @Nullable
    private AbstractReactorBlockEntity controller;

    public ReactorEnergyBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.REACTOR_ENERGY_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    public AbstractReactorBlockEntity getController() {
        return controller;
    }

    public void setController(@Nullable AbstractReactorBlockEntity controller) {
        this.controller = controller;
        //noinspection ConstantConditions
        //this.lazyEnergyHandler = LazyOptional.of(() -> controller.getEnergyHandler());
    }
}
