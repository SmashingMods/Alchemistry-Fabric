package com.smashingmods.alchemistry.api.blockentity;

import com.smashingmods.alchemistry.common.block.reactor.ReactorCoreBlock;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.minecraft.core.Direction;
import com.smashingmods.alchemistry.common.block.reactor.ReactorEnergyBlockEntity;
import com.smashingmods.alchemistry.common.block.reactor.ReactorInputBlockEntity;
import com.smashingmods.alchemistry.common.block.reactor.ReactorOutputBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;

import java.util.function.Consumer;

public abstract class AbstractReactorBlockEntity extends AbstractInventoryBlockEntity implements ReactorBlockEntity {

    // Keep one adapter alive so ports and direct controller access share its slot snapshots.
    private final ContainerStorage itemStorage = ContainerStorage.of(this, Direction.UP);

    public ContainerStorage getItemStorage() {
        return itemStorage;
    }

    private ReactorShape reactorShape;
    private ReactorType reactorType;

    private ReactorEnergyBlockEntity reactorEnergyBlockEntity;
    private ReactorInputBlockEntity reactorInputBlockEntity;
    private ReactorOutputBlockEntity reactorOutputBlockEntity;

    private boolean energyFound;
    private boolean inputFound;
    private boolean outputFound;

    public AbstractReactorBlockEntity(NonNullList<ItemStack> inventory, BlockEntityType<?> type, BlockPos worldPosition, BlockState state, long energyCapacity) {
        super(inventory, type, worldPosition, state, energyCapacity);
    }

