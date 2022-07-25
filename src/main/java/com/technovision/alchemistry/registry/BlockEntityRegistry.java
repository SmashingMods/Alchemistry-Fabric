package com.technovision.alchemistry.registry;

import com.technovision.alchemistry.Alchemistry;
import com.technovision.alchemistry.common.block.dissolver.DissolverBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockEntityRegistry {

    public static final BlockEntityType<DissolverBlockEntity> DISSOLVER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(DissolverBlockEntity::new, BlockRegistry.DISSOLVER).build(null);

    public static void registerBlockEntities() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Alchemistry.MOD_ID, "dissolver_block_entity"), DISSOLVER_BLOCK_ENTITY);
    }
}
