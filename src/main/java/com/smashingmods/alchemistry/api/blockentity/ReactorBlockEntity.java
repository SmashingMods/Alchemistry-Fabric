package com.smashingmods.alchemistry.api.blockentity;

import com.mojang.datafixers.util.Function3;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ReactorBlockEntity {

    ReactorShape getReactorShape();

    void setReactorShape(ReactorShape pReactorShape);

    ReactorType getReactorType();

    void setReactorType(ReactorType pReactorType);

    void setMultiblockHandlers();

    PowerState getPowerState();

    void setPowerState(PowerState pPowerState);

    default Function3<BlockBox, List<Block>, World, Boolean> blockPredicate() {
        return (box, blockList, level) -> BlockPos.stream(box).allMatch(blockPos -> blockList.contains(level.getBlockState(blockPos).getBlock()));
    }

    default boolean validateMultiblockShape(World pLevel, Map<BlockBox, List<Block>> pMap) {
        List<Boolean> checks = new ArrayList<>();
        pMap.forEach((box, list) -> checks.add(blockPredicate().apply(box, list, pLevel)));
        return checks.stream().allMatch(check -> check);
    }

    boolean isValidMultiblock();
}
