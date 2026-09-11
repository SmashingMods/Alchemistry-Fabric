package com.smashingmods.alchemistry.common.block.fusion;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.block.AbstractAlchemistryBlock;
import com.smashingmods.alchemistry.api.blockentity.PowerState;
import com.smashingmods.alchemistry.api.blockentity.PowerStateProperty;
import com.smashingmods.alchemistry.common.block.fission.FissionControllerBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FusionControllerBlock extends AbstractAlchemistryBlock {

    public FusionControllerBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(FusionControllerBlockEntity::new, properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PowerStateProperty.POWER_STATE, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(PowerStateProperty.POWER_STATE, PowerState.DISABLED);
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag options) {
        super.appendHoverText(stack, context, display, tooltip, options);
        tooltip.accept(Component.translatable("tooltip.alchemistry.energy_requirement", Config.Common.fissionEnergyPerTick.get()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos worldPosition, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (level.getBlockState(worldPosition).getValue(PowerStateProperty.POWER_STATE) != PowerState.DISABLED) {
                MenuProvider screenHandlerFactory = state.getMenuProvider(level, worldPosition);
                if (screenHandlerFactory != null) {
                    player.openMenu(screenHandlerFactory);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return (tickLevel, worldPosition, blockState, blockEntity) -> {
                if (blockEntity instanceof FusionControllerBlockEntity fusionBlockEntity) {
                    fusionBlockEntity.tick();
                }
            };
        }
        return null;
    }
}
