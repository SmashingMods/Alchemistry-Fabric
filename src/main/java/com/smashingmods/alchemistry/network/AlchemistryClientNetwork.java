package com.smashingmods.alchemistry.network;
import com.smashingmods.alchemistry.network.packets.*;
import com.smashingmods.alchemistry.common.block.combiner.CombinerBlockEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
public final class AlchemistryClientNetwork {
    public static void sendToServer(AlchemistryPacket packet) { ClientPlayNetworking.send(packet); }
    public static void registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(CombinerRecipePacket.TYPE, (packet, context) -> {
            var level = context.client().level;
            if (level != null && level.getBlockEntity(packet.blockPos()) instanceof CombinerBlockEntity machine) {
                if (packet.reset()) machine.getRecipes().clear();
                packet.recipe().setId(packet.id());
                machine.addRecipe(packet.recipe());
            }
        });
    }
}
