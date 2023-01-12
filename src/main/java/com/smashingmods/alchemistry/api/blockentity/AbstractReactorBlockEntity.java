package com.smashingmods.alchemistry.api.blockentity;

import com.smashingmods.alchemistry.common.block.reactor.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;

import java.util.function.Consumer;

public abstract class AbstractReactorBlockEntity extends AbstractInventoryBlockEntity implements ReactorBlockEntity {

    private ReactorShape reactorShape;
    private ReactorType reactorType;

    private ReactorEnergyBlockEntity reactorEnergyBlockEntity;
    private ReactorInputBlockEntity reactorInputBlockEntity;
    private ReactorOutputBlockEntity reactorOutputBlockEntity;

    private boolean energyFound;
    private boolean inputFound;
    private boolean outputFound;

    public AbstractReactorBlockEntity(DefaultedList<ItemStack> inventory, BlockEntityType<?> type, BlockPos pos, BlockState state, long energyCapacity) {
        super(inventory, type, pos, state, energyCapacity);
    }

    @Override
    public void tick() {
        if (world != null && !world.isClient()) {
            if (reactorShape == null) {
                setReactorShape(new ReactorShape(getPos(), getReactorType(), world));
            }

            setMultiblockHandlers();
            if (isValidMultiblock()) {
                switch (getPowerState()) {
                    case ON -> {
                        BlockPos coreCenter = reactorShape.getCoreBoundingBox().getCenter();
                        DustParticleEffect options = new DustParticleEffect(new Vec3f(1f, 1f, 0.5f), 0.15f);
                        ((ServerWorld) world).spawnParticles(options,
                                coreCenter.getX(),
                                coreCenter.getY(),
                                coreCenter.getZ(),
                                50,
                                1.5f,
                                1.5f,
                                1.5f,
                                0f);
                    }
                    case DISABLED -> {
                        if (getEnergyStorage().getAmount() > 0) {
                            setPowerState(PowerState.STANDBY);
                        } else {
                            setPowerState(PowerState.OFF);
                        }
                    }
                }
            } else {
                setPowerState(PowerState.DISABLED);
            }
        }
    }

    @Override
    public ReactorShape getReactorShape() {
        return reactorShape;
    }

    @Override
    public void setReactorShape(ReactorShape reactorShape) {
        this.reactorShape = reactorShape;
    }

    @Override
    public ReactorType getReactorType() {
        return reactorType;
    }

    @Override
    public void setReactorType(ReactorType reactorType) {
        this.reactorType = reactorType;
    }

