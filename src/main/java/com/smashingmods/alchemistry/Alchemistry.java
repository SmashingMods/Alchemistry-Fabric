package com.smashingmods.alchemistry;

import com.smashingmods.alchemistry.datagen.RecipeGenerator;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
import com.smashingmods.alchemistry.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.neoforged.fml.config.ModConfig;
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

    public static final CreativeModeTab MACHINE_TAB = net.minecraft.core.Registry.register(
        net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath(MOD_ID, "machine_tab"),
        FabricCreativeModeTab.builder().title(net.minecraft.network.chat.Component.translatable("itemGroup.alchemistry.machine_tab"))
            .icon(() -> new ItemStack(ItemRegistry.ATOMIZER))
            .displayItems((context, entries) -> ItemRegistry.ITEMS.forEach(entries::accept)).build());

    @Override
    public void onInitialize() {
        // Register and load config
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, Config.COMMON_SPEC);

        // Register in-game items, blocks, entities, and GUIs
        BlockRegistry.registerBlocks();
        ItemRegistry.registerItems();
        BlockEntityRegistry.registerBlockEntities();
        ScreenRegistry.registerScreens();
        RecipeRegistry.registerRecipes();

        // Register server-side packet handlers
        AlchemistryNetwork.registerServerHandlers();
    }
}
