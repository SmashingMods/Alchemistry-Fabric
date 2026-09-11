package com.smashingmods.alchemistry.common.block.dissolver;

import com.smashingmods.alchemistry.api.container.slots.OutputSlot;
import com.smashingmods.alchemistry.api.container.AbstractAlchemistryScreenHandler;
import com.smashingmods.alchemistry.registry.ScreenRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;

import java.util.Objects;

public class DissolverScreenHandler extends AbstractAlchemistryScreenHandler {

    protected final ContainerData propertyDelegate;

    public DissolverScreenHandler(int syncId, Inventory playerInventory, net.minecraft.core.BlockPos position) {
        this(syncId, playerInventory,Objects.requireNonNull(playerInventory.player.level().getBlockEntity(position)), new SimpleContainer(DissolverBlockEntity.INVENTORY_SIZE), new SimpleContainerData(4));
    }

    protected DissolverScreenHandler(int syncId, Inventory playerInventory, BlockEntity blockEntity, Container inventory, ContainerData delegate) {
        super(ScreenRegistry.DISSOLVER_SCREEN_HANDLER, syncId, playerInventory, blockEntity, inventory, delegate, 1, DissolverBlockEntity.INVENTORY_SIZE-1);

        // input slots
        addSlots(Slot::new, inventory, 1, 1, 0, 1, 84, 12);
        // output slots 2x5 grid
        addSlots(OutputSlot::new, inventory, 2, 5, 1, 10, 48, 68);

        this.propertyDelegate = delegate;
        addDataSlots(delegate);
    }

    @Override
    public void addPlayerInventorySlots(Container pInventory) {
        addSlots(Slot::new, pInventory, 3, 9, 9, 27, 12, 113);
        addSlots(Slot::new, pInventory, 1, 9, 0, 9, 12, 171);
    }
}
