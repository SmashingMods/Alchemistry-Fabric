package com.smashingmods.alchemistry;

import com.smashingmods.alchemistry.common.block.atomizer.AtomizerScreen;
import com.smashingmods.alchemistry.common.block.combiner.CombinerScreen;
import com.smashingmods.alchemistry.common.block.compactor.CompactorScreen;
import com.smashingmods.alchemistry.common.block.fission.FissionControllerScreen;
import com.smashingmods.alchemistry.common.block.fusion.FusionControllerScreen;
import com.smashingmods.alchemistry.common.block.liquifier.LiquifierScreen;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverScreen;
import com.smashingmods.alchemistry.registry.BlockRegistry;
import com.smashingmods.alchemistry.registry.ScreenRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class AlchemistryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, level, hand) -> {
            if (level.isClientSide() && player.getItemInHand(hand).is(com.smashingmods.alchemistry.registry.ItemRegistry.GUIDE_BOOK)) {
                net.minecraft.client.Minecraft.getInstance().gui.setScreen(new com.smashingmods.alchemistry.client.guide.GuideBookScreen());
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
            return net.minecraft.world.InteractionResult.PASS;
        });
        com.smashingmods.alchemistry.client.guide.ReactorProjection.register();
        // Register screens
        MenuScreens.register(ScreenRegistry.DISSOLVER_SCREEN_HANDLER, DissolverScreen::new);
        MenuScreens.register(ScreenRegistry.LIQUIFIER_SCREEN_HANDLER, LiquifierScreen::new);
        MenuScreens.register(ScreenRegistry.ATOMIZER_SCREEN_HANDLER, AtomizerScreen::new);
        MenuScreens.register(ScreenRegistry.COMPACTOR_SCREEN_HANDLER, CompactorScreen::new);
        MenuScreens.register(ScreenRegistry.COMBINER_SCREEN_HANDLER, CombinerScreen::new);
        MenuScreens.register(ScreenRegistry.FISSION_SCREEN_HANDLER, FissionControllerScreen::new);
        MenuScreens.register(ScreenRegistry.FUSION_SCREEN_HANDLER, FusionControllerScreen::new);

        // Register client-side packet handlers
        com.smashingmods.alchemistry.network.AlchemistryClientNetwork.registerClientHandlers();
    }
}
