package com.smashingmods.alchemistry.api.container;

import com.smashingmods.alchemistry.api.blockentity.AbstractFluidBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.AbstractProcessingBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.fluid.Fluids;
import net.minecraft.screen.PropertyDelegate;

import java.text.NumberFormat;
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

    public SingleVariantStorage<FluidVariant> getFluidHandler() {
        return blockEntity.getFluidStorage();
    }

    @Override
    public String toString() {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        FluidVariant fluidVariant = getFluidHandler().getResource();

        boolean emptyFluid = fluidVariant.isOf(Fluids.EMPTY);

        String fluidName = emptyFluid ? "" : String.format(" %s", fluidVariant.getFluid().getBucketItem().getName());
        String stored = numberFormat.format(getValue());
        String capacity = numberFormat.format(getMaxValue());
        return String.format("%s/%s mb%s", stored, capacity, fluidName);
    }
}
