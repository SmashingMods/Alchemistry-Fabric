package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.atomizer.AtomizerScreenHandler;
import com.smashingmods.alchemistry.common.block.combiner.CombinerScreenHandler;
import com.smashingmods.alchemistry.common.block.compactor.CompactorScreenHandler;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverScreenHandler;
import com.smashingmods.alchemistry.common.block.fission.FissionControllerScreenHandler;
import com.smashingmods.alchemistry.common.block.fusion.FusionControllerScreenHandler;
import com.smashingmods.alchemistry.common.block.liquifier.LiquifierScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ScreenRegistry {

    public static ExtendedMenuType<DissolverScreenHandler, BlockPos> DISSOLVER_SCREEN_HANDLER = new ExtendedMenuType<>(DissolverScreenHandler::new, BlockPos.STREAM_CODEC);
    public static ExtendedMenuType<LiquifierScreenHandler, BlockPos> LIQUIFIER_SCREEN_HANDLER = new ExtendedMenuType<>(LiquifierScreenHandler::new, BlockPos.STREAM_CODEC);
    public static ExtendedMenuType<AtomizerScreenHandler, BlockPos> ATOMIZER_SCREEN_HANDLER = new ExtendedMenuType<>(AtomizerScreenHandler::new, BlockPos.STREAM_CODEC);
    public static ExtendedMenuType<CompactorScreenHandler, BlockPos> COMPACTOR_SCREEN_HANDLER = new ExtendedMenuType<>(CompactorScreenHandler::new, BlockPos.STREAM_CODEC);
    public static ExtendedMenuType<CombinerScreenHandler, BlockPos> COMBINER_SCREEN_HANDLER = new ExtendedMenuType<>(CombinerScreenHandler::new, BlockPos.STREAM_CODEC);
    public static ExtendedMenuType<FissionControllerScreenHandler, BlockPos> FISSION_SCREEN_HANDLER = new ExtendedMenuType<>(FissionControllerScreenHandler::new, BlockPos.STREAM_CODEC);
    public static ExtendedMenuType<FusionControllerScreenHandler, BlockPos> FUSION_SCREEN_HANDLER = new ExtendedMenuType<>(FusionControllerScreenHandler::new, BlockPos.STREAM_CODEC);

    public static void registerScreens() {
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "dissolver_menu"), DISSOLVER_SCREEN_HANDLER);
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "liquifier_menu"), LIQUIFIER_SCREEN_HANDLER);
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "atomizer_menu"), ATOMIZER_SCREEN_HANDLER);
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "compactor_menu"), COMPACTOR_SCREEN_HANDLER);
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "combiner_menu"), COMBINER_SCREEN_HANDLER);
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "fission_controller_menu"), FISSION_SCREEN_HANDLER);
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "fusion_controller_menu"), FUSION_SCREEN_HANDLER);
    }
}
