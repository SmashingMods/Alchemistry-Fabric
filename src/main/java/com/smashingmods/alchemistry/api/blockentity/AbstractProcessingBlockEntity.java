package com.smashingmods.alchemistry.api.blockentity;

import com.smashingmods.alchemistry.Alchemistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.Objects;

public abstract class AbstractProcessingBlockEntity extends BlockEntity implements ProcessingBlockEntity, ExtendedScreenHandlerFactory, Nameable {

    private final Text name;
    private int progress = 0;
    private boolean recipeLocked = false;
    private boolean paused = false;
    private final SimpleEnergyStorage energyStorage;

    public AbstractProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long energyCapacity) {
        super(type, pos, state);
        String blockEntityName = Objects.requireNonNull(Registry.BLOCK_ENTITY_TYPE.getId(getType())).getPath();
        this.name = Text.translatable(String.format("%s.container.%s", Alchemistry.MOD_ID, blockEntityName));
        energyStorage = new SimpleEnergyStorage(energyCapacity, energyCapacity, energyCapacity) {
            @Override
            protected void onFinalCommit() {
                markDirty();
            }
        };
    }

    @Override
    public Text getName() {
        return name != null ? name : this.getDefaultName();
    }

    @Override
    public Text getDisplayName() {
        return getName();
    }

    protected Text getDefaultName() {
        return name;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = super.toInitialChunkDataNbt();
        writeNbt(tag);
        return tag;
    }

    @Override
    public void tick() {
        if (world != null && !world.isClient()) {
            if (!paused) {
                if (!recipeLocked) {
                    updateRecipe();
                }
                if (canProcessRecipe()) {
                    processRecipe();
                }
            }
        }
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public void incrementProgress() {
        this.progress++;
    }

    @Override
    public boolean isRecipeLocked() {
        return this.recipeLocked;
    }

    @Override
    public boolean isProcessingPaused() {
        return this.paused;
    }

    @Override
    public void setRecipeLocked(boolean recipeLocked) {
        this.recipeLocked = recipeLocked;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putInt("progress", progress);
        nbt.putBoolean("locked", isRecipeLocked());
        nbt.putBoolean("paused", isProcessingPaused());
        nbt.putLong("energy", energyStorage.amount);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        setProgress(nbt.getInt("progress"));
        setRecipeLocked(nbt.getBoolean("locked"));
        setPaused(nbt.getBoolean("paused"));
        insertEnergy(nbt.getLong("energy"));
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public void insertEnergy(long value) {
        try (Transaction transaction = Transaction.openOuter()) {
            long amountExtracted = getEnergyStorage().insert(value, transaction);
            if (amountExtracted == value) {
                transaction.commit();
            }
        }
    }

    public void extractEnergy(long value) {
        try (Transaction transaction = Transaction.openOuter()) {
            long amountExtracted = getEnergyStorage().extract(value, transaction);
            if (amountExtracted == value) {
                transaction.commit();
            }
        }
    }

    public void forceSync() {
        this.markDirty();
        world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }
}
