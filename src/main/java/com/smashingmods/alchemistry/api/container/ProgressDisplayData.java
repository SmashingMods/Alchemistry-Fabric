package com.smashingmods.alchemistry.api.container;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ProgressDisplayData extends DisplayData {

    private final ContainerData delegate;
    private final Direction2D direction2D;
    private final int valueSlot;
    private final int maxValueSlot;

    public ProgressDisplayData(ContainerData delegate, int valueSLot, int maxValueSlot, int x, int y, int width, int height, Direction2D direction2D) {
        super(x, y, width, height);
        this.delegate = delegate;
        this.direction2D = direction2D;
        this.valueSlot = valueSLot;
        this.maxValueSlot = maxValueSlot;
    }

    @Override
    public int getValue() {
        return delegate.get(valueSlot);
    }

    @Override
    public int getMaxValue() {
        return delegate.get(maxValueSlot);
    }

    public Direction2D getDirection() {
        return direction2D;
    }

    @Override
    public List<Component> toText() {
        return List.of(Component.literal("Show Recipes"));
    }
}
