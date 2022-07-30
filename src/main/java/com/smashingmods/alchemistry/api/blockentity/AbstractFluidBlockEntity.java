package com.smashingmods.alchemistry.api.blockentity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractFluidBlockEntity extends AbstractInventoryBlockEntity {

    private final SingleVariantStorage<FluidVariant> fluidStorage;

    public AbstractFluidBlockEntity(DefaultedList<ItemStack> inventory, BlockEntityType<?> type, BlockPos pos, BlockState state, long energyCapacity, long fluidCapacity) {
        super(inventory, type, pos, state, energyCapacity);
        this.fluidStorage = new SingleVariantStorage<>() {
            @Override
            protected FluidVariant getBlankVariant() {
                return FluidVariant.blank();
            }
            @Override
            protected long getCapacity(FluidVariant variant) {
                return fluidCapacity;
            }
            @Override
            protected void onFinalCommit() {
                markDirty();
            }
        };
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.put("fluidVariant", fluidStorage.variant.toNbt());
        tag.putLong("fluid", fluidStorage.amount);
        super.writeNbt(tag);
    }
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        fluidStorage.variant = FluidVariant.fromNbt(tag.getCompound("fluidVariant"));
        fluidStorage.amount = tag.getLong("fluid");
    }

    public SingleVariantStorage<FluidVariant> getFluidStorage() {
        return fluidStorage;
    }

    public void insertFluid(FluidVariant fluid) {
        try (Transaction transaction = Transaction.openOuter()) {
            long amountInserted = fluidStorage.insert(fluid, 1000, transaction);
            if (amountInserted == 1000) {
                transaction.commit();
            }
        }
    }

    public void extractFluid(FluidVariant fluid) {
        try (Transaction transaction = Transaction.openOuter()) {
            long amountExtracted = fluidStorage.extract(fluid, 1000, transaction);
            if (amountExtracted == 1000) {
                transaction.commit();
            }
        }
    }
}
