package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.atomizer.AtomizerBlock;
import com.smashingmods.alchemistry.common.block.combiner.CombinerBlock;
import com.smashingmods.alchemistry.common.block.compactor.CompactorBlock;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverBlock;
import com.smashingmods.alchemistry.common.block.liquifier.LiquifierBlock;
import com.smashingmods.alchemistry.common.block.reactor.ReactorCoreBlock;
import com.smashingmods.alchemistry.common.block.reactor.ReactorGlassBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockRegistry {

    public static final DissolverBlock DISSOLVER = new DissolverBlock();
    public static final LiquifierBlock LIQUIFIER = new LiquifierBlock();
    public static final AtomizerBlock ATOMIZER = new AtomizerBlock();
    public static final CompactorBlock COMPACTOR = new CompactorBlock();
    public static final CombinerBlock COMBINER = new CombinerBlock();
    public static final ReactorCoreBlock FUSION_CORE = new ReactorCoreBlock();
    public static final ReactorCoreBlock FISSION_CORE = new ReactorCoreBlock();
    public static final ReactorGlassBlock REACTOR_GLASS = new ReactorGlassBlock();

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "dissolver"), DISSOLVER);
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "liquifier"), LIQUIFIER);
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "atomizer"), ATOMIZER);
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "compactor"), COMPACTOR);
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "combiner"), COMBINER);
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "fission_core"), FISSION_CORE);
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "fusion_core"), FUSION_CORE);
        Registry.register(Registry.BLOCK, new Identifier(Alchemistry.MOD_ID, "reactor_glass"), REACTOR_GLASS);
    }
}
