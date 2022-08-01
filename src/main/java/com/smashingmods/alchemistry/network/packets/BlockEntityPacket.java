package com.smashingmods.alchemistry.network.packets;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.api.blockentity.AbstractProcessingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class BlockEntityPacket implements AlchemistryPacket {

    public static final Identifier PACKET_ID = new Identifier(Alchemistry.MOD_ID, "block_entity_update");

    private final BlockPos blockPos;
    private final NbtCompound tag;

    public BlockEntityPacket(BlockPos blockPos, NbtCompound tag) {
        this.blockPos = blockPos;
        this.tag = tag;
    }

    public BlockEntityPacket(PacketByteBuf buffer) {
        this.blockPos = buffer.readBlockPos();
        this.tag = buffer.readNbt();
    }

    public void encode(PacketByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeNbt(tag);
    }

    public PacketByteBuf toByteBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockPos);
        return buf;
    }

    public static void handle(ClientPlayNetworkHandler handler, BlockEntityPacket packet) {
        // Get block entity from position
        AbstractProcessingBlockEntity blockEntity = (AbstractProcessingBlockEntity) handler.getWorld().getBlockEntity(packet.blockPos);
        Objects.requireNonNull(blockEntity);

        // Set new values
        blockEntity.readNbt(packet.tag);
        blockEntity.forceSync();
    }
}
