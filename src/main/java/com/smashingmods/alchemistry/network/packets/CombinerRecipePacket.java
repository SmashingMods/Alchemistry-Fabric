package com.smashingmods.alchemistry.network.packets;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.combiner.CombinerBlockEntity;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CombinerRecipePacket implements AlchemistryPacket {

    public static final Identifier PACKET_ID = new Identifier(Alchemistry.MOD_ID, "combiner_recipe");

    private final BlockPos blockPos;
    private final NbtCompound tag;

    public CombinerRecipePacket(BlockPos blockPos, CombinerRecipe recipe) {
        this.blockPos = blockPos;
        this.tag = new NbtCompound();

        // Serialize recipe
        tag.putString("namespace", recipe.getId().getNamespace());
        tag.putString("path", recipe.getId().getPath());
        tag.putInt("output", Registry.ITEM.getRawId(recipe.getOutput().getItem()));
        NbtCompound inputs = new NbtCompound();
        for (ItemStack stack : recipe.getInput()) {
            NbtCompound entry = new NbtCompound();
            entry.putInt("count", stack.getCount());
            entry.putInt("item", Registry.ITEM.getRawId(stack.getItem()));
            inputs.put(stack.getItem().getName().getString(), entry);
        }
        tag.put("input", inputs);
    }

    public CombinerRecipePacket(PacketByteBuf buffer) {
        this.blockPos = buffer.readBlockPos();
        this.tag = buffer.readNbt();
    }

    public void encode(PacketByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeNbt(tag);
    }

    public PacketByteBuf toByteBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockPos);
        buf.writeNbt(tag);
        return buf;
    }

    public static void handle(ClientPlayNetworkHandler handler, CombinerRecipePacket packet) {
        // Get block entity from position
        CombinerBlockEntity blockEntity = (CombinerBlockEntity) handler.getWorld().getBlockEntity(packet.blockPos);
        Objects.requireNonNull(blockEntity);

        // Deserialize recipe
        Identifier id = new Identifier(packet.tag.getString("namespace"), packet.tag.getString("path"));
        ItemStack output = new ItemStack(Registry.ITEM.get(packet.tag.getInt("output")));
        List<ItemStack> inputs = new ArrayList<>();
        NbtCompound inputCompound = packet.tag.getCompound("input");
        for (String key : inputCompound.getKeys()) {
            NbtCompound entry = inputCompound.getCompound(key);
            int count = entry.getInt("count");
            Item item = Registry.ITEM.get(entry.getInt("item"));
            inputs.add(new ItemStack(item, count));
        }

        // Add recipe to list
        blockEntity.addRecipe(new CombinerRecipe(id, inputs, output));
    }
}
