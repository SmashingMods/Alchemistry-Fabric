package com.smashingmods.alchemistry;

import com.smashingmods.alchemistry.common.block.atomizer.AtomizerScreen;
import com.smashingmods.alchemistry.common.block.combiner.CombinerScreen;
import com.smashingmods.alchemistry.common.block.compactor.CompactorScreen;
import com.smashingmods.alchemistry.common.block.fission.FissionScreen;
import com.smashingmods.alchemistry.common.block.liquifier.LiquifierScreen;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverScreen;
import com.smashingmods.alchemistry.registry.BlockRegistry;
import com.smashingmods.alchemistry.registry.ScreenRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;

public class AlchemistryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register screens
        HandledScreens.register(ScreenRegistry.DISSOLVER_SCREEN_HANDLER, DissolverScreen::new);
        HandledScreens.register(ScreenRegistry.LIQUIFIER_SCREEN_HANDLER, LiquifierScreen::new);
        HandledScreens.register(ScreenRegistry.ATOMIZER_SCREEN_HANDLER, AtomizerScreen::new);
        HandledScreens.register(ScreenRegistry.COMPACTOR_SCREEN_HANDLER, CompactorScreen::new);
        HandledScreens.register(ScreenRegistry.COMBINER_SCREEN_HANDLER, CombinerScreen::new);
        HandledScreens.register(ScreenRegistry.FISSION_SCREEN_HANDLER, FissionScreen::new);

        // Make glass block translucent
        BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.REACTOR_GLASS, RenderLayer.getTranslucent());

        // Register client-side packet handlers
        AlchemistryNetwork.registerClientHandlers();
    }
}
