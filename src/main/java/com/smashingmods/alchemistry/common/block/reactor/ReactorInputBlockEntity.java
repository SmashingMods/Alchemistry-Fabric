package com.smashingmods.alchemistry.common.block.reactor;

import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.ImplementedInventory;
import com.smashingmods.alchemistry.api.blockentity.ReactorType;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

public class ReactorInputBlockEntity extends BlockEntity implements ImplementedInventory {

    @Nullable
    private AbstractReactorBlockEntity controller;
    private final DefaultedList<ItemStack> tempInv;

    public ReactorInputBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.REACTOR_INPUT_BLOCK_ENTITY, pos, state);
        tempInv = DefaultedList.ofSize(1, ItemStack.EMPTY);
    }

    @Nullable
    public AbstractReactorBlockEntity getController() {
        return controller;
    }

    public void setController(@Nullable AbstractReactorBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @org.jetbrains.annotations.Nullable Direction side) {
        if (controller != null) {
            if (controller.getReactorType() == ReactorType.FISSION) {

                if (controller.isRecipeLocked() && controller.getRecipe() != null) {
                    // there is only one input slot in recipe no matter what
                    boolean validInsert = controller.getRecipe().getIngredients().get(0).test(stack);
                    return validInsert && slot == 0;
                }
                return slot == 0;
            } else if (controller.getReactorType() == ReactorType.FUSION) {
                if (controller.isRecipeLocked() && controller.getRecipe() != null) {
                    int validInsert = 0;
                    for (int i = 0; i < controller.getRecipe().getIngredients().size(); i++) {
                        if (controller.getRecipe().getIngredients().get(i).test(stack)) {
                            validInsert++;
                        }
                    }
                    int totalItems = 0;
                    ItemStack item;
                    for (int i = 0; i < getItems().size(); i++) {
                        item = getItems().get(i);
                        totalItems += item.isOf(stack.getItem()) ? item.getCount() : 0;
                    }
                    return slot < 2 && totalItems < validInsert * stack.getMaxCount();
                }
                return slot != 2;
            }
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return (controller != null) ? controller.getItems() : tempInv;
    }
}
