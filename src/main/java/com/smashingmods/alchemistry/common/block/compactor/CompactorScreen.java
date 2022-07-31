package com.smashingmods.alchemistry.common.block.compactor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.api.container.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CompactorScreen extends AbstractAlchemistryScreen<CompactorScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(Alchemistry.MOD_ID, "textures/gui/compactor_gui.png");

    protected final List<DisplayData> displayData = new ArrayList<>();
    private final ButtonWidget resetTargetButton;

    public CompactorScreen(CompactorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 184;
        this.backgroundHeight = 163;
        displayData.add(new ProgressDisplayData(handler.getPropertyDelegate(), 0, 1, 75, 39, 60, 9, Direction2D.RIGHT));
        displayData.add(new EnergyDisplayData(handler.getPropertyDelegate(), 2, 3, 17, 16, 16, 54));
        resetTargetButton = new ButtonWidget(0, 0, 100, 20, Text.translatable("alchemistry.container.reset_target"), handleResetTargetButton());
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
        renderTarget(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        MutableText title = Text.translatable("alchemistry.container.compactor");
        drawTextWithShadow(matrices, textRenderer, title, backgroundWidth / 2 - textRenderer.getWidth(title) / 2, -10, 0xFFFFFFFF);
    }

    @Override
    public void renderWidgets() {
        super.renderWidgets();
        renderWidget(resetTargetButton, x - 104, y + 48);
    }

    private void renderTarget(MatrixStack matrices, int mouseX, int mouseY) {
        ItemStack target = ((CompactorBlockEntity) this.handler.getBlockEntity()).getTarget();

        int xStart = x + 80;
        int xEnd = xStart + 18;
        int yStart = y + 12;
        int yEnd = yStart + 18;

        if (!target.isEmpty()) {
            FakeItemRenderer.renderFakeItem(target, xStart, yStart, 0.5f);
            if (mouseX >= xStart && mouseX < xEnd && mouseY >= yStart && mouseY < yEnd) {
                List<Text> components = new ArrayList<>();
                components.add(0, Text.translatable("alchemistry.container.target").setStyle(Style.EMPTY.withColor(Formatting.YELLOW).withUnderline(true)));
                components.addAll(target.getTooltip(client.player, TooltipContext.Default.NORMAL));
                renderTooltip(matrices, components, target.getTooltipData(), mouseX, mouseY);
            }
        }
    }

    private ButtonWidget.PressAction handleResetTargetButton() {
        return pButton -> System.out.println("PRESSED RESET");
    }
}
