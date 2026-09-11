package com.smashingmods.alchemistry.api.block;

import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.ProcessingBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public abstract class AbstractAlchemistryBlock extends BaseEntityBlock implements EntityBlock {

    public void appendHoverText(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag options) { }
    private final BiFunction<BlockPos, BlockState, BlockEntity> blockEntityFunction;
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.core.Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected AbstractAlchemistryBlock(BiFunction<BlockPos, BlockState, BlockEntity> pBlockEntity, BlockBehaviour.Properties properties) {
        super(properties);
        blockEntityFunction = pBlockEntity;
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return com.mojang.serialization.MapCodec.unit(this);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityFunction.apply(pos, state);
    }
}
