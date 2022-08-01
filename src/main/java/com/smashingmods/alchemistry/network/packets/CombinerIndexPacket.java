package com.smashingmods.alchemistry.network.packets;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.combiner.CombinerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class CombinerIndexPacket implements AlchemistryPacket {

    public static final Identifier PACKET_ID = new Identifier(Alchemistry.MOD_ID, "combiner_index");

    private final BlockPos blockPos;
    private final int recipeIndex;

    public CombinerIndexPacket(BlockPos blockPos, int recipeIndex) {
        this.blockPos = blockPos;
        this.recipeIndex = recipeIndex;
    }

    public CombinerIndexPacket(PacketByteBuf buffer) {
        this.blockPos = buffer.readBlockPos();
        this.recipeIndex = buffer.readInt();
    }

    public void encode(PacketByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeInt(recipeIndex);
    }

    public PacketByteBuf toByteBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockPos);
        buf.writeInt(recipeIndex);
        return buf;
    }

    public static void handle(ServerPlayNetworkHandler handler, CombinerIndexPacket packet) {
        // Get block entity from position
        CombinerBlockEntity blockEntity = (CombinerBlockEntity) handler.getPlayer().getWorld().getBlockEntity(packet.blockPos);
        Objects.requireNonNull(blockEntity);

        // Set selected recipe on server side
        blockEntity.setRecipe(blockEntity.getRecipes().get(packet.recipeIndex));
    }
}
