package com.smashingmods.alchemistry.network.packets;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.server.level.ServerPlayer;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
public record CombinerIndexPacket(BlockPos blockPos, int recipeIndex) implements AlchemistryPacket {
    public static final Type<CombinerIndexPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("alchemistry", "combiner_index"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CombinerIndexPacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, CombinerIndexPacket::blockPos, ByteBufCodecs.VAR_INT, CombinerIndexPacket::recipeIndex, CombinerIndexPacket::new);
    @Override public Type<CombinerIndexPacket> type() { return TYPE; }
    public static void handle(ServerPlayer player, CombinerIndexPacket packet) {
        var entity = AlchemistryNetwork.getOpenMachine(player, packet.blockPos());
        if (entity instanceof com.smashingmods.alchemistry.common.block.combiner.CombinerBlockEntity combiner && !combiner.isRecipeLocked() && packet.recipeIndex() >= 0 && packet.recipeIndex() < combiner.getRecipes().size()) {
            var recipe = combiner.getRecipes().get(packet.recipeIndex());
            if (combiner.getRecipe() != recipe) {
                combiner.setProgress(0);
                combiner.setRecipe(recipe);
            }
            combiner.forceSync();
        }
    }
}
