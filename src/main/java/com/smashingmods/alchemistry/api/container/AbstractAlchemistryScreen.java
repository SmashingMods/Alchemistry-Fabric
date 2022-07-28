package com.smashingmods.alchemistry.api.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
import com.smashingmods.alchemistry.network.packets.ProcessingButtonPacket;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public abstract class AbstractAlchemistryScreen<M extends AbstractAlchemistryScreenHandler> extends HandledScreen<M> {

    protected final ButtonWidget lockButton;
    protected final ButtonWidget unlockButton;

    protected final ButtonWidget pauseButton;
    protected final ButtonWidget resumeButton;

    public AbstractAlchemistryScreen(M handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        lockButton = new ButtonWidget(0, 0, 100, 20, Text.translatable("alchemistry.container.lock_recipe"), handleLock());
        unlockButton = new ButtonWidget(0, 0, 100, 20, Text.translatable("alchemistry.container.unlock_recipe"), handleLock());
        pauseButton = new ButtonWidget(0, 0, 100, 20, Text.translatable("alchemistry.container.pause"), handlePause());
        resumeButton = new ButtonWidget(0, 0, 100, 20, Text.translatable("alchemistry.container.resume"), handlePause());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawBackground(matrices, delta, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
        renderWidgets();
    }

    public <W extends Element & Drawable & Selectable> void renderWidget(W widget, int x, int y) {
        if (widget instanceof ClickableWidget clickableWidget) {
            clickableWidget.x = x;
            clickableWidget.y = y;
        }
        addDrawableChild(widget);
    }

    public void renderWidgets() {
        clearChildren();
        if (handler.getBlockEntity().isRecipeLocked()) {
            renderWidget(unlockButton, x - 104, y);
        } else {
            renderWidget(lockButton, x - 104, y);
        }
        if (handler.getBlockEntity().isProcessingPaused()) {
            renderWidget(resumeButton, x - 104, y + 24);
        } else {
            renderWidget(pauseButton, x - 104, y + 24);
        }
    }

    private ButtonWidget.PressAction handleLock() {
        return buttonWidget -> {
            boolean lockState = !handler.getBlockEntity().isRecipeLocked();
            boolean pausedState = handler.getBlockEntity().isProcessingPaused();
            AlchemistryNetwork.sendToServer(new ProcessingButtonPacket(handler.getBlockEntity().getPos(), lockState, pausedState));
        };
    }

    private ButtonWidget.PressAction handlePause() {
        return buttonWidget -> {
            boolean lockState = handler.getBlockEntity().isRecipeLocked();
            boolean pausedState = !handler.getBlockEntity().isProcessingPaused();
            AlchemistryNetwork.sendToServer(new ProcessingButtonPacket(handler.getBlockEntity().getPos(), lockState, pausedState));
        };
    }

    public void renderDisplayTooltip(List<DisplayData> displayData, MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        displayData.stream().filter(data ->
                mouseX >= data.getX() + x &&
                        mouseX <= data.getX() + x + data.getWidth() &&
                        mouseY >= data.getY() + y &&
                        mouseY <= data.getY() + y + data.getHeight()
        ).forEach(data -> {
            if (!(data instanceof ProgressDisplayData)) {
                renderTooltip(matrices, data.toTextComponent(), mouseX, mouseY);
            }
        });
    }

    public void renderDisplayData(List<DisplayData> displayData, MatrixStack matrices, int x, int y) {
        displayData.forEach(data -> {
            if (data instanceof ProgressDisplayData) {
                directionalArrow(matrices, x + data.getX(), y + data.getY(), data.getValue(), data.getMaxValue(), ((ProgressDisplayData) data).getDirection());
            }
            // TODO: Add when energy and fluid are implemented
            /**
            if (data instanceof EnergyDisplayData) {
                drawEnergyBar(pPoseStack, (EnergyDisplayData) data, 0, 40);
            }
            if (data instanceof FluidDisplayData) {
                drawFluidTank((FluidDisplayData) data, pX + data.getX(), pY + data.getY());
            }
             */
        });
    }

    public void directionalArrow(MatrixStack matrices, int x, int y, int progress, int maxProgress, Direction2D direction2D) {
        switch (direction2D) {
            case LEFT -> directionalBlit(matrices, x, y, 0, 120, 9, 30, progress, maxProgress, Direction2D.LEFT);
            case UP -> directionalBlit(matrices, x, y, 0, 138, 9, 30, progress, maxProgress, Direction2D.UP);
            case RIGHT -> directionalBlit(matrices, x, y, 0, 129, 9, 30, progress, maxProgress, Direction2D.RIGHT);
            case DOWN -> directionalBlit(matrices, x, y, 9, 138, 9, 30, progress, maxProgress, Direction2D.DOWN);
        }
    }

    private void directionalBlit(MatrixStack matrices, int x, int y, int uOffset, int vOffset, int u, int v, int progress, int maxProgress, Direction2D direction2D) {
        RenderSystem.setShaderTexture(0, new Identifier(Alchemistry.MOD_ID, "textures/gui/widgets.png"));

        switch (direction2D) {
            case LEFT -> {
                int scaled = getBarScaled(v, progress, maxProgress);
                drawTexture(matrices, x - scaled, y, uOffset + u - scaled, vOffset, scaled, v);
            }
            case UP -> {
                int scaled = getBarScaled(v, progress, maxProgress);
                drawTexture(matrices, x, y - scaled, uOffset, vOffset + v - scaled, u, scaled);
            }
            case RIGHT -> {
                int scaled = getBarScaled(v, progress, maxProgress);
                drawTexture(matrices, x, y, uOffset, vOffset, scaled, u);
            }
            case DOWN -> {
                int scaled = getBarScaled(v, progress, maxProgress);
                drawTexture(matrices, x, y, uOffset, vOffset, u, scaled);
            }
        }
    }

    public static int getBarScaled(int pixels, int progress, int maxProgress) {
        if (progress > 0 && maxProgress > 0) {
            return progress * pixels / maxProgress;
        } else {
            return 0;
        }
    }
}