    @Override
    public void setMultiblockHandlers() {
        if (world != null && !world.isClient()) {
            BlockBox reactorBox = getReactorShape().getFullBoundingBox();

            if (reactorEnergyBlockEntity == null || !energyFound) {
                BlockPos.stream(reactorBox)
                        .filter(blockPos -> world.getBlockEntity(blockPos) instanceof ReactorEnergyBlockEntity)
                        .findFirst()
                        .ifPresent(blockPos -> {
                            BlockState energyState = world.getBlockState(blockPos);
                            world.setBlockState(blockPos, energyState);
                            setEnergyFound(true);
                            reactorEnergyBlockEntity = (ReactorEnergyBlockEntity) world.getBlockEntity(blockPos);
                        });
            } else {
                reactorEnergyBlockEntity.setController(this);
            }

            if (reactorInputBlockEntity == null || !inputFound) {
                BlockPos.stream(reactorBox)
                        .filter(blockPos -> world.getBlockEntity(blockPos) instanceof ReactorInputBlockEntity)
                        .findFirst()
                        .ifPresent(blockPos -> {
                            BlockState inputState = world.getBlockState(blockPos);
                            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 7);
                            world.setBlockState(blockPos, inputState, 7);
                            inputFound = true;
                            reactorInputBlockEntity = (ReactorInputBlockEntity) world.getBlockEntity(blockPos);
                        });
            } else {
                reactorInputBlockEntity.setController(this);
            }

            if (reactorOutputBlockEntity == null || !outputFound) {
                outputFound = false;
                BlockPos.stream(reactorBox)
                        .filter(blockPos -> world.getBlockEntity(blockPos) instanceof ReactorOutputBlockEntity)
                        .findFirst()
                        .ifPresent(blockPos -> {
                            BlockState outputState = world.getBlockState(blockPos);
                            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 7);
                            world.setBlockState(blockPos, outputState, 7);
                            outputFound = true;
                            reactorOutputBlockEntity = (ReactorOutputBlockEntity) world.getBlockEntity(blockPos);
                        });
            } else {
                reactorOutputBlockEntity.setController(this);
            }
        }
    }

    @Override
    public PowerState getPowerState() {
        return getCachedState().get(PowerStateProperty.POWER_STATE);
    }

    @Override
    public void setPowerState(PowerState powerState) {
        if (world != null && !world.isClient()) {
            world.setBlockState(getPos(), getCachedState().with(PowerStateProperty.POWER_STATE, powerState));
        }
    }

    public void setEnergyFound(boolean energyFound) {
        this.energyFound = energyFound;
    }

    public void setInputFound(boolean inputFound) {
        this.inputFound = inputFound;
    }

    public void setOutputFound(boolean outputFound) {
        this.outputFound = outputFound;
    }

    @Override
    public boolean isValidMultiblock() {
        if (world != null && !world.isClient()) {
            Consumer<BlockPos> handleCorePowerState = blockPos -> {
                if (world != null && !world.isClient()) {
                    BlockState blockState = world.getBlockState(blockPos);
                    if (blockState.getBlock() instanceof ReactorCoreBlock) {
                        PowerState coreState = blockState.get(PowerStateProperty.POWER_STATE);
                        switch (getPowerState()) {
                            case DISABLED, OFF -> {
                                if (coreState.equals(PowerState.ON)) {
                                    BlockState newState = blockState.with(PowerStateProperty.POWER_STATE, PowerState.OFF);
                                    world.setBlockState(blockPos, newState, 7);
                                }
                            }
                            case STANDBY, ON -> {
                                if (coreState.equals(PowerState.OFF)) {
                                    BlockState newState = blockState.with(PowerStateProperty.POWER_STATE, PowerState.ON);
                                    world.setBlockState(blockPos, newState, 7);
                                }
                            }
                        }
                    }
                }
            };
            BlockPos.stream(reactorShape.getCoreBoundingBox()).forEach(handleCorePowerState);
            return validateMultiblockShape(world, getReactorShape().createShapeMap()) && energyFound && inputFound && outputFound;
        }
        return false;
    }

    public void resetIO() {
        if (world != null && !world.isClient()) {
            setMultiblockHandlers();
            if (reactorEnergyBlockEntity != null && getBlock(reactorEnergyBlockEntity) instanceof ReactorEnergyBlock) {
                BlockState energyState = reactorEnergyBlockEntity.getCachedState();
                BlockPos energyPos = reactorEnergyBlockEntity.getPos();
                world.setBlockState(energyPos, energyState);
            }
            if (reactorInputBlockEntity != null && getBlock(reactorInputBlockEntity) instanceof ReactorInputBlock) {
                BlockState inputState = reactorInputBlockEntity.getCachedState();
                BlockPos inputPos = reactorInputBlockEntity.getPos();
                world.setBlockState(inputPos, inputState);
            }
            if (reactorOutputBlockEntity != null && getBlock(reactorOutputBlockEntity) instanceof ReactorOutputBlock) {
                BlockState outputState = reactorOutputBlockEntity.getCachedState();
                BlockPos outputPos = reactorOutputBlockEntity.getPos();
                world.setBlockState(outputPos, outputState);
            }
        }
    }
    private Block getBlock(BlockEntity block) {
        return world.getBlockState(block.getPos()).getBlock();
    }

    public void onRemove() {
        if (world != null && !world.isClient()) {
            resetIO();
            BlockPos.stream(reactorShape.getCoreBoundingBox()).forEach(blockPos -> {
                BlockState blockState = world.getBlockState(blockPos);
                if (blockState.getBlock() instanceof ReactorCoreBlock) {
                    BlockState offState = blockState.with(PowerStateProperty.POWER_STATE, PowerState.OFF);
                    world.setBlockState(blockPos, offState);
                }
            });
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (reactorEnergyBlockEntity != null) {
            nbt.put("reactorEnergyPos", blockPosToTag(reactorEnergyBlockEntity.getPos()));
        }
        if (reactorInputBlockEntity != null) {
            nbt.put("reactorInputPos", blockPosToTag(reactorInputBlockEntity.getPos()));
        }
        if (reactorOutputBlockEntity != null) {
            nbt.put("reactorOutputPos", blockPosToTag(reactorOutputBlockEntity.getPos()));
        }
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (world != null && !world.isClient()) {
            if (world.getBlockEntity(blockPosFromTag(nbt.getCompound("reactorEnergyPos"))) instanceof ReactorEnergyBlockEntity blockEntity) {
                reactorEnergyBlockEntity = blockEntity;
                energyFound = true;
            } else {
                energyFound = false;
            }
            if (world.getBlockEntity(blockPosFromTag(nbt.getCompound("reactorInputPos"))) instanceof ReactorInputBlockEntity blockEntity) {
                reactorInputBlockEntity = blockEntity;
                inputFound = true;
            } else {
                inputFound = false;
            }
            if (world.getBlockEntity(blockPosFromTag(nbt.getCompound("reactorOutputPos"))) instanceof ReactorOutputBlockEntity blockEntity) {
                reactorOutputBlockEntity = blockEntity;
                outputFound = true;
            } else {
                outputFound = false;
            }
        }
    }

    private NbtCompound blockPosToTag(BlockPos pBlockPos) {
        NbtCompound tag = new NbtCompound();
        tag.putInt("x", pBlockPos.getX());
        tag.putInt("y", pBlockPos.getY());
        tag.putInt("z", pBlockPos.getZ());
        return tag;
    }

    private BlockPos blockPosFromTag(NbtCompound pTag) {
        return new BlockPos(pTag.getInt("x"), pTag.getInt("y"), pTag.getInt("z"));
    }
}
