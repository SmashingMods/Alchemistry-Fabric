package com.smashingmods.alchemistry.api.container;

import net.minecraft.screen.PropertyDelegate;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EnergyDisplayData extends DisplayData {

    private final PropertyDelegate delegate;
    private final int valueSlot;
    private final int maxValueSlot;

    public EnergyDisplayData(PropertyDelegate delegate, int valueSlot, int maxValueSlot, int x, int y, int width, int height) {
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
    public List<Text> toText() {
        NumberFormat numFormat = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        numFormat.setMinimumFractionDigits(0);
        numFormat.setMaximumFractionDigits(1);

        String stored = numFormat.format(getValue()).toLowerCase();
        String capacity = numFormat.format(getMaxValue()).toLowerCase();
        int percent = (int) (((double) getValue() / (double) getMaxValue()) * 100);

        Formatting color;
        if (percent < 11) color = Formatting.RED;
        else if (percent < 75) color = Formatting.YELLOW;
        else color = Formatting.GREEN;

        MutableText line1 = Text.literal(stored + "/" + capacity + " E");
        MutableText line2 = Text.literal(percent + "%").formatted(color).append(Text.literal(" Charged").formatted(Formatting.GRAY));
        return List.of(line1, line2);
    }
}
