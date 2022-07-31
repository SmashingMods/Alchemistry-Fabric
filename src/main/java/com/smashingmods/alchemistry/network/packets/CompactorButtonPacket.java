package com.smashingmods.alchemistry.network.packets;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.compactor.CompactorBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class CompactorButtonPacket implements AlchemistryPacket {

    public static final Identifier PACKET_ID = new Identifier(Alchemistry.MOD_ID, "target_update");

    private final BlockPos blockPos;

    public CompactorButtonPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public CompactorButtonPacket(PacketByteBuf buffer) {
        this.blockPos = buffer.readBlockPos();
    }

    public void encode(PacketByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
    }

    public PacketByteBuf toByteBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockPos);
        return buf;
    }

    public static void handle(ServerPlayNetworkHandler handler, CompactorButtonPacket packet) {
        // Get block entity from position
        CompactorBlockEntity blockEntity = (CompactorBlockEntity) handler.getPlayer().getWorld().getBlockEntity(packet.blockPos);
        Objects.requireNonNull(blockEntity);

        // Set new values
        blockEntity.setTarget(ItemStack.EMPTY);
        blockEntity.markDirty();
        handler.getPlayer().getWorld().updateListeners(packet.blockPos, blockEntity.getCachedState(), blockEntity.getCachedState(), 3);
    }
}
