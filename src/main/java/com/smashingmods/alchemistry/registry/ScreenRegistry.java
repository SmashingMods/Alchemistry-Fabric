package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.atomizer.AtomizerScreenHandler;
import com.smashingmods.alchemistry.common.block.combiner.CombinerScreenHandler;
import com.smashingmods.alchemistry.common.block.compactor.CompactorScreenHandler;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverScreenHandler;
import com.smashingmods.alchemistry.common.block.liquifier.LiquifierScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ScreenRegistry {

    public static ExtendedScreenHandlerType<DissolverScreenHandler> DISSOLVER_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(DissolverScreenHandler::new);
    public static ExtendedScreenHandlerType<LiquifierScreenHandler> LIQUIFIER_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(LiquifierScreenHandler::new);
    public static ExtendedScreenHandlerType<AtomizerScreenHandler> ATOMIZER_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(AtomizerScreenHandler::new);
    public static ExtendedScreenHandlerType<CompactorScreenHandler> COMPACTOR_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(CompactorScreenHandler::new);
    public static ExtendedScreenHandlerType<CombinerScreenHandler> COMBINER_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(CombinerScreenHandler::new);

    public static void registerScreens() {
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "dissolver_menu"), DISSOLVER_SCREEN_HANDLER);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "liquifier_menu"), LIQUIFIER_SCREEN_HANDLER);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "atomizer_menu"), ATOMIZER_SCREEN_HANDLER);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "compactor_menu"), COMPACTOR_SCREEN_HANDLER);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "combiner_menu"), COMBINER_SCREEN_HANDLER);
    }
}
