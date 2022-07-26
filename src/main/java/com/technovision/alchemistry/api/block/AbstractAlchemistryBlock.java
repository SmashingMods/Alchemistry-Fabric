package com.technovision.alchemistry.api.block;

import com.technovision.alchemistry.api.blockentity.ProcessingBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public abstract class AbstractAlchemistryBlock extends BlockWithEntity implements BlockEntityProvider {

    private final BiFunction<BlockPos, BlockState, BlockEntity> blockEntityFunction;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    protected AbstractAlchemistryBlock(BiFunction<BlockPos, BlockState, BlockEntity> pBlockEntity) {
        super(FabricBlockSettings.of(Material.METAL).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL));
        blockEntityFunction = pBlockEntity;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ProcessingBlockEntity processingBlockEntity) {
                processingBlockEntity.dropContents();
                world.updateComparators(pos, this);
            }
            // TODO: Uncomment this code once ProcessingBlockEntity and AbstractReactorBlockEntity are implemented
            /**
            if (blockEntity instanceof AbstractReactorBlockEntity reactorBlockEntity) {
                reactorBlockEntity.onRemove();
                world.updateComparators(pos, this);
            }
             */
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityFunction.apply(pos, state);
    }
}
