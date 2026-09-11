package com.smashingmods.alchemistry.common.block.liquifier;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.api.container.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class LiquifierScreen extends AbstractAlchemistryScreen<LiquifierScreenHandler> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "textures/gui/liquifier_gui.png");

    protected final List<DisplayData> displayData = new ArrayList<>();

    public LiquifierScreen(LiquifierScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        displayData.add(new ProgressDisplayData(menu.getPropertyDelegate(), 0, 1, 92, 39, 60, 9, Direction2D.RIGHT));
        displayData.add(new EnergyDisplayData(menu.getPropertyDelegate(), 2, 3, 26, 21, 16, 46));
        displayData.add(new FluidDisplayData(menu.getBlockEntity(), menu.getPropertyDelegate(), 4, 5, 134, 21, 16, 46));
    }

    @Override
    protected void drawBackground(GuiGraphicsExtractor matrices, float delta, int mouseX, int mouseY) {
        texture = TEXTURE;
        drawTexture(matrices, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor matrices, int mouseX, int mouseY, float delta) {
        super.extractRenderState(matrices, mouseX, mouseY, delta);
        renderDisplayData(displayData, matrices, this.leftPos, this.topPos);
        renderDisplayTooltip(displayData, matrices, this.leftPos, this.topPos, mouseX, mouseY);
        extractTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor matrices, int mouseX, int mouseY) {
        MutableComponent title = Component.translatable("alchemistry.container.liquifier");
        matrices.text(font, title, imageWidth / 2 - font.width(title) / 2, -10, 0xFFFFFFFF);
    }
}
