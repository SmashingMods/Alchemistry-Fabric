package com.smashingmods.alchemistry.api.container;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
import com.smashingmods.alchemistry.network.packets.ProcessingButtonPacket;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public abstract class AbstractAlchemistryScreen<M extends AbstractAlchemistryScreenHandler> extends AbstractContainerScreen<M> {
    protected Identifier texture;
    protected GuiGraphicsExtractor graphics;
    protected abstract void drawBackground(GuiGraphicsExtractor graphics, float delta, int mouseX, int mouseY);
    protected void drawTexture(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h) {
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, 256, 256);
    }
    protected void renderTooltip(GuiGraphicsExtractor graphics, List<Component> lines, int x, int y) {
        graphics.setTooltipForNextFrame(font, lines, java.util.Optional.empty(), x, y);
    }
    protected void renderTooltip(GuiGraphicsExtractor graphics, List<Component> lines, java.util.Optional<net.minecraft.world.inventory.tooltip.TooltipComponent> image, int x, int y) {
        graphics.setTooltipForNextFrame(font, lines, image, x, y);
    }


    protected final Button lockButton;
    protected final Button unlockButton;

    protected final Button pauseButton;
    protected final Button resumeButton;

    public AbstractAlchemistryScreen(M menu, Inventory inventory, Component title) {
        this(menu, inventory, title, 176, 166);
    }

    public AbstractAlchemistryScreen(M menu, Inventory inventory, Component title, int width, int height) {
        super(menu, inventory, title, width, height);
        lockButton = Button.builder(Component.translatable("alchemistry.container.lock_recipe"), handleLock()).size(100, 20).build();
        unlockButton = Button.builder(Component.translatable("alchemistry.container.unlock_recipe"), handleLock()).size(100, 20).build();
        pauseButton = Button.builder(Component.translatable("alchemistry.container.pause"), handlePause()).size(100, 20).build();
        resumeButton = Button.builder(Component.translatable("alchemistry.container.resume"), handlePause()).size(100, 20).build();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor matrices, int mouseX, int mouseY, float delta) {
        this.graphics = matrices;
        draw();
        drawBackground(matrices, delta, mouseX, mouseY);
        super.extractRenderState(matrices, mouseX, mouseY, delta);
    }

    public void renderWidget(AbstractWidget widget, int x, int y) {
        widget.setPosition(x, y);
        if (!children().contains(widget)) addRenderableWidget(widget);
    }

    public void draw() {
        renderWidget(lockButton, leftPos - 104, topPos);
        renderWidget(unlockButton, leftPos - 104, topPos);
        renderWidget(pauseButton, leftPos - 104, topPos + 24);
        renderWidget(resumeButton, leftPos - 104, topPos + 24);
        lockButton.visible = !menu.getBlockEntity().isRecipeLocked();
        unlockButton.visible = !lockButton.visible;
        pauseButton.visible = !menu.getBlockEntity().isProcessingPaused();
        resumeButton.visible = !pauseButton.visible;
    }

    private Button.OnPress handleLock() {
        return buttonWidget -> {
            boolean lockState = !menu.getBlockEntity().isRecipeLocked();
            boolean pausedState = menu.getBlockEntity().isProcessingPaused();
            com.smashingmods.alchemistry.network.AlchemistryClientNetwork.sendToServer(new ProcessingButtonPacket(menu.getBlockEntity().getBlockPos(), lockState, pausedState));
        };
    }

    private Button.OnPress handlePause() {
        return buttonWidget -> {
            boolean lockState = menu.getBlockEntity().isRecipeLocked();
            boolean pausedState = !menu.getBlockEntity().isProcessingPaused();
            com.smashingmods.alchemistry.network.AlchemistryClientNetwork.sendToServer(new ProcessingButtonPacket(menu.getBlockEntity().getBlockPos(), lockState, pausedState));
        };
    }

    public void renderDisplayTooltip(List<DisplayData> displayData, GuiGraphicsExtractor matrices, int x, int y, int mouseX, int mouseY) {
        displayData.stream().filter(data ->
                mouseX >= data.getX() + x &&
                        mouseX <= data.getX() + x + data.getWidth() &&
                        mouseY >= data.getY() + y &&
                        mouseY <= data.getY() + y + data.getHeight()
        ).forEach(data -> {
            if (!(data instanceof ProgressDisplayData)) {
                renderTooltip(matrices, data.toText(), mouseX, mouseY);
            }
        });
    }

    public void renderDisplayData(List<DisplayData> displayData, GuiGraphicsExtractor matrices, int x, int y) {
        displayData.forEach(data -> {
            if (data instanceof ProgressDisplayData) {
                directionalArrow(matrices, x + data.getX(), y + data.getY(), data.getValue(), data.getMaxValue(), ((ProgressDisplayData) data).getDirection());
            }
            if (data instanceof EnergyDisplayData) {
                drawEnergyBar(matrices, (EnergyDisplayData) data, 0, 40);
            }
            if (data instanceof FluidDisplayData) {
                drawFluidTank(matrices, (FluidDisplayData) data, x + data.getX(), y + data.getY());
            }
        });
    }

    public void drawEnergyBar(GuiGraphicsExtractor pGuiGraphicsExtractor, EnergyDisplayData data, int textureX, int textureY) {
        int x = data.getX() + (this.width - this.imageWidth) / 2;
        int y = data.getY() + (this.height - this.imageHeight) / 2;
        this.directionalBlit(pGuiGraphicsExtractor, x, y + data.getHeight(), textureX, textureY, data.getWidth(), data.getHeight(), data.getValue(), data.getMaxValue(), Direction2D.UP);
    }

    public void drawFluidTank(GuiGraphicsExtractor graphics, FluidDisplayData data, int x, int y) {
        if (data.getValue() <= 0 || data.getMaxValue() <= 0) return;
        FluidVariant variant = data.getFluidVariant();
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(variant.getFluid().defaultFluidState()).stillMaterial().sprite();
        if (sprite == null) return;
        int color = FluidVariantRendering.getColor(variant) | 0xFF000000;
        int height = getBarScaled(data.getHeight(), data.getValue(), data.getMaxValue());
        graphics.enableScissor(x, y + data.getHeight() - height, x + data.getWidth(), y + data.getHeight());
        for (int dy = 0; dy < data.getHeight(); dy += 16)
            for (int dx = 0; dx < data.getWidth(); dx += 16)
                graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, x + dx, y + dy, 16, 16, color);
        graphics.disableScissor();
    }

    public void directionalArrow(GuiGraphicsExtractor matrices, int x, int y, int progress, int maxProgress, Direction2D direction2D) {
        switch (direction2D) {
            case LEFT -> directionalBlit(matrices, x, y, 0, 120, 9, 30, progress, maxProgress, Direction2D.LEFT);
            case UP -> directionalBlit(matrices, x, y, 0, 138, 9, 30, progress, maxProgress, Direction2D.UP);
            case RIGHT -> directionalBlit(matrices, x, y, 0, 129, 9, 30, progress, maxProgress, Direction2D.RIGHT);
            case DOWN -> directionalBlit(matrices, x, y, 9, 138, 9, 30, progress, maxProgress, Direction2D.DOWN);
        }
    }

    private void directionalBlit(GuiGraphicsExtractor matrices, int x, int y, int uOffset, int vOffset, int u, int v, int progress, int maxProgress, Direction2D direction2D) {
        texture = Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "textures/gui/widgets.png");

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
