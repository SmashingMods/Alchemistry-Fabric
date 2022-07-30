package com.smashingmods.alchemistry.common.block.liquifier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.api.container.*;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class LiquifierScreen extends AbstractAlchemistryScreen<LiquifierScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(Alchemistry.MOD_ID, "textures/gui/liquifier_gui.png");

    protected final List<DisplayData> displayData = new ArrayList<>();

    public LiquifierScreen(LiquifierScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        displayData.add(new ProgressDisplayData(handler.getPropertyDelegate(), 0, 1, 92, 39, 60, 9, Direction2D.RIGHT));
        displayData.add(new EnergyDisplayData(handler.getPropertyDelegate(), 2, 3, 26, 21, 16, 46));
        displayData.add(new FluidDisplayData(handler.getBlockEntity(), handler.getPropertyDelegate(), 4, 5, 134, 21, 16, 46));
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matrices, this.x, this.y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        renderDisplayData(displayData, matrices, this.x, this.y);
        renderDisplayTooltip(displayData, matrices, this.x, this.y, mouseX, mouseY);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        MutableText title = Text.translatable("alchemistry.container.liquifier");
        drawTextWithShadow(matrices, textRenderer, title, backgroundWidth / 2 - textRenderer.getWidth(title) / 2, -10, 0xFFFFFFFF);
    }
}
