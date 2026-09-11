package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.blockentity.PowerState;
import com.smashingmods.alchemistry.api.blockentity.PowerStateProperty;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import java.util.Objects;

public class ReactorCoreBlock extends RotatedPillarBlock {

    public ReactorCoreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getValue(PowerStateProperty.POWER_STATE).equals(PowerState.ON)) {
            return 15;
        }
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PowerStateProperty.POWER_STATE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return Objects.requireNonNull(super.getStateForPlacement(ctx)).setValue(PowerStateProperty.POWER_STATE, PowerState.OFF);
    }
}
