package com.smashingmods.alchemistry.common.block.combiner;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.api.container.*;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.chemlib.api.Chemical;
import com.smashingmods.chemlib.api.ChemicalItemType;
import com.smashingmods.chemlib.common.items.ChemicalItem;
import com.smashingmods.chemlib.common.items.CompoundItem;
import com.smashingmods.chemlib.common.items.ElementItem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CombinerScreen extends AbstractAlchemistryScreen<CombinerScreenHandler> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Alchemistry.MOD_ID, "textures/gui/combiner_gui.png");

    protected final List<DisplayData> displayData = new ArrayList<>();
    private final CombinerBlockEntity blockEntity;
    protected final EditBox editBox;

    private final int DISPLAYED_SLOTS = 12;
    private final int RECIPE_BOX_SIZE = 18;
    private float scrollOffset;
    private boolean scrolling;
    private int startIndex;
    private int editBoxCharacters;

    public CombinerScreen(CombinerScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 184, 193);
        this.displayData.add(new ProgressDisplayData(menu.getPropertyDelegate(), 0, 1, 65, 84, 60, 9, Direction2D.RIGHT));
        this.displayData.add(new EnergyDisplayData(menu.getPropertyDelegate(), 2, 3, 156, 23, 16, 54));
        this.blockEntity = (CombinerBlockEntity) menu.getBlockEntity();
        this.editBoxCharacters = 0;

        this.editBox = new EditBox(Minecraft.getInstance().font, 0, 0, 72, 12, Component.literal(""));
        if (!blockEntity.getEditBoxText().isEmpty()) {
            editBox.setValue(blockEntity.getEditBoxText());
            menu.searchRecipeList(blockEntity.getEditBoxText());
        }
    }

    @Override
    protected void containerTick() {
        if (blockEntity.getEditBoxText().length() != editBoxCharacters) {
            editBoxCharacters = blockEntity.getEditBoxText().length();
            mouseScrolled(0, 0, 0, 0);
            blockEntity.setEditBoxText(editBox.getValue());
            menu.searchRecipeList(editBox.getValue());
            editBox.setSuggestion("");
            resetScrollbar();
        } else if (editBox.getValue().isEmpty()) {
            blockEntity.setEditBoxText("");
            menu.resetDisplayedRecipes();
            editBox.setSuggestion(I18n.get("alchemistry.container.combiner.search"));
        } else {
            mouseScrolled(0, 0, 0, 0);
            blockEntity.setEditBoxText(editBox.getValue());
            editBox.setSuggestion("");
        }
        super.containerTick();
    }

    private void resetScrollbar() {
        scrollOffset = 0;
        scrolling = false;
        startIndex = 0;
    }

    @Override
    protected void drawBackground(GuiGraphicsExtractor matrices, float delta, int mouseX, int mouseY) {
        texture = TEXTURE;
        drawTexture(matrices, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor matrices, int mouseX, int mouseY, float delta) {
        super.extractRenderState(matrices, mouseX, mouseY, delta);
        renderRecipeBox(matrices, mouseX, mouseY);
        renderCurrentRecipe(matrices, mouseX, mouseY);
        renderDisplayData(displayData, matrices, this.leftPos, this.topPos);

        extractTooltip(matrices, mouseX, mouseY);
        renderRecipeTooltips(matrices, mouseX, mouseY);
        renderDisplayTooltip(displayData, matrices, this.leftPos, this.topPos, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor matrices, int mouseX, int mouseY) {
        MutableComponent title = Component.translatable("alchemistry.container.combiner");
        matrices.text(font, title, imageWidth / 2 - font.width(title) / 2, -10, 0xFFFFFFFF);
    }

    @Override
    public void draw() {
        super.draw();
        renderWidget(editBox, leftPos + 57, topPos + 7);
    }

    protected void renderRecipeBox(GuiGraphicsExtractor matrices, int mouseX, int mouseY) {
        texture = TEXTURE;

        int scrollPosition = (int)(39.0F * scrollOffset);
        drawTexture(matrices, leftPos + 132, topPos + 23 + scrollPosition, 18 + (isScrollBarActive() ? 0 : 12), imageHeight, 12, 15);

        int recipeBoxLeftPos = leftPos + 57;
        int recipeBoxTopPos = topPos + 21;
        int lastVisibleElementIndex = startIndex + DISPLAYED_SLOTS;

        renderRecipeButtons(matrices, mouseX, mouseY, recipeBoxLeftPos, recipeBoxTopPos, lastVisibleElementIndex);
        renderRecipes(recipeBoxLeftPos, recipeBoxTopPos, lastVisibleElementIndex);
    }

    private void renderRecipeButtons(GuiGraphicsExtractor matrices, int pMouseX, int pMouseY, int pX, int pY, int pLastVisibleElementIndex) {

        for (int index = startIndex; index < pLastVisibleElementIndex && index < menu.getDisplayedRecipes().size(); index++) {
            int firstVisibleElementIndex = index - startIndex;
            int col = pX + firstVisibleElementIndex % 4 * RECIPE_BOX_SIZE;
            int rowt = firstVisibleElementIndex / 4;
            int row = pY + rowt * RECIPE_BOX_SIZE + 2;
            int vOffset = imageHeight;

            int currentRecipeIndex = menu.getDisplayedRecipes().indexOf((CombinerRecipe) menu.getBlockEntity().getRecipe());

            if (index == menu.getSelectedRecipeIndex() || index == currentRecipeIndex) {
                vOffset += RECIPE_BOX_SIZE;
            } else if (pMouseX >= col && pMouseY >= row && pMouseX < col + RECIPE_BOX_SIZE && pMouseY < row + RECIPE_BOX_SIZE) {
                vOffset += RECIPE_BOX_SIZE * 2;
            }
            drawTexture(matrices, col, row, 0, vOffset, RECIPE_BOX_SIZE, RECIPE_BOX_SIZE);
        }
    }

    private void renderRecipes(int pLeftPos, int pTopPos, int pRecipeIndexOffsetMax) {
        List<CombinerRecipe> list = menu.getDisplayedRecipes();

        for (int index = startIndex; index < pRecipeIndexOffsetMax && index < menu.getDisplayedRecipes().size(); index++) {

            ItemStack output = list.get(index).getOutput();

            int firstVisibleIndex = index - startIndex;
            int recipeBoxLeftPos = pLeftPos + firstVisibleIndex % 4 * RECIPE_BOX_SIZE + 1;
            int l = firstVisibleIndex / 4;
            int recipeBoxTopPos = pTopPos + l * RECIPE_BOX_SIZE + 3;
            graphics.item(output, recipeBoxLeftPos, recipeBoxTopPos);
        }
    }

    private void renderCurrentRecipe(GuiGraphicsExtractor matrices, int pMouseX, int pMouseY) {
        CombinerRecipe currentRecipe = (CombinerRecipe) menu.getBlockEntity().getRecipe();

        // Intellij thinks this is never null. Remove this and watch it crash.
        // noinspection ConstantConditions
        if (currentRecipe != null) {
            ItemStack currentOutput = currentRecipe.getOutput();
            matrices.item(currentOutput, leftPos + 21, topPos + 15);

            if (pMouseX >= leftPos + 20 && pMouseX < leftPos + 36 && pMouseY > topPos + 14 && pMouseY < topPos + 30) {
                renderItemTooltip(matrices, currentOutput, "alchemistry.container.combiner.current_recipe", pMouseX, pMouseY);
            }

            int xOrigin = leftPos + 12;
            int yOrigin = topPos + 63;

            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 2; column++) {
                    int index = column + row * 2;
                    int leftPos = xOrigin + column * 18;
                    int topPos = yOrigin + row * 18;

                    if (index < currentRecipe.getInput().size()) {

                        ItemStack itemStack = currentRecipe.getInput().get(index);
                        if (menu.getClientInventory().getItem(index).isEmpty()) {
                            FakeItemRenderer.renderFakeItem(matrices, itemStack, leftPos, topPos, 0.35F);

                            if (pMouseX >= leftPos - 1 && pMouseX < leftPos + 17 && pMouseY > topPos - 2 && pMouseY < topPos + 17) {
                                renderItemTooltip(matrices, itemStack, "alchemistry.container.combiner.required_input", pMouseX, pMouseY);
                            }
                        }
                    }
                }
            }
        }
    }

    private void renderRecipeTooltips(GuiGraphicsExtractor matrices, int pMouseX, int pMouseY) {
        int originX = leftPos + 57;
        int originY = topPos + 23;
        List<CombinerRecipe> displayedRecipes = menu.getDisplayedRecipes();

        for (int index = startIndex; index < startIndex + DISPLAYED_SLOTS && index < displayedRecipes.size(); index++) {
            ItemStack output = displayedRecipes.get(index).getOutput();

            int firstVisibleIndex = index - startIndex;
            int recipeBoxLeftPos = originX + firstVisibleIndex % 4 * RECIPE_BOX_SIZE;
            int col = firstVisibleIndex / 4;
            int recipeBoxTopPos = originY + col * RECIPE_BOX_SIZE;

            if (pMouseX >= recipeBoxLeftPos && pMouseX <= recipeBoxLeftPos + 17 && pMouseY >= recipeBoxTopPos && pMouseY <= recipeBoxTopPos + 17) {
                renderItemTooltip(matrices, output, "alchemistry.container.combiner.select_recipe", pMouseX, pMouseY);
            }
        }
    }

    private void renderItemTooltip(GuiGraphicsExtractor matrices, ItemStack pItemStack, String pTranslationKey, int pMouseX, int pMouseY) {
        List<Component> components = new ArrayList<>();
        String namespace = FabricLoader.getInstance().getModContainer(BuiltInRegistries.ITEM.getKey(pItemStack.getItem()).getNamespace()).get().getMetadata().getName();

        components.add(Component.translatable(pTranslationKey).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.YELLOW));
        components.add(Component.literal(String.format("%dx %s", pItemStack.getCount(), pItemStack.getHoverName().getString())));

        if (pItemStack.getItem() instanceof Chemical chemical) {
            String abbreviation = chemical.getAbbreviation();

            if (chemical instanceof ElementItem element) {
                components.add(Component.literal(String.format("%s (%d)", abbreviation, element.getAtomicNumber())).withStyle(ChatFormatting.DARK_AQUA));
                components.add(Component.literal(element.getGroupName()).withStyle(ChatFormatting.GRAY));
            } else if (chemical instanceof ChemicalItem chemicalItem && !chemicalItem.getItemType().equals(ChemicalItemType.COMPOUND)) {
                ElementItem element = (ElementItem) chemicalItem.getChemical();
                components.add(Component.literal(String.format("%s (%d)", chemicalItem.getAbbreviation(), element.getAtomicNumber())).withStyle(ChatFormatting.DARK_AQUA));
                components.add(Component.literal(element.getGroupName()).withStyle(ChatFormatting.GRAY));
            } else if (chemical instanceof CompoundItem) {
                components.add(Component.literal(abbreviation).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        components.add(Component.literal(namespace).withStyle(ChatFormatting.BLUE));
        renderTooltip(matrices, components, Optional.empty(), pMouseX, pMouseY);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int pKeyCode = event.key();
        if (pKeyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_E && editBox.isFocused()) {
            return false;
        } else if (pKeyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_TAB && !editBox.isFocused()) {
            editBox.setFocused(true);
            editBox.setEditable(true);
            editBox.active = true;
        } else if (pKeyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && editBox.isFocused()) {
            editBox.setFocused(false);
            editBox.setEditable(false);
            editBox.active = false;
            return false;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double pMouseX = event.x(), pMouseY = event.y();
        int pButton = event.button();
        Objects.requireNonNull(Minecraft.getInstance().player);
        Objects.requireNonNull(Minecraft.getInstance().gameMode);

        int editBoxMinX = leftPos + 57;
        int editBoxMaxX = editBoxMinX + 72;
        int editBoxMinY = topPos + 7;
        int editBoxMaxY = editBoxMinY + 12;

        if (pMouseX >= editBoxMinX && pMouseX < editBoxMaxX && pMouseY >= editBoxMinY && pMouseY < editBoxMaxY) {
            editBox.mouseClicked(event, doubleClick);
        } else {
            editBox.active = false;
        }
        scrolling = false;

        int recipeBoxLeftPos = leftPos + 57;
        int recipeBoxTopPos = topPos + 23;
        int k = startIndex + DISPLAYED_SLOTS;

        for (int index = this.startIndex; index < k; index++) {
            int currentIndex = index - startIndex;
            double boxX = pMouseX - (double)(recipeBoxLeftPos + currentIndex % 4 * RECIPE_BOX_SIZE);
            double boxY = pMouseY - (double)(recipeBoxTopPos + currentIndex / 4 * RECIPE_BOX_SIZE);

            if (boxX >= 0 && boxY >= 0 && boxX < RECIPE_BOX_SIZE && boxY < RECIPE_BOX_SIZE && menu.clickMenuButton(Minecraft.getInstance().player, index)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                Minecraft.getInstance().gameMode.handleInventoryButtonClick((menu).containerId, index);
                return true;
            }

            int scrollMinX = recipeBoxLeftPos + 75;
            int scrollMaxX = recipeBoxLeftPos + 87;
            int scrollMaxY = recipeBoxTopPos + 54;
            if (pMouseX >= scrollMinX
                    && pMouseX < scrollMaxX
                    && pMouseY >= recipeBoxTopPos
                    && pMouseY < scrollMaxY) {
                scrolling = true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double pDragX, double pDragY) {
        double pMouseX = event.x(), pMouseY = event.y();
        if (scrolling && isScrollBarActive()) {
            int i = topPos + 14;
            int j = i + 54;
            scrollOffset = ((float)pMouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
            startIndex = (int)((double)(scrollOffset * (float) getOffscreenRows()) + 0.5D) * 4;
            return true;
        } else {
            return super.mouseDragged(event, pDragX, pDragY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double horizontal, double pDelta) {
        if (isScrollBarActive()) {
            int offscreenRows = getOffscreenRows();
            float f = (float) pDelta / (float) offscreenRows;
            scrollOffset = Mth.clamp(scrollOffset - f, 0.0F, 1.0F);
            startIndex = (int)((double)(scrollOffset * (float) offscreenRows) + 0.5D) * 4;
        }
        return true;
    }

    private boolean isScrollBarActive() {
        return menu.getDisplayedRecipes().size() > 12;
    }

    private int getOffscreenRows() {
        return (menu.getDisplayedRecipes().size() + 4 - 1) / 4 - 3;
    }
}
