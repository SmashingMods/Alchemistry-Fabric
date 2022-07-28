package com.smashingmods.alchemistry;

import com.smashingmods.alchemistry.network.AlchemistryNetwork;
import com.smashingmods.alchemistry.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
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
        // Register in-game items, blocks, entities, and GUIs
        ItemRegistry.registerItems();
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.registerBlockEntities();
        ScreenRegistry.registerScreens();
        RecipeRegistry.registerRecipes();

        // Register server-side packet handlers
        AlchemistryNetwork.registerServerHandlers();
    }
}
