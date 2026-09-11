package com.smashingmods.alchemistry.api.blockentity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class AbstractFluidBlockEntity extends AbstractInventoryBlockEntity {

    public static final long BUCKET_CONSTANT = FluidConstants.BUCKET;

    private final SingleVariantStorage<FluidVariant> fluidStorage;

    public AbstractFluidBlockEntity(NonNullList<ItemStack> inventory, BlockEntityType<?> type, BlockPos worldPosition, BlockState state, long energyCapacity, long fluidCapacity) {
        super(inventory, type, worldPosition, state, energyCapacity);
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
                setChanged();
            }
        };
    }

    /**
     * Handles fluid insertion/extraction when using a bucket
     * @return true if fluid was transferred, false if GUI should be opened instead.
     */
    public boolean onBlockActivated(Level level, BlockPos blockPos, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof BucketItem bucket)) return false;
        boolean extracting = held.is(Items.BUCKET);
        FluidVariant fluid = extracting ? fluidStorage.getResource() : FluidVariant.of(bucket.getContent());
        Item result = extracting ? fluid.getFluid().getBucket() : Items.BUCKET;
        if (fluid.isBlank() || result == Items.AIR) return false;
        try (Transaction transaction = Transaction.openOuter()) {
            long transferred = extracting
                ? fluidStorage.extract(fluid, BUCKET_CONSTANT, transaction)
                : fluidStorage.insert(fluid, BUCKET_CONSTANT, transaction);
            if (transferred != BUCKET_CONSTANT) return false;
            if (!level.isClientSide()) {
                transaction.commit();
                player.setItemInHand(hand, net.minecraft.world.item.ItemUtils.createFilledResult(held, player, new ItemStack(result)));
                level.playSound(null, blockPos, extracting ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        return true;
    }

    @Override
    public void saveAdditional(net.minecraft.world.level.storage.ValueOutput tag) {
        tag.store("fluidVariant", FluidVariant.CODEC, fluidStorage.variant);
        tag.putLong("fluid", fluidStorage.amount);
        super.saveAdditional(tag);
    }
    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput tag) {
        super.loadAdditional(tag);
        fluidStorage.variant = tag.read("fluidVariant", FluidVariant.CODEC).orElse(FluidVariant.blank());
        fluidStorage.amount = tag.getLongOr("fluid", 0L);
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
