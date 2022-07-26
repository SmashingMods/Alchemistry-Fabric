package com.technovision.alchemistry.common.block.dissolver;

import com.mojang.blaze3d.systems.RenderSystem;
import com.technovision.alchemistry.Alchemistry;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class DissolverScreen extends HandledScreen<DissolverScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(Alchemistry.MOD_ID, "textures/gui/dissolver_gui.png");

    protected final List<WindowSettings> displayData = new ArrayList<>();

    public DissolverScreen(DissolverScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 184;
        this.backgroundHeight = 200;
        // TODO: Add when implemented
        //displayData.add(new ProgressDisplayData(pMenu.getContainerData(), 0, 1, 88, 34, 60, 9, Direction2D.DOWN));
        //displayData.add(new EnergyDisplayData(pMenu.getContainerData(), 2, 3, 156, 12, 16, 54));
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
        renderBackground(matrices);
        drawBackground(matrices, delta, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
        //renderWidgets();
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        MutableText title = Text.translatable("alchemistry.container.dissolver");
        drawTextWithShadow(matrices, textRenderer, title, backgroundWidth / 2 - textRenderer.getWidth(title) / 2, -10, 0xFFFFFFFF);
    }
}
