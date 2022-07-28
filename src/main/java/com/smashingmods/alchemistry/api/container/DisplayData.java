package com.smashingmods.alchemistry.api.container;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public abstract class DisplayData implements IDisplayData {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public DisplayData(int pX, int pY, int pWidth, int pHeight) {
        this.x = pX;
        this.y = pY;
        this.width = pWidth;
        this.height = pHeight;
    }

    public MutableText toTextComponent() {
        String temp = "";
        if (this.toString() != null) {
            temp = this.toString();
        }
        return Text.literal(temp);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