    @Override
    public void tick() {
        if (level != null && !level.isClientSide()) {
            refreshRecipe();
            if (reactorShape == null) {
                setReactorShape(new ReactorShape(getBlockPos(), getReactorType(), level));
            }

            setMultiblockHandlers();
            if (isValidMultiblock()) {
                switch (getPowerState()) {
                    case ON -> {
                        BlockPos coreCenter = reactorShape.getCoreBoundingBox().getCenter();
                        DustParticleOptions options = new DustParticleOptions(0xFFFF80, 0.15f);
                        ((ServerLevel) level).sendParticles(options,
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
        if (level == null || level.isClientSide() || reactorShape == null) return;
        reactorEnergyBlockEntity = null; reactorInputBlockEntity = null; reactorOutputBlockEntity = null;
        BlockPos.betweenClosedStream(reactorShape.getFullBoundingBox()).forEach(pos -> {
            var entity = level.getBlockEntity(pos);
            if (entity instanceof ReactorEnergyBlockEntity energy) { reactorEnergyBlockEntity = energy; energy.setController(this); }
            if (entity instanceof ReactorInputBlockEntity input) { reactorInputBlockEntity = input; input.setController(this); }
            if (entity instanceof ReactorOutputBlockEntity output) { reactorOutputBlockEntity = output; output.setController(this); }
        });
        energyFound = reactorEnergyBlockEntity != null;
        inputFound = reactorInputBlockEntity != null;
        outputFound = reactorOutputBlockEntity != null;
    }

    @Override
    public PowerState getPowerState() {
        return getBlockState().getValue(PowerStateProperty.POWER_STATE);
    }

    @Override
    public void setPowerState(PowerState powerState) {
        if (level != null && !level.isClientSide()) {
            level.setBlock(getBlockPos(), getBlockState().setValue(PowerStateProperty.POWER_STATE, powerState), 3);
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
        if (level != null && !level.isClientSide()) {
            Consumer<BlockPos> handleCorePowerState = blockPos -> {
                if (level != null && !level.isClientSide()) {
                    BlockState blockState = level.getBlockState(blockPos);
                    if (blockState.getBlock() instanceof ReactorCoreBlock) {
                        PowerState coreState = blockState.getValue(PowerStateProperty.POWER_STATE);
                        switch (getPowerState()) {
                            case DISABLED, OFF -> {
                                if (coreState.equals(PowerState.ON)) {
                                    BlockState newState = blockState.setValue(PowerStateProperty.POWER_STATE, PowerState.OFF);
                                    level.setBlock(blockPos, newState, 7);
                                }
                            }
                            case STANDBY, ON -> {
                                if (coreState.equals(PowerState.OFF)) {
                                    BlockState newState = blockState.setValue(PowerStateProperty.POWER_STATE, PowerState.ON);
                                    level.setBlock(blockPos, newState, 7);
                                }
                            }
                        }
                    }
                }
            };
            BlockPos.betweenClosedStream(reactorShape.getCoreBoundingBox()).forEach(handleCorePowerState);
            return validateMultiblockShape(level, getReactorShape().createShapeMap()) && energyFound && inputFound && outputFound;
        }
        return false;
    }

    public void resetIO() {
        if (level == null || reactorShape == null) return;
        BlockPos.betweenClosedStream(reactorShape.getFullBoundingBox()).forEach(pos -> {
            var entity = level.getBlockEntity(pos);
            if (entity instanceof ReactorEnergyBlockEntity energy && energy.getController() == this) energy.setController(null);
            if (entity instanceof ReactorInputBlockEntity input && input.getController() == this) input.setController(null);
            if (entity instanceof ReactorOutputBlockEntity output && output.getController() == this) output.setController(null);
        });
    }

    public void onRemove() {
        if (level != null && !level.isClientSide() && reactorShape != null) {
            resetIO();
            BlockPos.betweenClosedStream(reactorShape.getCoreBoundingBox()).forEach(blockPos -> {
                BlockState blockState = level.getBlockState(blockPos);
                if (blockState.getBlock() instanceof ReactorCoreBlock) {
                    BlockState offState = blockState.setValue(PowerStateProperty.POWER_STATE, PowerState.OFF);
                    level.setBlock(blockPos, offState, 3);
                }
            });
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput nbt) {
        if (reactorEnergyBlockEntity != null) {
            nbt.store("reactorEnergyPos", CompoundTag.CODEC, blockPosToTag(reactorEnergyBlockEntity.getBlockPos()));
        }
        if (reactorInputBlockEntity != null) {
            nbt.store("reactorInputPos", CompoundTag.CODEC, blockPosToTag(reactorInputBlockEntity.getBlockPos()));
        }
        if (reactorOutputBlockEntity != null) {
            nbt.store("reactorOutputPos", CompoundTag.CODEC, blockPosToTag(reactorOutputBlockEntity.getBlockPos()));
        }
        super.saveAdditional(nbt);
    }

    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput nbt) {
        super.loadAdditional(nbt);
        if (level != null && !level.isClientSide()) {
            if (level.getBlockEntity(blockPosFromTag(nbt.read("reactorEnergyPos", CompoundTag.CODEC).orElseGet(CompoundTag::new))) instanceof ReactorEnergyBlockEntity blockEntity) {
                reactorEnergyBlockEntity = blockEntity;
                energyFound = true;
            } else {
                energyFound = false;
            }
            if (level.getBlockEntity(blockPosFromTag(nbt.read("reactorInputPos", CompoundTag.CODEC).orElseGet(CompoundTag::new))) instanceof ReactorInputBlockEntity blockEntity) {
                reactorInputBlockEntity = blockEntity;
                inputFound = true;
            } else {
                inputFound = false;
            }
            if (level.getBlockEntity(blockPosFromTag(nbt.read("reactorOutputPos", CompoundTag.CODEC).orElseGet(CompoundTag::new))) instanceof ReactorOutputBlockEntity blockEntity) {
                reactorOutputBlockEntity = blockEntity;
                outputFound = true;
            } else {
                outputFound = false;
            }
        }
    }

    private CompoundTag blockPosToTag(BlockPos pBlockPos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pBlockPos.getX());
        tag.putInt("y", pBlockPos.getY());
        tag.putInt("z", pBlockPos.getZ());
        return tag;
    }

    private BlockPos blockPosFromTag(CompoundTag pTag) {
        return new BlockPos(pTag.getIntOr("x", 0), pTag.getIntOr("y", 0), pTag.getIntOr("z", 0));
    }
}
