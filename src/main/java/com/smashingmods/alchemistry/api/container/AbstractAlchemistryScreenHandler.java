package com.smashingmods.alchemistry.api.container;

import com.mojang.datafixers.util.Function4;
import com.smashingmods.alchemistry.api.blockentity.AbstractProcessingBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public abstract class AbstractAlchemistryScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final int slotCount;
    private final World world;
    private final AbstractProcessingBlockEntity blockEntity;

    public AbstractAlchemistryScreenHandler(ScreenHandlerType<?> screenHandlerType, int syncId, PlayerInventory playerInventory, BlockEntity blockEntity, Inventory inventory, int slotCount) {
        super(screenHandlerType, syncId);
        this.inventory = inventory;
        this.slotCount = slotCount;
        this.world = playerInventory.player.getWorld();
        this.blockEntity = (AbstractProcessingBlockEntity) blockEntity;
        addPlayerInventorySlots(playerInventory);
        inventory.onOpen(playerInventory.player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        Slot sourceSlot = slots.get(invSlot);
        if (!sourceSlot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyStack = sourceStack.copy();
        if (invSlot < 36) {
            if (!insertItem(sourceStack, 36, 36 + slotCount, false)) {
                return ItemStack.EMPTY;
            }
        } else if (invSlot < 36 + slotCount) {
            if (!insertItem(sourceStack, 0, 36, false))  {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (sourceStack.getCount() == 0) {
            sourceSlot.setStack(ItemStack.EMPTY);
        } else {
            sourceSlot.markDirty();
        }
        sourceSlot.onTakeItem(player, sourceStack);
        return copyStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    protected <T> void addSlots(Function4<Inventory, Integer, Integer, Integer, Slot> slotType, Inventory container, int xOrigin, int yOrigin) {
        addSlots(slotType, container, 1, 1, 0, 1, xOrigin, yOrigin);
    }

    protected <T> void addSlots(Function4<Inventory, Integer, Integer, Integer, Slot> slotType, Inventory container, int startIndex, int totalSlots, int xOrigin, int yOrigin) {
        addSlots(slotType, container, 1, 1, startIndex, totalSlots, xOrigin, yOrigin);
    }

    protected <T> void addSlots(Function4<Inventory, Integer, Integer, Integer, Slot> slotType, Inventory container, int rows, int columns, int startIndex, int totalSlots, int xOrigin, int yOrigin) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int slotIndex = column + row * columns + startIndex;
                int x = xOrigin + column * 18;
                int y = yOrigin + row * 18;
                if (slotIndex < startIndex + totalSlots) {
                    this.addSlot(slotType.apply(container, slotIndex, x, y));
                }
            }
        }
    }

    public void addPlayerInventorySlots(Inventory pInventory) {
        // player main inventory
        addSlots(Slot::new, pInventory, 3, 9, 9, 27,8, 86);
        // player hotbar
        addSlots(Slot::new, pInventory, 1, 9, 0, 9,8, 144);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public AbstractProcessingBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public World getWorld() {
        return world;
    }
}
