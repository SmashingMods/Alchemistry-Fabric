package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.block.AbstractAlchemistryBlock;
import com.smashingmods.alchemistry.api.blockentity.PowerState;
import com.smashingmods.alchemistry.api.blockentity.PowerStateProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ReactorEnergyBlock extends AbstractAlchemistryBlock {

    public ReactorEnergyBlock() {
        super(ReactorEnergyBlockEntity::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PowerStateProperty.POWER_STATE, Properties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite())
                .with(PowerStateProperty.POWER_STATE, PowerState.DISABLED);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient()) {
            if (world.getBlockEntity(pos) instanceof ReactorEnergyBlockEntity blockEntity) {
                // TODO: Implement
                /**
                if (blockEntity.getController() != null) {
                    blockEntity.getController().setEnergyFound(false);
                }
                 */
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
