package com.smashingmods.alchemistry.registry;
import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.atomizer.*;
import com.smashingmods.alchemistry.common.block.combiner.*;
import com.smashingmods.alchemistry.common.block.compactor.*;
import com.smashingmods.alchemistry.common.block.dissolver.*;
import com.smashingmods.alchemistry.common.block.liquifier.*;
import com.smashingmods.alchemistry.common.block.fission.*;
import com.smashingmods.alchemistry.common.block.fusion.*;
import com.smashingmods.alchemistry.common.block.reactor.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
public class BlockRegistry {
    public static final DissolverBlock DISSOLVER = register("dissolver", DissolverBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final LiquifierBlock LIQUIFIER = register("liquifier", LiquifierBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final AtomizerBlock ATOMIZER = register("atomizer", AtomizerBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final CompactorBlock COMPACTOR = register("compactor", CompactorBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final CombinerBlock COMBINER = register("combiner", CombinerBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final ReactorCoreBlock FUSION_CORE = register("fusion_core", ReactorCoreBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final ReactorCoreBlock FISSION_CORE = register("fission_core", ReactorCoreBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final ReactorGlassBlock REACTOR_GLASS = register("reactor_glass", ReactorGlassBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion().sound(SoundType.GLASS));
    public static final ReactorEnergyBlock REACTOR_ENERGY = register("reactor_energy", ReactorEnergyBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final ReactorInputBlock REACTOR_INPUT = register("reactor_input", ReactorInputBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final ReactorOutputBlock REACTOR_OUTPUT = register("reactor_output", ReactorOutputBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final Block REACTOR_CASING = register("reactor_casing", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final FissionControllerBlock FISSION_CONTROLLER = register("fission_chamber_controller", FissionControllerBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final FusionControllerBlock FUSION_CONTROLLER = register("fusion_chamber_controller", FusionControllerBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    private static <T extends Block> T register(String id, java.util.function.Function<BlockBehaviour.Properties,T> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, id));
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
    }
    public static void registerBlocks() { }
}
