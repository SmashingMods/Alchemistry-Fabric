package com.smashingmods.alchemistry.common.block.fission;

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

public class FissionControllerScreen extends AbstractAlchemistryScreen<FissionControllerScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(Alchemistry.MOD_ID, "textures/gui/fission_gui.png");

    protected final List<DisplayData> displayData = new ArrayList<>();

    public FissionControllerScreen(FissionControllerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        displayData.add(new ProgressDisplayData(handler.getPropertyDelegate(), 0, 1, 74, 39, 60, 9, Direction2D.RIGHT));
        displayData.add(new EnergyDisplayData(handler.getPropertyDelegate(), 2, 3, 17, 16, 16, 54));
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
        MutableText title = Text.translatable("alchemistry.container.fission_controller");
        drawTextWithShadow(matrices, textRenderer, title, backgroundWidth / 2 - textRenderer.getWidth(title) / 2, -10, 0xFFFFFFFF);
    }
}
