package com.smashingmods.alchemistry.api.container.slots;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

public class OutputSlot extends Slot {

    public OutputSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
