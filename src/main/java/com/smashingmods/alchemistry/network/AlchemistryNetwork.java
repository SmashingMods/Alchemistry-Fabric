package com.smashingmods.alchemistry.network;
import com.smashingmods.alchemistry.network.packets.*;
import com.smashingmods.alchemistry.api.blockentity.AbstractProcessingBlockEntity;
import com.smashingmods.alchemistry.api.container.AbstractAlchemistryScreenHandler;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
public final class AlchemistryNetwork {
    public static void sendToClient(AlchemistryPacket packet, ServerPlayer player) { ServerPlayNetworking.send(player, packet); }
    public static AbstractProcessingBlockEntity getOpenMachine(ServerPlayer player, BlockPos pos) {
        if (player.containerMenu instanceof AbstractAlchemistryScreenHandler menu && menu.getBlockEntity().getBlockPos().equals(pos) && menu.stillValid(player)) return menu.getBlockEntity();
        return null;
    }
    public static void registerServerHandlers() {
        PayloadTypeRegistry.serverboundPlay().register(ProcessingButtonPacket.TYPE, ProcessingButtonPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CompactorButtonPacket.TYPE, CompactorButtonPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CombinerIndexPacket.TYPE, CombinerIndexPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CombinerRecipePacket.TYPE, CombinerRecipePacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ProcessingButtonPacket.TYPE, (packet, context) -> ProcessingButtonPacket.handle(context.player(), packet));
        ServerPlayNetworking.registerGlobalReceiver(CompactorButtonPacket.TYPE, (packet, context) -> CompactorButtonPacket.handle(context.player(), packet));
        ServerPlayNetworking.registerGlobalReceiver(CombinerIndexPacket.TYPE, (packet, context) -> CombinerIndexPacket.handle(context.player(), packet));
    }
}
