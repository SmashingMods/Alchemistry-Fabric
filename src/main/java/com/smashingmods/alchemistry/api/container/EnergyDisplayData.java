package com.smashingmods.alchemistry.api.container;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EnergyDisplayData extends DisplayData {

    private final ContainerData delegate;
    private final int valueSlot;
    private final int maxValueSlot;

    public EnergyDisplayData(ContainerData delegate, int valueSlot, int maxValueSlot, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.delegate = delegate;
        this.valueSlot = valueSlot;
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

    @Override
    public List<Component> toText() {
        NumberFormat numFormat = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        numFormat.setMinimumFractionDigits(0);
        numFormat.setMaximumFractionDigits(1);

        String stored = numFormat.format(getValue()).toLowerCase();
        String capacity = numFormat.format(getMaxValue()).toLowerCase();
        int percent = (int) (((double) getValue() / (double) getMaxValue()) * 100);

        ChatFormatting color;
        if (percent < 11) color = ChatFormatting.RED;
        else if (percent < 75) color = ChatFormatting.YELLOW;
        else color = ChatFormatting.GREEN;

        MutableComponent line1 = Component.literal(stored + "/" + capacity + " E");
        MutableComponent line2 = Component.literal(percent + "%").withStyle(color).append(Component.literal(" Charged").withStyle(ChatFormatting.GRAY));
        return List.of(line1, line2);
    }
}
