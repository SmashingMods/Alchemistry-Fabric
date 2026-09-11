package com.smashingmods.alchemistry.client.rei;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import java.util.List;
import java.util.Optional;
public final class ReiDisplayCodecs {
    public static <T extends BasicDisplay> DisplaySerializer<T> create(Function3<List<EntryIngredient>, List<EntryIngredient>, Optional<Identifier>, T> factory) {
        MapCodec<T> codec = RecordCodecBuilder.mapCodec(i -> i.group(
            EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(BasicDisplay::getInputEntries),
            EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(BasicDisplay::getOutputEntries),
            Identifier.CODEC.optionalFieldOf("location").forGetter(BasicDisplay::getDisplayLocation)
        ).apply(i, factory));
        return DisplaySerializer.of(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }
}
