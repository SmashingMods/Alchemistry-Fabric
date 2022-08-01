package com.smashingmods.alchemistry.common.block.fusion;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.ReactorType;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class FusionControllerBlockEntity extends AbstractReactorBlockEntity {

    public static final int INVENTORY_SIZE = 3;

    protected final PropertyDelegate propertyDelegate;
    private final int maxProgress;

    public FusionControllerBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.FUSION_CONTROLLER_BLOCK_ENTITY, pos, state, Config.Common.fusionEnergyCapacity.get());
        setReactorType(ReactorType.FUSION);
        this.maxProgress = Config.Common.fusionTicksPerOperation.get();
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> getProgress();
                    case 1 -> maxProgress;
                    case 2 -> (int) getEnergyStorage().getAmount();
                    case 3 -> (int) getEnergyStorage().getCapacity();
                    default -> 0;
                };
            }
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> setProgress(value);
                    case 2 -> insertEnergy(value);
                }
            }
            public int size() {
                return 4;
            }
        };
    }

    @Override
    public void updateRecipe() {
        // TODO: Implement
    }

    @Override
    public boolean canProcessRecipe() {
        // TODO: Implement
        return false;
    }

    @Override
    public void processRecipe() {
        // TODO: Implement
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T pRecipe) {
        // TODO: Implement
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        // TODO: Implement
        return null;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        // TODO: Implement
        return null;
    }
}
