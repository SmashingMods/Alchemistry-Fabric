package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.ImplementedInventory;
import com.smashingmods.alchemistry.api.blockentity.ReactorType;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

public class ReactorOutputBlockEntity extends BlockEntity implements ImplementedInventory {

    @Nullable
    private AbstractReactorBlockEntity controller;
    private final NonNullList<ItemStack> tempInv;
    private final ReactorItemStorage itemStorage = new ReactorItemStorage(this, this::getController, false);

    public ReactorItemStorage getItemStorage() { return itemStorage; }

    public ReactorOutputBlockEntity(BlockPos worldPosition, BlockState state) {
        super(BlockEntityRegistry.REACTOR_OUTPUT_BLOCK_ENTITY, worldPosition, state);
        // Both reactor controllers expose three slots; cached automation views must remain valid when detached.
        tempInv = NonNullList.withSize(3, ItemStack.EMPTY);
    }

    @Nullable
    public AbstractReactorBlockEntity getController() {
        return controller;
    }

    public void setController(@Nullable AbstractReactorBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // The controller owns these items. Vanilla must not drop its inventory when a port is removed.
        setController(null);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @org.jetbrains.annotations.Nullable Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (controller != null) {
            if (controller.getReactorType() == ReactorType.FISSION) {
                return slot > 0;
            } else if (controller.getReactorType() == ReactorType.FUSION) {
                return slot == 2;
            }
        }
        return false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (controller != null) controller.setChanged();
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return (controller != null) ? controller.getItems() : tempInv;
    }
}
