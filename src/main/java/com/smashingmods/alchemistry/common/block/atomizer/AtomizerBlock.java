package com.smashingmods.alchemistry.common.block.atomizer;

import com.smashingmods.alchemistry.api.block.AbstractAlchemistryBlock;
import com.smashingmods.alchemistry.common.block.liquifier.LiquifierBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AtomizerBlock extends AbstractAlchemistryBlock {

    public static final VoxelShape base = Block.createCuboidShape(0, 0, 0, 16, 1, 16);
    public static final VoxelShape rest = Block.createCuboidShape(2, 1, 2, 14, 16, 14);
    public static final VoxelShape SHAPE = VoxelShapes.union(base, rest);

    public AtomizerBlock() {
        super(AtomizerBlockEntity::new);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        boolean interactionSuccessful = true;

        if (blockEntity instanceof AtomizerBlockEntity atomizerBlockEntity) {
            interactionSuccessful = atomizerBlockEntity.onBlockActivated(world, pos, player, hand);
        }

        if (!world.isClient() && !interactionSuccessful) {
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient()) {
            return (level, pos, blockState, blockEntity) -> {
                if (blockEntity instanceof AtomizerBlockEntity atomizerBlockEntity) {
                    atomizerBlockEntity.tick();
                }
            };
        }
        return null;
    }
}
