package com.smashingmods.alchemistry.common.block.compactor;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.api.container.*;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
import com.smashingmods.alchemistry.network.packets.CompactorButtonPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CompactorScreen extends AbstractAlchemistryScreen<CompactorScreenHandler> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "textures/gui/compactor_gui.png");

    protected final List<DisplayData> displayData = new ArrayList<>();
    private final Button resetTargetButton;

    public CompactorScreen(CompactorScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 184, 163);
        displayData.add(new ProgressDisplayData(menu.getPropertyDelegate(), 0, 1, 75, 39, 60, 9, Direction2D.RIGHT));
        displayData.add(new EnergyDisplayData(menu.getPropertyDelegate(), 2, 3, 17, 16, 16, 54));
        resetTargetButton = Button.builder(Component.translatable("alchemistry.container.reset_target"), handleResetTargetButton()).size(100, 20).build();
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
        renderTarget(matrices, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor matrices, int mouseX, int mouseY) {
        MutableComponent title = Component.translatable("alchemistry.container.compactor");
        matrices.text(font, title, imageWidth / 2 - font.width(title) / 2, -10, 0xFFFFFFFF);
    }

    @Override
    public void draw() {
        super.draw();
        renderWidget(resetTargetButton, leftPos - 104, topPos + 48);
    }

    private void renderTarget(GuiGraphicsExtractor matrices, int mouseX, int mouseY) {
        ItemStack target = ((CompactorBlockEntity) this.menu.getBlockEntity()).getTarget();

        int xStart = leftPos + 80;
        int xEnd = xStart + 18;
        int yStart = topPos + 12;
        int yEnd = yStart + 18;

        if (!target.isEmpty()) {
            FakeItemRenderer.renderFakeItem(matrices, target, xStart, yStart, 0.5f);
            if (mouseX >= xStart && mouseX < xEnd && mouseY >= yStart && mouseY < yEnd) {
                List<Component> components = new ArrayList<>();
                components.add(0, Component.translatable("alchemistry.container.target").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withUnderlined(true)));
                components.addAll(getTooltipFromContainerItem(target));
                renderTooltip(matrices, components, target.getTooltipImage(), mouseX, mouseY);
            }
        }
    }

    private Button.OnPress handleResetTargetButton() {
        return button -> com.smashingmods.alchemistry.network.AlchemistryClientNetwork.sendToServer(new CompactorButtonPacket(menu.getBlockEntity().getBlockPos()));
    }
}
