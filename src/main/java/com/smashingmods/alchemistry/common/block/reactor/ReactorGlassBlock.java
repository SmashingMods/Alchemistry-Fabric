package com.smashingmods.alchemistry.common.block.reactor;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.Material;

public class ReactorGlassBlock extends AbstractGlassBlock {

    public ReactorGlassBlock() {
        super(FabricBlockSettings.of(Material.GLASS).strength(2.0f));
    }
}
