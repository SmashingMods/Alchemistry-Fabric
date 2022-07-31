package com.smashingmods.alchemistry.registry;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.atomizer.AtomizerBlockEntity;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverBlockEntity;
import com.smashingmods.alchemistry.common.block.liquifier.LiquifierBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import team.reborn.energy.api.EnergyStorage;

public class BlockEntityRegistry {

    public static final BlockEntityType<DissolverBlockEntity> DISSOLVER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(DissolverBlockEntity::new, BlockRegistry.DISSOLVER).build(null);
    public static final BlockEntityType<LiquifierBlockEntity> LIQUIFIER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(LiquifierBlockEntity::new, BlockRegistry.LIQUIFIER).build(null);
    public static final BlockEntityType<AtomizerBlockEntity> ATOMIZER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(AtomizerBlockEntity::new, BlockRegistry.ATOMIZER).build(null);

    public static void registerBlockEntities() {
        // Register block entity
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Alchemistry.MOD_ID, "dissolver_block_entity"), DISSOLVER_BLOCK_ENTITY);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Alchemistry.MOD_ID, "liquifier_block_entity"), LIQUIFIER_BLOCK_ENTITY);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Alchemistry.MOD_ID, "atomizer_block_entity"), ATOMIZER_BLOCK_ENTITY);

        // Register energy storage for block entity
        EnergyStorage.SIDED.registerForBlockEntity((myBlockEntity, direction) -> myBlockEntity.getEnergyStorage(), DISSOLVER_BLOCK_ENTITY);
        EnergyStorage.SIDED.registerForBlockEntity((myBlockEntity, direction) -> myBlockEntity.getEnergyStorage(), LIQUIFIER_BLOCK_ENTITY);
        EnergyStorage.SIDED.registerForBlockEntity((myBlockEntity, direction) -> myBlockEntity.getEnergyStorage(), ATOMIZER_BLOCK_ENTITY);

        // Register fluid storage for block entity
        FluidStorage.SIDED.registerForBlockEntity((myTank, direction) -> myTank.getFluidStorage(), LIQUIFIER_BLOCK_ENTITY);
        FluidStorage.SIDED.registerForBlockEntity((myTank, direction) -> myTank.getFluidStorage(), ATOMIZER_BLOCK_ENTITY);
    }
}
