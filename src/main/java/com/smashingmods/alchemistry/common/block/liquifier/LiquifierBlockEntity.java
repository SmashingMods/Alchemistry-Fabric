package com.smashingmods.alchemistry.common.block.liquifier;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractFluidBlockEntity;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverScreenHandler;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class LiquifierBlockEntity extends AbstractFluidBlockEntity {

    public static final int INVENTORY_SIZE = 1;

    private final int maxProgress;

    public LiquifierBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.LIQUIFIER_BLOCK_ENTITY, pos, state, Config.Common.liquifierEnergyCapacity.get());
        this.maxProgress = Config.Common.liquifierTicksPerOperation.get();
    }

    @Override
    public void updateRecipe() {
        // TODO: Implement
    }

    @Override
    public boolean canProcessRecipe() {
        // TODO: Implement
        return false;
    }

    @Override
    public void processRecipe() {
        // TODO: Implement
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T pRecipe) {
        // TODO: Implement
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        // TODO: Implement
        return null;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        // TODO: Replace null at the new with property delegate
        return new LiquifierScreenHandler(syncId, inv, this, this, null);
    }
}
