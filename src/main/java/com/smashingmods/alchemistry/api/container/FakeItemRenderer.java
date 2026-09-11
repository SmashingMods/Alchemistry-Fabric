package com.smashingmods.alchemistry.api.container;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
public final class FakeItemRenderer {
    public static void renderFakeItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, float opacity) {
        graphics.fakeItem(stack, x, y);
        graphics.fill(x, y, x + 16, y + 16, ((int)((1 - opacity) * 255) << 24) | 0xC6C6C6);
    }
}
