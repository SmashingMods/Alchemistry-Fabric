package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import team.reborn.energy.api.EnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.Nullable;

public class ReactorEnergyBlockEntity extends BlockEntity {

    @Nullable
    private AbstractReactorBlockEntity controller;
    private final EnergyStorage energyStorage = new EnergyStorage() {
        private EnergyStorage backing() {
            return !isRemoved() && controller != null && !controller.isRemoved()
                    && level != null && controller.getLevel() == level
                    ? controller.getEnergyStorage() : EnergyStorage.EMPTY;
        }

        @Override public boolean supportsInsertion() { return backing().supportsInsertion(); }
        @Override public boolean supportsExtraction() { return backing().supportsExtraction(); }
        @Override public long insert(long amount, TransactionContext transaction) { return backing().insert(amount, transaction); }
        @Override public long extract(long amount, TransactionContext transaction) { return backing().extract(amount, transaction); }
        @Override public long getAmount() { return backing().getAmount(); }
        @Override public long getCapacity() { return backing().getCapacity(); }
    };

    public ReactorEnergyBlockEntity(BlockPos worldPosition, BlockState state) {
        super(BlockEntityRegistry.REACTOR_ENERGY_BLOCK_ENTITY, worldPosition, state);
    }

    @Nullable
    public AbstractReactorBlockEntity getController() {
        return controller;
    }

    public void setController(@Nullable AbstractReactorBlockEntity controller) {
        this.controller = controller;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }


}
