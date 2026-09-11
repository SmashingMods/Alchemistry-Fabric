package com.smashingmods.alchemistry.network.packets;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.server.level.ServerPlayer;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
public record ProcessingButtonPacket(BlockPos blockPos, boolean isLocked, boolean isPaused) implements AlchemistryPacket {
    public static final Type<ProcessingButtonPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("alchemistry", "processing_button"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ProcessingButtonPacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ProcessingButtonPacket::blockPos, ByteBufCodecs.BOOL, ProcessingButtonPacket::isLocked, ByteBufCodecs.BOOL, ProcessingButtonPacket::isPaused, ProcessingButtonPacket::new);
    @Override public Type<ProcessingButtonPacket> type() { return TYPE; }
    public static void handle(ServerPlayer player, ProcessingButtonPacket packet) {
        var entity = AlchemistryNetwork.getOpenMachine(player, packet.blockPos());
        if (entity != null) { entity.setRecipeLocked(packet.isLocked()); entity.setPaused(packet.isPaused()); entity.forceSync(); }
    }
}
