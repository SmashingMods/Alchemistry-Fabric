package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/** Stable port views; transaction snapshots belong to the controller, never to individual ports. */
public final class ReactorItemStorage extends CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>>
        implements SlottedStorage<ItemVariant> {
    public ReactorItemStorage(BlockEntity port, Supplier<AbstractReactorBlockEntity> controller, boolean input) {
        super(createSlots(port, controller, input));
    }

    private static List<SingleSlotStorage<ItemVariant>> createSlots(BlockEntity port,
            Supplier<AbstractReactorBlockEntity> controller, boolean input) {
        // Both controller types have three slots. Retained views must work across detach and reconnect.
        return IntStream.range(0, 3).mapToObj(index -> (SingleSlotStorage<ItemVariant>) new SingleSlotStorage<ItemVariant>() {
            @Nullable
            private SingleSlotStorage<ItemVariant> backing() {
                var current = controller.get();
                if (port.isRemoved() || current == null || current.isRemoved()
                        || port.getLevel() == null || current.getLevel() != port.getLevel()) return null;
                return current.getItemStorage().getSlot(index);
            }

            @Override
            public boolean supportsInsertion() { return input && backing() != null; }

            @Override
            public boolean supportsExtraction() { return !input && backing() != null; }

            @Override
            public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                var storage = backing();
                return input && storage != null ? storage.insert(resource, maxAmount, transaction) : 0;
            }

            @Override
            public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                var storage = backing();
                return !input && storage != null ? storage.extract(resource, maxAmount, transaction) : 0;
            }

            @Override
            public boolean isResourceBlank() { return getResource().isBlank(); }

            @Override
            public ItemVariant getResource() {
                var storage = backing();
                return storage == null ? ItemVariant.blank() : storage.getResource();
            }

            @Override
            public long getAmount() {
                var storage = backing();
                return storage == null ? 0 : storage.getAmount();
            }

            @Override
            public long getCapacity() {
                var storage = backing();
                return storage == null ? 0 : storage.getCapacity();
            }
        }).toList();
    }

    @Override
    public int getSlotCount() { return parts.size(); }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) { return parts.get(slot); }
}
