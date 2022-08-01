package com.smashingmods.alchemistry.network;

import com.smashingmods.alchemistry.network.packets.AlchemistryPacket;
import com.smashingmods.alchemistry.network.packets.CombinerRecipePacket;
import com.smashingmods.alchemistry.network.packets.ProcessingButtonPacket;
import com.smashingmods.alchemistry.network.packets.CompactorButtonPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Sets up and handles networking for Alchemistry.
 * Requires that any packets sent implement the AlchemistryPacket interface.
 *
 * @author TechnoVision
 */
public class AlchemistryNetwork {

    /**
     * Sends a AlchemistryPacket to the server side.
     * @param packet a packet with data to be sent.
     */
    public static void sendToServer(AlchemistryPacket packet) {
        if (packet instanceof ProcessingButtonPacket processingButtonPacket) {
            ClientPlayNetworking.send(ProcessingButtonPacket.PACKET_ID, processingButtonPacket.toByteBuf());
        }
        else if (packet instanceof CompactorButtonPacket targetUpdatePacket) {
            ClientPlayNetworking.send(CompactorButtonPacket.PACKET_ID, targetUpdatePacket.toByteBuf());
        }
    }

    /**
     * Sends a AlchemistryPacket to the client side.
     * @param packet a packet with data to be sent.
     */
    public static void sendToClient(AlchemistryPacket packet, ServerPlayerEntity recipient) {
        if (packet instanceof CombinerRecipePacket blockEntityPacket) {
            ServerPlayNetworking.send(recipient, CombinerRecipePacket.PACKET_ID, blockEntityPacket.toByteBuf());
        }
    }

    /**
     * Registers the handlers for each of the client-side packets.
     * Should only be called once inside the client mod initializer.
     */
    public static void registerClientHandlers() {
        // CombinerRecipePacket
        ClientPlayNetworking.registerGlobalReceiver(CombinerRecipePacket.PACKET_ID, (client, handler, buf, sender) -> {
            CombinerRecipePacket packet = new CombinerRecipePacket(buf);
            client.execute(() -> CombinerRecipePacket.handle(handler, packet));
        });
    }

    /**
     * Registers the handlers for each of the server-side packets.
     * Should only be called once inside the mod initializer.
     */
    public static void registerServerHandlers() {
        // ProcessingButtonPacket
        ServerPlayNetworking.registerGlobalReceiver(ProcessingButtonPacket.PACKET_ID, (server, player, handler, buf, sender) -> {
            ProcessingButtonPacket packet = new ProcessingButtonPacket(buf);
            server.execute(() -> ProcessingButtonPacket.handle(handler, packet));
        });

        // TargetUpdatePacket
        ServerPlayNetworking.registerGlobalReceiver(CompactorButtonPacket.PACKET_ID, (server, player, handler, buf, sender) -> {
            CompactorButtonPacket packet = new CompactorButtonPacket(buf);
            server.execute(() -> CompactorButtonPacket.handle(handler, packet));
        });
    }
}
