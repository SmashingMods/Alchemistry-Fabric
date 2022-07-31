package com.smashingmods.alchemistry.common.block.atomizer;

import com.smashingmods.alchemistry.api.container.AbstractAlchemistryScreenHandler;
import com.smashingmods.alchemistry.registry.ScreenRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;

import java.util.Objects;

public class AtomizerScreenHandler extends AbstractAlchemistryScreenHandler {

    protected final PropertyDelegate propertyDelegate;

    public AtomizerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buffer) {
        this(syncId, playerInventory, Objects.requireNonNull(playerInventory.player.getWorld().getBlockEntity(buffer.readBlockPos())), new SimpleInventory(AtomizerBlockEntity.INVENTORY_SIZE), new ArrayPropertyDelegate(7));
    }

    protected AtomizerScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity, Inventory inventory, PropertyDelegate delegate) {
        super(ScreenRegistry.ATOMIZER_SCREEN_HANDLER, syncId, playerInventory, blockEntity, inventory, delegate, AtomizerBlockEntity.INVENTORY_SIZE);
        addSlots(Slot::new, inventory, 1, 1, 0, inventory.size(), 98, 35);

        this.propertyDelegate = delegate;
        addProperties(delegate);
    }
}
