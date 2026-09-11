package com.smashingmods.alchemistry.api.container.slots;

import com.smashingmods.alchemistry.common.block.compactor.CompactorBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

/**
 * Represents a target slot in the Compactor.
 * Does not accept items but uses the item data.
 *
 * @author TechnoVision
 */
public class TargetSlot extends Slot {

    CompactorBlockEntity blockEntity;

    public TargetSlot(Container inventory, int index, int x, int y, CompactorBlockEntity blockEntity) {
        super(inventory, index, x, y);
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int count) {
        blockEntity.setTarget(stack.copy());
        return stack;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
