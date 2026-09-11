package com.smashingmods.alchemistry.common.block.dissolver;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.block.AbstractAlchemistryBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DissolverBlock extends AbstractAlchemistryBlock {

    public static final VoxelShape A = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
    public static final VoxelShape B = Block.box(2.0, 4.0, 2.0, 14, 14.0, 14);
    public static final VoxelShape SHAPE = Shapes.or(A,B);

    public DissolverBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(DissolverBlockEntity::new, properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos worldPosition, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag options) {
        tooltip.accept(Component.translatable("tooltip.alchemistry.energy_requirement", Config.Common.dissolverEnergyPerTick.get()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos worldPosition, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            MenuProvider screenHandlerFactory = state.getMenuProvider(level, worldPosition);
            if (screenHandlerFactory != null) {
                player.openMenu(screenHandlerFactory);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return (tickLevel, worldPosition, blockState, blockEntity) -> {
                if (blockEntity instanceof DissolverBlockEntity dissolverBlockEntity) {
                    dissolverBlockEntity.tick();
                }
            };
        }
        return null;
    }
}
