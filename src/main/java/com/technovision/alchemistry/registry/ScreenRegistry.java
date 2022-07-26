package com.technovision.alchemistry.registry;

import com.technovision.alchemistry.Alchemistry;
import com.technovision.alchemistry.common.block.dissolver.DissolverScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ScreenRegistry {

    public static ScreenHandlerType<DissolverScreenHandler> DISSOLVER_SCREEN_HANDLER = new ScreenHandlerType<>(DissolverScreenHandler::new);

    public static void registerScreens() {
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "dissolver_menu"), DISSOLVER_SCREEN_HANDLER);
    }
}
