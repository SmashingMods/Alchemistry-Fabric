package com.smashingmods.alchemistry.api.blockentity;

import com.smashingmods.alchemistry.common.block.reactor.ReactorEnergyBlockEntity;
import com.smashingmods.alchemistry.common.block.reactor.ReactorInputBlockEntity;
import com.smashingmods.alchemistry.common.block.reactor.ReactorOutputBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

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
        // TODO: Implement
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
        // TODO: Implement
        return false;
    }
}
