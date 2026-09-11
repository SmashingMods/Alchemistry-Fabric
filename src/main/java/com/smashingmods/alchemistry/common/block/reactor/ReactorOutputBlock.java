package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.block.AbstractAlchemistryBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ReactorOutputBlock extends AbstractAlchemistryBlock {

    public ReactorOutputBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(ReactorOutputBlockEntity::new, properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite());
    }

}
