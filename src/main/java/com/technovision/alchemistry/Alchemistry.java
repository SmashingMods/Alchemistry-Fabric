package com.technovision.alchemistry;

import com.technovision.alchemistry.registry.BlockEntityRegistry;
import com.technovision.alchemistry.registry.BlockRegistry;
import com.technovision.alchemistry.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Chemistry mod that allows you to break down items
 * into elements and recombine them into different items.
 *
 * @version 1.0.0
 * @author TechnoVision
 */
public class Alchemistry implements ModInitializer {

    public static String MOD_ID = "alchemistry";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final ItemGroup MACHINE_TAB = FabricItemGroupBuilder.build(
            new Identifier(Alchemistry.MOD_ID, "machine_tab"),
            () -> new ItemStack(ItemRegistry.DISSOLVER)
    );

    @Override
    public void onInitialize() {
        ItemRegistry.registerItems();
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.registerBlockEntities();
    }
}
