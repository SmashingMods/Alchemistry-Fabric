package com.smashingmods.alchemistry.common.block.compactor;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.block.AbstractAlchemistryBlock;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverBlockEntity;
import net.minecraft.world.level.block.Block;
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

public class CompactorBlock extends AbstractAlchemistryBlock {

    public static final VoxelShape base = Block.box(0, 0, 0, 16, 1, 16);
    public static final VoxelShape rest = Block.box(2, 1, 2, 14, 16, 14);
    public static final VoxelShape SHAPE = Shapes.or(base, rest);

    public CompactorBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(CompactorBlockEntity::new, properties);
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag options) {
        tooltip.accept(Component.translatable("tooltip.alchemistry.energy_requirement", Config.Common.compactorEnergyPerTick.get()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos worldPosition, CollisionContext context) {
        return SHAPE;
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
                if (blockEntity instanceof CompactorBlockEntity compactorBlockEntity) {
                    compactorBlockEntity.tick();
                }
            };
        }
        return null;
    }
}
