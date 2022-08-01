package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.blockentity.PowerState;
import com.smashingmods.alchemistry.api.blockentity.PowerStateProperty;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.Objects;

public class ReactorCoreBlock extends PillarBlock {

    public ReactorCoreBlock() {
        super(FabricBlockSettings.of(Material.METAL).strength(2.0f));
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        if (state.get(PowerStateProperty.POWER_STATE).equals(PowerState.ON)) {
            return 15;
        }
        return 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PowerStateProperty.POWER_STATE);
        super.appendProperties(builder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return Objects.requireNonNull(super.getPlacementState(ctx)).with(PowerStateProperty.POWER_STATE, PowerState.OFF);
    }
}
