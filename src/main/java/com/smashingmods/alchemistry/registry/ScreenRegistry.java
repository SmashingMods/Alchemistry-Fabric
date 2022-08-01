package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.atomizer.AtomizerScreenHandler;
import com.smashingmods.alchemistry.common.block.combiner.CombinerScreenHandler;
import com.smashingmods.alchemistry.common.block.compactor.CompactorScreenHandler;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverScreenHandler;
import com.smashingmods.alchemistry.common.block.fission.FissionControllerScreenHandler;
import com.smashingmods.alchemistry.common.block.fusion.FusionControllerScreenHandler;
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
    public static ExtendedScreenHandlerType<FissionControllerScreenHandler> FISSION_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(FissionControllerScreenHandler::new);
    public static ExtendedScreenHandlerType<FusionControllerScreenHandler> FUSION_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(FusionControllerScreenHandler::new);

    public static void registerScreens() {
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "dissolver_menu"), DISSOLVER_SCREEN_HANDLER);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "liquifier_menu"), LIQUIFIER_SCREEN_HANDLER);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "atomizer_menu"), ATOMIZER_SCREEN_HANDLER);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "compactor_menu"), COMPACTOR_SCREEN_HANDLER);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "combiner_menu"), COMBINER_SCREEN_HANDLER);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "fission_controller_menu"), FISSION_SCREEN_HANDLER);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(Alchemistry.MOD_ID, "fusion_controller_menu"), FUSION_SCREEN_HANDLER);
    }
}
