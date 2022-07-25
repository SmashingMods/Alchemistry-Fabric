package com.technovision.alchemistry.registry;

import com.technovision.alchemistry.Alchemistry;
import com.technovision.alchemistry.common.block.dissolver.DissolverBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockRegistry {

    public static final DissolverBlock DISSOLVER = new DissolverBlock();

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "dissolver"), DISSOLVER);
    }
}
