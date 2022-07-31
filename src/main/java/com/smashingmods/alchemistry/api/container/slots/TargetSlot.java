package com.smashingmods.alchemistry.api.container.slots;

import com.smashingmods.alchemistry.common.block.compactor.CompactorBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * Represents a target slot in the Compactor.
 * Does not accept items but uses the item data.
 *
 * @author TechnoVision
 */
public class TargetSlot extends Slot {

    CompactorBlockEntity blockEntity;

    public TargetSlot(Inventory inventory, int index, int x, int y, CompactorBlockEntity blockEntity) {
        super(inventory, index, x, y);
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        blockEntity.setTarget(stack.copy());
        return stack;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }
}
