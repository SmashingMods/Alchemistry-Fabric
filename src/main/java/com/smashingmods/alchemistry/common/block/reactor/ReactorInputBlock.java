package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.block.AbstractAlchemistryBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ReactorInputBlock extends AbstractAlchemistryBlock {

    public ReactorInputBlock() {
        super(ReactorInputBlockEntity ::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient()) {
            if (world.getBlockEntity(pos) instanceof ReactorInputBlockEntity  blockEntity) {
                // TODO: Implement
                /**
                if (blockEntity.getController() != null) {
                    blockEntity.getController().setInputFound(false);
                }
                 */
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
