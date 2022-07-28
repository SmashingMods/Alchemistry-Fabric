package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemRegistry {

    public static final FabricItemSettings ITEM_SETTINGS = new FabricItemSettings().group(Alchemistry.MACHINE_TAB);

    public static final BlockItem DISSOLVER = new BlockItem(BlockRegistry.DISSOLVER, ITEM_SETTINGS);

    public static void registerItems() {
        Registry.register(Registry.ITEM, new Identifier(Alchemistry.MOD_ID, "dissolver"), DISSOLVER);
    }
}
