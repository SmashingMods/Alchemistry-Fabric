package com.smashingmods.alchemistry.network.packets;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.api.blockentity.AbstractProcessingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class ProcessingButtonPacket implements AlchemistryPacket {

    public static final Identifier PACKET_ID = new Identifier(Alchemistry.MOD_ID, "processing_button");

    private final BlockPos blockPos;
    private final boolean isLocked;
    private final boolean isPaused;

    public ProcessingButtonPacket(BlockPos blockPos, boolean isLocked, boolean isPaused) {
        this.blockPos = blockPos;
        this.isLocked = isLocked;
        this.isPaused = isPaused;
    }

    public ProcessingButtonPacket(PacketByteBuf buffer) {
        this.blockPos = buffer.readBlockPos();
        this.isLocked = buffer.readBoolean();
        this.isPaused = buffer.readBoolean();
    }

    public void encode(PacketByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeBoolean(isLocked);
        buffer.writeBoolean(isPaused);
    }

    public PacketByteBuf toByteBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(isLocked);
        buf.writeBoolean(isPaused);
        return buf;
    }

    public static void handle(ServerPlayNetworkHandler handler, ProcessingButtonPacket packet) {
        // Get block entity from position
        AbstractProcessingBlockEntity blockEntity = (AbstractProcessingBlockEntity) handler.getPlayer().getWorld().getBlockEntity(packet.blockPos);
        Objects.requireNonNull(blockEntity);

        // Set new values
        blockEntity.setRecipeLocked(packet.isLocked);
        blockEntity.setPaused(packet.isPaused);
        blockEntity.markDirty();
        handler.getPlayer().getWorld().updateListeners(packet.blockPos, blockEntity.getCachedState(), blockEntity.getCachedState(), 3);
    }
}
