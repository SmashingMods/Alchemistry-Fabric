package com.smashingmods.alchemistry.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class RecipeCodecs {
    public static final Identifier UNASSIGNED = Identifier.fromNamespaceAndPath("alchemistry", "unassigned");
    public static final Codec<RecipeStack> STACK = RecordCodecBuilder.create(i -> i.group(
        BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(RecipeStack::item),
        Codec.intRange(1, 9999).optionalFieldOf("count", 1).forGetter(RecipeStack::count)
    ).apply(i, RecipeStack::new));
    public record FluidAmount(FluidVariant fluid, long amount) { }
    public static final Codec<FluidAmount> FLUID = RecordCodecBuilder.create(i -> i.group(
        BuiltInRegistries.FLUID.byNameCodec().xmap(FluidVariant::of, FluidVariant::getFluid).fieldOf("fluid").forGetter(FluidAmount::fluid),
        Codec.LONG.validate(value -> value > 0 ? com.mojang.serialization.DataResult.success(value) : com.mojang.serialization.DataResult.error(() -> "Amount must be positive")).fieldOf("amount").forGetter(FluidAmount::amount)
    ).apply(i, FluidAmount::new));
    public static <T extends Recipe<?>> RecipeSerializer<T> serializer(MapCodec<T> codec) {
        return new RecipeSerializer<>(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }
}
