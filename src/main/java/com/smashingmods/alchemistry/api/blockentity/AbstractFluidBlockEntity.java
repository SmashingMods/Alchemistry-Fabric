package com.smashingmods.alchemistry.api.blockentity;

import com.smashingmods.alchemistry.mixin.BucketItemMixin;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractFluidBlockEntity extends AbstractInventoryBlockEntity {

    public static final long BUCKET_CONSTANT = FluidConstants.BUCKET;

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

    /**
     * Handles fluid insertion/extraction when using a bucket
     * @return true if fluid was transferred, false if GUI should be opened instead.
     */
    public boolean onBlockActivated(World world, BlockPos blockPos, PlayerEntity player, Hand hand) {
        Item item = player.getStackInHand(hand).getItem();
        if (item instanceof BucketItem bucket) {
            if (item == Items.BUCKET && getFluidStorage().getAmount() >= BUCKET_CONSTANT) {
                // Extract fluid if possible
                FluidVariant fluidVariant = getFluidStorage().getResource();
                try { extractFluid(fluidVariant); }
                catch (IllegalArgumentException e) { return false; }

                // Fill bucket in hand
                if (!world.isClient() && !player.isCreative()) player.setStackInHand(hand, new ItemStack(fluidVariant.getFluid().getBucketItem()));
                else player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0f, 1.0f);
            } else if (item != Items.BUCKET && getFluidStorage().getAmount() + BUCKET_CONSTANT <= getFluidStorage().getCapacity()) {
                // Insert fluid if it matches
                Fluid fluid = ((BucketItemMixin) bucket).getFluid();
                FluidVariant fluidVariant = FluidVariant.of(fluid);
                if (getFluidStorage().getAmount() == 0 || fluidVariant.equals(getFluidStorage().getResource())) insertFluid(fluidVariant);
                else return false;

                // Empty bucket in hand
                if (!world.isClient() && !player.isCreative()) player.setStackInHand(hand, new ItemStack(Items.BUCKET));
                else player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1.0f, 1.0f);
            }
            return true;
        }
        return false;
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
        insertFluid(fluid, BUCKET_CONSTANT);
    }

    public void extractFluid(FluidVariant fluid) {
        extractFluid(fluid, BUCKET_CONSTANT);
    }

    public void insertFluid(FluidVariant fluid, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long amountInserted = fluidStorage.insert(fluid, amount, transaction);
            if (amountInserted == amount) {
                transaction.commit();
            }
        }
    }

    public void extractFluid(FluidVariant fluid, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long amountExtracted = fluidStorage.extract(fluid, amount, transaction);
            if (amountExtracted == amount) {
                transaction.commit();
            }
        }
    }
}
