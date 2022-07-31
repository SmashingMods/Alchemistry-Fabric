package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.atomizer.AtomizerBlock;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverBlock;
import com.smashingmods.alchemistry.common.block.liquifier.LiquifierBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockRegistry {

    public static final DissolverBlock DISSOLVER = new DissolverBlock();
    public static final LiquifierBlock LIQUIFIER = new LiquifierBlock();
    public static final AtomizerBlock ATOMIZER = new AtomizerBlock();

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "dissolver"), DISSOLVER);
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "liquifier"), LIQUIFIER);
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "atomizer"), ATOMIZER);
    }
}
