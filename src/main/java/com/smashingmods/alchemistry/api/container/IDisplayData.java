package com.smashingmods.alchemistry.api.container;

import net.minecraft.text.MutableText;

public interface IDisplayData {

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    int getValue();

    int getMaxValue();

    MutableText toText();
}
