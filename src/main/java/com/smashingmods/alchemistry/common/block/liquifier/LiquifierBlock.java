package com.smashingmods.alchemistry.common.block.liquifier;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.block.AbstractAlchemistryBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LiquifierBlock extends AbstractAlchemistryBlock {

    public static final VoxelShape base = Block.createCuboidShape(0, 0, 0, 16, 1, 16);
    public static final VoxelShape rest = Block.createCuboidShape(2, 1, 2, 14, 16, 14);
    public static final VoxelShape SHAPE = VoxelShapes.union(base, rest);

    public LiquifierBlock() {
        super(LiquifierBlockEntity::new);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        tooltip.add(Text.translatable("tooltip.alchemistry.energy_requirement", Config.Common.liquifierEnergyPerTick.get()).formatted(Formatting.GRAY));
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

        if (blockEntity instanceof LiquifierBlockEntity liquifierBlockEntity) {
            interactionSuccessful = liquifierBlockEntity.onBlockActivated(world, pos, player, hand);
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
                if (blockEntity instanceof LiquifierBlockEntity liquifierBlockEntity) {
                    liquifierBlockEntity.tick();
                }
            };
        }
        return null;
    }
}
