package com.smashingmods.alchemistry.network.packets;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.server.level.ServerPlayer;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
public record CompactorButtonPacket(BlockPos blockPos) implements AlchemistryPacket {
    public static final Type<CompactorButtonPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("alchemistry", "target_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CompactorButtonPacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, CompactorButtonPacket::blockPos, CompactorButtonPacket::new);
    @Override public Type<CompactorButtonPacket> type() { return TYPE; }
    public static void handle(ServerPlayer player, CompactorButtonPacket packet) {
        var entity = AlchemistryNetwork.getOpenMachine(player, packet.blockPos());
        if (entity instanceof com.smashingmods.alchemistry.common.block.compactor.CompactorBlockEntity compactor) { compactor.setTarget(net.minecraft.world.item.ItemStack.EMPTY); compactor.forceSync(); }
    }
}
