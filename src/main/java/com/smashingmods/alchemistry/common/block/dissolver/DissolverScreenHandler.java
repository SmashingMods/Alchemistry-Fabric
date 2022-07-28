package com.smashingmods.alchemistry.common.block.dissolver;

import com.smashingmods.alchemistry.api.blockentity.slots.OutputSlot;
import com.smashingmods.alchemistry.api.container.AbstractAlchemistryScreenHandler;
import com.smashingmods.alchemistry.registry.ScreenRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;

import java.util.Objects;

public class DissolverScreenHandler extends AbstractAlchemistryScreenHandler {

    public DissolverScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buffer) {
        this(syncId, playerInventory,Objects.requireNonNull(playerInventory.player.getWorld().getBlockEntity(buffer.readBlockPos())), new SimpleInventory(DissolverBlockEntity.INVENTORY_SIZE));
    }

    public DissolverScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity, Inventory inventory) {
        super(ScreenRegistry.DISSOLVER_SCREEN_HANDLER, syncId, playerInventory, blockEntity, inventory, DissolverBlockEntity.INVENTORY_SIZE);

        // input
        addSlots(Slot::new, inventory, 1, 1, 0, 1, 84, 12);
        // output 2x5 grid
        addSlots(OutputSlot::new, inventory, 2, 5, 1, 10, 48, 68);
    }

    @Override
    public void addPlayerInventorySlots(Inventory pInventory) {
        addSlots(Slot::new, pInventory, 3, 9, 9, 27, 12, 113);
        addSlots(Slot::new, pInventory, 1, 9, 0, 9, 12, 171);
    }
}
