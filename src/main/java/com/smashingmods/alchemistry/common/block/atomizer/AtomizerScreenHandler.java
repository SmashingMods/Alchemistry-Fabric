package com.smashingmods.alchemistry.common.block.atomizer;

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

public class AtomizerScreenHandler extends AbstractAlchemistryScreenHandler {

    protected final ContainerData propertyDelegate;

    public AtomizerScreenHandler(int syncId, Inventory playerInventory, net.minecraft.core.BlockPos position) {
        this(syncId, playerInventory, Objects.requireNonNull(playerInventory.player.level().getBlockEntity(position)), new SimpleContainer(AtomizerBlockEntity.INVENTORY_SIZE), new SimpleContainerData(7));
    }

    protected AtomizerScreenHandler(int syncId, Inventory playerInventory, BlockEntity blockEntity, Container inventory, ContainerData delegate) {
        super(ScreenRegistry.ATOMIZER_SCREEN_HANDLER, syncId, playerInventory, blockEntity, inventory, delegate, 0, AtomizerBlockEntity.INVENTORY_SIZE);
        addSlots(Slot::new, inventory, 1, 1, 0, inventory.getContainerSize(), 98, 35);

        this.propertyDelegate = delegate;
        addDataSlots(delegate);
    }
}
