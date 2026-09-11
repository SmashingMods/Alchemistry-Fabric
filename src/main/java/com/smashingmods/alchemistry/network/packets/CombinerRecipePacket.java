package com.smashingmods.alchemistry.network.packets;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import com.smashingmods.alchemistry.common.recipe.combiner.*;
public record CombinerRecipePacket(BlockPos blockPos, Identifier id, CombinerRecipe recipe, boolean reset) implements AlchemistryPacket {
    public CombinerRecipePacket(BlockPos pos, CombinerRecipe recipe, boolean reset) { this(pos, recipe.getId(), recipe, reset); }
    public static final Type<CombinerRecipePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("alchemistry", "combiner_recipe"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CombinerRecipePacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, CombinerRecipePacket::blockPos, Identifier.STREAM_CODEC, CombinerRecipePacket::id,
        CombinerRecipeSerializer.INSTANCE.streamCodec(), CombinerRecipePacket::recipe,
        ByteBufCodecs.BOOL, CombinerRecipePacket::reset, CombinerRecipePacket::new);
    @Override public Type<CombinerRecipePacket> type() { return TYPE; }
}
