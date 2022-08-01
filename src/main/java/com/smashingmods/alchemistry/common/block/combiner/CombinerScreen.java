package com.smashingmods.alchemistry.common.block.combiner;

import com.mojang.blaze3d.systems.RenderSystem;
import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.api.container.*;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.chemlib.api.Chemical;
import com.smashingmods.chemlib.api.ChemicalItemType;
import com.smashingmods.chemlib.common.items.ChemicalItem;
import com.smashingmods.chemlib.common.items.CompoundItem;
import com.smashingmods.chemlib.common.items.ElementItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CombinerScreen extends AbstractAlchemistryScreen<CombinerScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(Alchemistry.MOD_ID, "textures/gui/combiner_gui.png");

    protected final List<DisplayData> displayData = new ArrayList<>();
    private final CombinerBlockEntity blockEntity;
    protected final TextFieldWidget editBox;

    private final int DISPLAYED_SLOTS = 12;
    private final int RECIPE_BOX_SIZE = 18;
    private float scrollOffset;
    private boolean scrolling;
    private int startIndex;

    public CombinerScreen(CombinerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 184;
        this.backgroundHeight = 193;
        this.displayData.add(new ProgressDisplayData(handler.getPropertyDelegate(), 0, 1, 65, 84, 60, 9, Direction2D.RIGHT));
        this.displayData.add(new EnergyDisplayData(handler.getPropertyDelegate(), 2, 3, 156, 23, 16, 54));
        this.blockEntity = (CombinerBlockEntity) handler.getBlockEntity();

        this.editBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 72, 12, Text.literal(""));
        if (!blockEntity.getEditBoxText().isEmpty()) {
            editBox.setText(blockEntity.getEditBoxText());
            handler.searchRecipeList(blockEntity.getEditBoxText());
        }
    }

    @Override
    protected void handledScreenTick() {
        if (editBox.getText().isEmpty()) {
            blockEntity.setEditBoxText("");
            handler.resetDisplayedRecipes();
            editBox.setSuggestion(I18n.translate("alchemistry.container.combiner.search"));
        } else {
            mouseScrolled(0, 0, 0);
            blockEntity.setEditBoxText(editBox.getText());
            handler.searchRecipeList(editBox.getText());
            editBox.setSuggestion("");
        }
        super.handledScreenTick();
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
        renderRecipeBox(matrices, mouseX, mouseY);
        renderCurrentRecipe(matrices, mouseX, mouseY);

        renderDisplayData(displayData, matrices, this.x, this.y);
        renderDisplayTooltip(displayData, matrices, this.x, this.y, mouseX, mouseY);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
        renderRecipeTooltips(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        MutableText title = Text.translatable("alchemistry.container.combiner");
        drawTextWithShadow(matrices, textRenderer, title, backgroundWidth / 2 - textRenderer.getWidth(title) / 2, -10, 0xFFFFFFFF);
    }

    @Override
    public void renderWidgets() {
        super.renderWidgets();
        renderWidget(editBox, x + 57, y + 7);
    }

    protected void renderRecipeBox(MatrixStack matrices, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int scrollPosition = (int)(39.0F * scrollOffset);
        drawTexture(matrices, x + 132, y + 23 + scrollPosition, 18 + (isScrollBarActive() ? 0 : 12), backgroundHeight, 12, 15);

        int recipeBoxLeftPos = x + 57;
        int recipeBoxTopPos = y + 21;
        int lastVisibleElementIndex = startIndex + DISPLAYED_SLOTS;

        renderRecipeButtons(matrices, mouseX, mouseY, recipeBoxLeftPos, recipeBoxTopPos, lastVisibleElementIndex);
        renderRecipes(recipeBoxLeftPos, recipeBoxTopPos, lastVisibleElementIndex);
    }

    private void renderRecipeButtons(MatrixStack matrices, int pMouseX, int pMouseY, int pX, int pY, int pLastVisibleElementIndex) {

        for (int index = startIndex; index < pLastVisibleElementIndex && index < handler.getDisplayedRecipes().size(); index++) {
            int firstVisibleElementIndex = index - startIndex;
            int col = pX + firstVisibleElementIndex % 4 * RECIPE_BOX_SIZE;
            int rowt = firstVisibleElementIndex / 4;
            int row = pY + rowt * RECIPE_BOX_SIZE + 2;
            int vOffset = backgroundHeight;

            int currentRecipeIndex = handler.getDisplayedRecipes().indexOf((CombinerRecipe) handler.getBlockEntity().getRecipe());

            if (index == handler.getSelectedRecipeIndex() || index == currentRecipeIndex) {
                vOffset += RECIPE_BOX_SIZE;
            } else if (pMouseX >= col && pMouseY >= row && pMouseX < col + RECIPE_BOX_SIZE && pMouseY < row + RECIPE_BOX_SIZE) {
                vOffset += RECIPE_BOX_SIZE * 2;
            }
            drawTexture(matrices, col, row, 0, vOffset, RECIPE_BOX_SIZE, RECIPE_BOX_SIZE);
        }
    }

    private void renderRecipes(int pLeftPos, int pTopPos, int pRecipeIndexOffsetMax) {
        List<CombinerRecipe> list = handler.getDisplayedRecipes();

        for (int index = startIndex; index < pRecipeIndexOffsetMax && index < handler.getDisplayedRecipes().size(); index++) {

            ItemStack output = list.get(index).getOutput();

            int firstVisibleIndex = index - startIndex;
            int recipeBoxLeftPos = pLeftPos + firstVisibleIndex % 4 * RECIPE_BOX_SIZE + 1;
            int l = firstVisibleIndex / 4;
            int recipeBoxTopPos = pTopPos + l * RECIPE_BOX_SIZE + 3;
            MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(output, recipeBoxLeftPos, recipeBoxTopPos);
        }
    }

    private void renderCurrentRecipe(MatrixStack matrices, int pMouseX, int pMouseY) {
        CombinerRecipe currentRecipe = (CombinerRecipe) handler.getBlockEntity().getRecipe();

        // Intellij thinks this is never null. Remove this and watch it crash.
        // noinspection ConstantConditions
        if (currentRecipe != null) {
            ItemStack currentOutput = currentRecipe.getOutput();
            MinecraftClient.getInstance().getItemRenderer().renderInGuiWithOverrides(currentOutput, x + 21, y + 15);

            if (pMouseX >= x + 20 && pMouseX < x + 36 && pMouseY > y + 14 && pMouseY < y + 30) {
                renderItemTooltip(matrices, currentOutput, "alchemistry.container.combiner.current_recipe", pMouseX, pMouseY);
            }

            int xOrigin = x + 12;
            int yOrigin = y + 63;

            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 2; column++) {
                    int index = column + row * 2;
                    int x = xOrigin + column * 18;
                    int y = yOrigin + row * 18;

                    if (index < currentRecipe.getInput().size()) {

                        ItemStack itemStack = currentRecipe.getInput().get(index);

                        if (blockEntity.getStackInSlot(index).isEmpty()) {
                            FakeItemRenderer.renderFakeItem(itemStack, x, y, 0.35F);

                            if (pMouseX >= x - 1 && pMouseX < x + 17 && pMouseY > y - 2 && pMouseY < y + 17) {
                                renderItemTooltip(matrices, itemStack, "alchemistry.container.combiner.required_input", pMouseX, pMouseY);
                            }
                        }
                    }
                }
            }
        }
    }

    private void renderRecipeTooltips(MatrixStack matrices, int pMouseX, int pMouseY) {
        int originX = x + 57;
        int originY = y + 23;
        List<CombinerRecipe> displayedRecipes = handler.getDisplayedRecipes();

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

    private void renderItemTooltip(MatrixStack matrices, ItemStack pItemStack, String pTranslationKey, int pMouseX, int pMouseY) {
        List<Text> components = new ArrayList<>();
        String namespace = StringUtils.capitalize(Registry.ITEM.getId(pItemStack.getItem()).getNamespace());

        components.add(Text.translatable(pTranslationKey).formatted(Formatting.UNDERLINE, Formatting.BLUE));
        components.add(Text.literal(String.format("%dx %s", pItemStack.getCount(), pItemStack.getItem().getName().getString())));

        if (pItemStack.getItem() instanceof Chemical chemical) {

            String abbreviation = chemical.getAbbreviation();

            if (chemical instanceof ElementItem element) {
                components.add(Text.literal(String.format("%s (%d)", abbreviation, element.getAtomicNumber())).formatted(Formatting.DARK_AQUA));
                components.add(Text.literal(element.getGroupName()).formatted(Formatting.GRAY));
            } else if (chemical instanceof ChemicalItem chemicalItem && !chemicalItem.getItemType().equals(ChemicalItemType.COMPOUND)) {
                ElementItem element = (ElementItem) chemicalItem.getChemical();
                components.add(Text.literal(String.format("%s (%d)", chemicalItem.getAbbreviation(), element.getAtomicNumber())).formatted(Formatting.DARK_AQUA));
                components.add(Text.literal(element.getGroupName()).formatted(Formatting.GRAY));
            } else if (chemical instanceof CompoundItem) {
                components.add(Text.literal(abbreviation).formatted(Formatting.DARK_AQUA));
            }
        }
        components.add(Text.literal(namespace).formatted(Formatting.BLUE));
        renderTooltip(matrices, components, Optional.empty(), pMouseX, pMouseY);
    }

    private boolean isScrollBarActive() {
        return handler.getDisplayedRecipes().size() > 12;
    }
}
