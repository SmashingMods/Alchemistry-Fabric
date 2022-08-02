package com.smashingmods.alchemistry.api.container;

import com.smashingmods.alchemistry.api.blockentity.AbstractFluidBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.AbstractProcessingBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluids;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FluidDisplayData extends DisplayData {

    private final AbstractFluidBlockEntity blockEntity;
    private final PropertyDelegate data;
    private final int valueSlot;
    private final int maxValueSlot;

    public FluidDisplayData(AbstractProcessingBlockEntity blockEntity, PropertyDelegate data, int valueSlot, int maxValueSlot, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.blockEntity = (AbstractFluidBlockEntity) blockEntity;
        this.data = data;
        this.valueSlot = valueSlot;
        this.maxValueSlot = maxValueSlot;
    }

    @Override
    public int getValue() {
        return data.get(valueSlot);
    }

    @Override
    public int getMaxValue() {
        return data.get(maxValueSlot);
    }

    public FluidVariant getFluidVariant() {
        return FluidVariant.of(Registry.FLUID.get(data.get(6)));
    }

    @Override
    public List<Text> toText() {
        NumberFormat numFormat = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        numFormat.setMinimumFractionDigits(0);
        numFormat.setMaximumFractionDigits(1);

        FluidVariant fluidVariant = getFluidVariant();
        String fluidName = "";
        String[] splitted = Registry.ITEM.getId(fluidVariant.getFluid().getBucketItem()).getPath().split("_");
        for (int i = 0; i < splitted.length-1; i++) {
            fluidName += (splitted[i].charAt(0)+"").toUpperCase() + splitted[i].substring(1) + " ";
        }
        String stored = numFormat.format(getValue()/81).toLowerCase();;
        String capacity = numFormat.format(getMaxValue()/81).toLowerCase();;

        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.literal(String.format("%s/%s mb", stored, capacity)));
        if (!fluidVariant.isOf(Fluids.EMPTY)) tooltip.add(Text.literal(fluidName).formatted(Formatting.GRAY));
        return tooltip;
    }
}
