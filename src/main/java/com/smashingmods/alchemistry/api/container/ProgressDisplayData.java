package com.smashingmods.alchemistry.api.container;

import net.minecraft.screen.PropertyDelegate;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

public class ProgressDisplayData extends DisplayData {

    private final PropertyDelegate delegate;
    private final Direction2D direction2D;
    private final int valueSlot;
    private final int maxValueSlot;

    public ProgressDisplayData(PropertyDelegate delegate, int valueSLot, int maxValueSlot, int x, int y, int width, int height, Direction2D direction2D) {
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
    public List<Text> toText() {
        return List.of(Text.literal("Show Recipes"));
    }
}
