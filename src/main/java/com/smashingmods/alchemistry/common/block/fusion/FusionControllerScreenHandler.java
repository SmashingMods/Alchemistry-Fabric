package com.smashingmods.alchemistry.common.block.fusion;

import com.smashingmods.alchemistry.api.container.AbstractAlchemistryScreenHandler;
import com.smashingmods.alchemistry.api.container.slots.OutputSlot;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverBlockEntity;
import com.smashingmods.alchemistry.common.block.fission.FissionControllerBlockEntity;
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

public class FusionControllerScreenHandler extends AbstractAlchemistryScreenHandler {

    protected final PropertyDelegate propertyDelegate;

    public FusionControllerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buffer) {
        this(syncId, playerInventory,Objects.requireNonNull(playerInventory.player.getWorld().getBlockEntity(buffer.readBlockPos())), new SimpleInventory(FissionControllerBlockEntity.INVENTORY_SIZE), new ArrayPropertyDelegate(4));
    }

    protected FusionControllerScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity, Inventory inventory, PropertyDelegate delegate) {
        super(ScreenRegistry.FUSION_SCREEN_HANDLER, syncId, playerInventory, blockEntity, inventory, delegate, 1, DissolverBlockEntity.INVENTORY_SIZE-1);

        addSlots(Slot::new, inventory, 0, 1, 44, 35);
        addSlots(Slot::new, inventory, 1, 1, 62, 35);
        addSlots(OutputSlot::new, inventory, 2, 1, 134, 35);

        this.propertyDelegate = delegate;
        addProperties(delegate);
    }

    @Override
    public void addPlayerInventorySlots(Inventory pInventory) {
        addSlots(Slot::new, pInventory, 3, 9, 9, 27, 8, 84);
        addSlots(Slot::new, pInventory, 1, 9, 0, 9, 8, 142);
    }
}
