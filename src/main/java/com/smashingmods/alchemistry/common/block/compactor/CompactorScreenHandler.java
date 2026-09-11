package com.smashingmods.alchemistry.common.block.compactor;

import com.smashingmods.alchemistry.api.container.slots.OutputSlot;
import com.smashingmods.alchemistry.api.container.slots.TargetSlot;
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

public class CompactorScreenHandler extends AbstractAlchemistryScreenHandler {

    protected final ContainerData propertyDelegate;

    public CompactorScreenHandler(int syncId, Inventory playerInventory, net.minecraft.core.BlockPos position) {
        this(syncId, playerInventory,Objects.requireNonNull(playerInventory.player.level().getBlockEntity(position)), new SimpleContainer(CompactorBlockEntity.INVENTORY_SIZE), new SimpleContainerData(4));
    }

    protected CompactorScreenHandler(int syncId, Inventory playerInventory, BlockEntity blockEntity, Container inventory, ContainerData delegate) {
        super(ScreenRegistry.COMPACTOR_SCREEN_HANDLER, syncId, playerInventory, blockEntity, inventory, delegate, 2, 1);

        // input slot
        addSlots(Slot::new, inventory, 0, 1, 51, 35);
        // target slot
        addSlot(new TargetSlot(inventory, 1, 80, 12, (CompactorBlockEntity) blockEntity));
        // output slot
        addSlots(OutputSlot::new, inventory, 2, 1, 111, 35);

        this.propertyDelegate = delegate;
        addDataSlots(delegate);
    }

    @Override
    public void addPlayerInventorySlots(Container pInventory) {
        addSlots(Slot::new, pInventory, 3, 9, 9, 27, 8, 84);
        addSlots(Slot::new, pInventory, 1, 9, 0, 9, 8, 142);
    }
}
