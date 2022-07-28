package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockRegistry {

    public static final DissolverBlock DISSOLVER = new DissolverBlock();

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "dissolver"), DISSOLVER);
    }
}
