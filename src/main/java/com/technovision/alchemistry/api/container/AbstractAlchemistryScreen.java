package com.technovision.alchemistry.api.container;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

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
        addDrawable(widget);
    }

    public void renderWidgets() {
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
        return pButton -> {
            boolean lockState = handler.getBlockEntity().isRecipeLocked();
            boolean pausedState = handler.getBlockEntity().isProcessingPaused();
            //AlchemistryPacketHandler.INSTANCE.sendToServer(new ProcessingButtonPacket(menu.getBlockEntity().getBlockPos(), !lockState, pausedState));
        };
    }

    private ButtonWidget.PressAction handlePause() {
        return pButton -> {
            boolean lockState = handler.getBlockEntity().isRecipeLocked();
            boolean pausedState = handler.getBlockEntity().isProcessingPaused();
            //AlchemistryPacketHandler.INSTANCE.sendToServer(new ProcessingButtonPacket(menu.getBlockEntity().getBlockPos(), lockState, !pausedState));
        };
    }
}
