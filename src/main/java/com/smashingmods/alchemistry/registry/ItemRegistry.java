package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemRegistry {

    public static final FabricItemSettings ITEM_SETTINGS = new FabricItemSettings().group(Alchemistry.MACHINE_TAB);

    public static final BlockItem DISSOLVER = new BlockItem(BlockRegistry.DISSOLVER, ITEM_SETTINGS);
    public static final BlockItem LIQUIFIER = new BlockItem(BlockRegistry.LIQUIFIER, ITEM_SETTINGS);
    public static final BlockItem ATOMIZER = new BlockItem(BlockRegistry.ATOMIZER, ITEM_SETTINGS);
    public static final BlockItem COMPACTOR = new BlockItem(BlockRegistry.COMPACTOR, ITEM_SETTINGS);
    public static final BlockItem COMBINER = new BlockItem(BlockRegistry.COMBINER, ITEM_SETTINGS);
    public static final BlockItem FUSION_CORE = new BlockItem(BlockRegistry.FUSION_CORE, ITEM_SETTINGS);
    public static final BlockItem FISSION_CORE = new BlockItem(BlockRegistry.FISSION_CORE, ITEM_SETTINGS);
    public static final BlockItem REACTOR_GLASS = new BlockItem(BlockRegistry.REACTOR_GLASS, ITEM_SETTINGS);
    public static final BlockItem REACTOR_ENERGY = new BlockItem(BlockRegistry.REACTOR_ENERGY, ITEM_SETTINGS);
    public static final BlockItem REACTOR_INPUT = new BlockItem(BlockRegistry.REACTOR_INPUT, ITEM_SETTINGS);

    public static void registerItems() {
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "dissolver"), DISSOLVER);
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "liquifier"), LIQUIFIER);
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "atomizer"), ATOMIZER);
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "compactor"), COMPACTOR);
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "combiner"), COMBINER);
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "fusion_core"), FUSION_CORE);
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "fission_core"), FISSION_CORE);
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "reactor_glass"), REACTOR_GLASS);
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "reactor_energy"), REACTOR_ENERGY);
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "reactor_input"), REACTOR_INPUT);
    }
}
