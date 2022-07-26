package com.technovision.alchemistry;

import com.technovision.alchemistry.common.block.dissolver.DissolverScreen;
import com.technovision.alchemistry.registry.ScreenRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AlchemistryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register screens
        HandledScreens.register(ScreenRegistry.DISSOLVER_SCREEN_HANDLER, DissolverScreen::new);
    }
}
