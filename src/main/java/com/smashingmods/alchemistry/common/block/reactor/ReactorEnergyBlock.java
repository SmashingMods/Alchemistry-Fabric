package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.block.AbstractAlchemistryBlock;
import com.smashingmods.alchemistry.api.blockentity.PowerState;
import com.smashingmods.alchemistry.api.blockentity.PowerStateProperty;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ReactorEnergyBlock extends AbstractAlchemistryBlock {

    public ReactorEnergyBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(ReactorEnergyBlockEntity::new, properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PowerStateProperty.POWER_STATE, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(PowerStateProperty.POWER_STATE, PowerState.DISABLED);
    }

}
