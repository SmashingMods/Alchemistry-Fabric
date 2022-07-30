package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import team.reborn.energy.api.EnergyStorage;

public class BlockEntityRegistry {

    public static final BlockEntityType<DissolverBlockEntity> DISSOLVER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(DissolverBlockEntity::new, BlockRegistry.DISSOLVER).build(null);

    public static void registerBlockEntities() {
        // Register block entity
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Alchemistry.MOD_ID, "dissolver_block_entity"), DISSOLVER_BLOCK_ENTITY);

        // Register energy storage for block entity
        EnergyStorage.SIDED.registerForBlockEntity((myBlockEntity, direction) -> myBlockEntity.getEnergyStorage(), DISSOLVER_BLOCK_ENTITY);
    }
}
