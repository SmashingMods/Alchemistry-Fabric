package com.smashingmods.alchemistry.api.container;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

public interface IDisplayData {

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    int getValue();

    int getMaxValue();

    List<Text> toText();
}
