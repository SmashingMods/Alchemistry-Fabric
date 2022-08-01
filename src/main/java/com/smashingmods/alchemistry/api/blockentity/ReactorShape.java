package com.smashingmods.alchemistry.api.blockentity;

import com.smashingmods.alchemistry.registry.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactorShape {

    private final ReactorType reactorType;

    private final BlockBox fullBoundingBox;
    private final BlockBox core;
    private final BlockBox innerFrontPlane;
    private final BlockBox innerRearPlane;
    private final BlockBox innerTopPlane;
    private final BlockBox innerBottomPlane;
    private final BlockBox innerLeftPlane;
    private final BlockBox innerRightPlane;

    private final BlockBox frontTopBorder;
    private final BlockBox frontBottomBorder;
    private final BlockBox leftTopBorder;
    private final BlockBox leftBottomBorder;
    private final BlockBox rightTopBorder;
    private final BlockBox rightBottomBorder;
    private final BlockBox rearTopBorder;
    private final BlockBox rearBottomBorder;
    private final BlockBox frontLeftCornerBorder;
    private final BlockBox frontRightCornerBorder;
    private final BlockBox rearLeftCornerBorder;
    private final BlockBox rearRightCornerBorder;

    public ReactorShape(BlockPos pBlockPos, ReactorType pReactorType, World pLevel) {

        reactorType = pReactorType;

        Direction facing = pLevel.getBlockState(pBlockPos).get(Properties.HORIZONTAL_FACING);
        Direction oppositeFacing = facing.getOpposite();
        Direction rightFacing = facing.rotateYCounterclockwise();
        Direction leftFacing = facing.rotateYClockwise();

        BlockPos coreBottom = pBlockPos.offset(oppositeFacing, 2);
        BlockPos coreTop = coreBottom.offset(Direction.UP, 2);
        core = fromCorners(coreBottom, coreTop);

        BlockPos frontTopRight = pBlockPos.offset(Direction.UP, 3).offset(rightFacing, 2);
        BlockPos frontTopLeft = pBlockPos.offset(Direction.UP, 3).offset(leftFacing, 2);
        BlockPos frontBottomRight = pBlockPos.offset(Direction.DOWN, 1).offset(rightFacing, 2);
        BlockPos frontBottomLeft = pBlockPos.offset(Direction.DOWN, 1).offset(leftFacing, 2);
        BlockPos rearTopRight = pBlockPos.offset(Direction.UP, 3).offset(oppositeFacing, 4).offset(rightFacing, 2);
        BlockPos rearTopLeft = pBlockPos.offset(Direction.UP, 3).offset(oppositeFacing, 4).offset(leftFacing, 2);
        BlockPos rearBottomRight = pBlockPos.offset(Direction.DOWN, 1).offset(oppositeFacing, 4).offset(rightFacing, 2);
        BlockPos rearBottomLeft = pBlockPos.offset(Direction.DOWN, 1).offset(oppositeFacing, 4).offset(leftFacing, 2);

        fullBoundingBox = fromCorners(frontTopLeft, rearBottomRight);
        frontTopBorder = fromCorners(frontTopRight, frontTopLeft);
        frontBottomBorder = fromCorners(frontBottomRight, frontBottomLeft);
        leftTopBorder = fromCorners(frontTopLeft, rearTopLeft);
        leftBottomBorder = fromCorners(frontBottomLeft, rearBottomLeft);
        rightTopBorder = fromCorners(frontTopRight, rearTopRight);
        rightBottomBorder = fromCorners(frontBottomRight, rearBottomRight);
        rearTopBorder = fromCorners(rearTopRight, rearTopLeft);
        rearBottomBorder = fromCorners(rearBottomRight, rearBottomLeft);
        frontLeftCornerBorder = fromCorners(frontTopLeft, frontBottomLeft);
        frontRightCornerBorder = fromCorners(frontTopRight, frontBottomRight);
        rearLeftCornerBorder = fromCorners(rearTopLeft, rearBottomLeft);
        rearRightCornerBorder = fromCorners(rearTopRight, rearBottomRight);

        BlockPos innerFrontTopClockwise = pBlockPos.offset(Direction.UP, 3).offset(oppositeFacing, 1).offset(rightFacing, 1);
        BlockPos innerRearTopCounterClockwise = pBlockPos.offset(Direction.UP, 3).offset(oppositeFacing, 3).offset(leftFacing, 1);
        innerTopPlane = fromCorners(innerFrontTopClockwise, innerRearTopCounterClockwise);

        BlockPos innerFrontBottomRight = pBlockPos.offset(Direction.DOWN, 1).offset(oppositeFacing, 1).offset(rightFacing, 1);
        BlockPos innerRearBottomLeft = pBlockPos.offset(Direction.DOWN, 1).offset(oppositeFacing, 3).offset(leftFacing, 1);
        innerBottomPlane = fromCorners(innerFrontBottomRight, innerRearBottomLeft);

        BlockPos innerLeftFrontTop = pBlockPos.offset(Direction.UP, 2).offset(oppositeFacing, 1).offset(leftFacing, 2);
        BlockPos innerLeftRearBottom = pBlockPos.offset(oppositeFacing, 3).offset(leftFacing, 2);
        innerLeftPlane = fromCorners(innerLeftFrontTop, innerLeftRearBottom);

        BlockPos innerRightFrontTop = pBlockPos.offset(Direction.UP, 2).offset(oppositeFacing, 1).offset(rightFacing, 2);
        BlockPos innerRightRearBottom = pBlockPos.offset(oppositeFacing, 3).offset(rightFacing, 2);
        innerRightPlane = fromCorners(innerRightFrontTop, innerRightRearBottom);

        BlockPos innerFrontLeftTop = pBlockPos.offset(Direction.UP, 2).offset(leftFacing, 1);
        BlockPos innerFrontRightBottom = pBlockPos.offset(rightFacing, 1);
        innerFrontPlane = fromCorners(innerFrontLeftTop, innerFrontRightBottom);

        BlockPos innerRearLeftTop = pBlockPos.offset(Direction.UP, 2).offset(oppositeFacing, 4).offset(leftFacing, 1);
        BlockPos innerRearRightBottom = pBlockPos.offset(oppositeFacing, 4).offset(rightFacing, 1);
        innerRearPlane = fromCorners(innerRearLeftTop, innerRearRightBottom);
    }

    public Map<BlockBox, List<Block>> createShapeMap() {
        Map<BlockBox, List<Block>> reactorShapeMap = new HashMap<>();

        Block coreComponent = switch (reactorType) {
            case FUSION -> BlockRegistry.FUSION_CORE;
            case FISSION -> BlockRegistry.FISSION_CORE;
        };

        Block controllerComponent = switch(reactorType) {
            case FUSION -> BlockRegistry.FUSION_CONTROLLER;
            case FISSION -> BlockRegistry.FISSION_CONTROLLER;
        };

        reactorShapeMap.put(core, List.of(coreComponent));

        reactorShapeMap.put(frontTopBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(frontBottomBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(rearTopBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(rearBottomBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(leftTopBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(leftBottomBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(rightTopBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(rightBottomBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(frontLeftCornerBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(frontRightCornerBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(rearLeftCornerBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));
        reactorShapeMap.put(rearRightCornerBorder, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_ENERGY, BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT));

        reactorShapeMap.put(innerTopPlane, List.of(BlockRegistry.REACTOR_CASING));
        reactorShapeMap.put(innerBottomPlane, List.of(BlockRegistry.REACTOR_CASING));
        reactorShapeMap.put(innerFrontPlane, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_GLASS, controllerComponent));
        reactorShapeMap.put(innerRearPlane, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_GLASS));
        reactorShapeMap.put(innerLeftPlane, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_GLASS));
        reactorShapeMap.put(innerRightPlane, List.of(BlockRegistry.REACTOR_CASING, BlockRegistry.REACTOR_GLASS));

        return reactorShapeMap;
    }

    private static BlockBox fromCorners(BlockPos pStart, BlockPos pEnd) {
        return BlockBox.create(blockPosToVec3i(pStart), blockPosToVec3i(pEnd));
    }

    private static Vec3i blockPosToVec3i(BlockPos pBlockPos) {
        return new Vec3i(pBlockPos.getX(), pBlockPos.getY(), pBlockPos.getZ());
    }

    public BlockBox getFullBoundingBox() {
        return fullBoundingBox;
    }

    public BlockBox getCoreBoundingBox() {
        return core;
    }
}
