package com.smashingmods.alchemistry.client.guide;

import com.google.gson.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** The original handbook, including crafting pages and inspectable reactor layers. */
public class GuideBookScreen extends Screen {
    private final List<JsonObject> entries = new ArrayList<>();
    private int entryIndex, pageIndex, scroll, layer, rotation;
    private int left, top, panelWidth, panelHeight;
    private Button projectButton;
    public GuideBookScreen() { super(Component.translatable("alchemistry.patchouli.book_name")); }

    @Override protected void init() {
        panelWidth = Math.min(510, width - 16);
        panelHeight = Math.min(340, height - 16);
        left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        if (entries.isEmpty()) {
            minecraft.getResourceManager().listResources("guide/entries", id -> id.getNamespace().equals("alchemistry") && id.getPath().endsWith(".json"))
                .entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(resource -> {
                    try (var reader = resource.getValue().openAsReader()) { entries.add(JsonParser.parseReader(reader).getAsJsonObject()); }
                    catch (Exception ex) { com.smashingmods.alchemistry.Alchemistry.LOGGER.error("Cannot read guide entry {}", resource.getKey(), ex); }
                });
        }
        clearWidgets();
        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            addRenderableWidget(Button.builder(Component.literal(entries.get(i).get("name").getAsString()), button -> {
                entryIndex = index; pageIndex = scroll = layer = rotation = 0;
            }).bounds(left + 10, top + 32 + i * 22, 142, 20).build());
        }
        projectButton = addRenderableWidget(Button.builder(Component.literal("Project in world"), button -> ReactorProjection.beginFromGuide()).bounds(left + 166, top + panelHeight - 53, 142, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Hide projection"), button -> ReactorProjection.clear()).bounds(left + 10, top + panelHeight - 53, 142, 20).build());
        addRenderableWidget(Button.builder(Component.literal("<"), b -> turn(-1)).bounds(left + 165, top + panelHeight - 27, 35, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> turn(1)).bounds(left + panelWidth - 47, top + panelHeight - 27, 35, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose()).bounds(left + 30, top + panelHeight - 27, 100, 20).build());
    }
    private void turn(int delta) {
        if (entries.isEmpty()) return;
        int count = entries.get(entryIndex).getAsJsonArray("pages").size();
        pageIndex = Math.floorMod(pageIndex + delta, count); scroll = layer = rotation = 0;
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xB0202434);
        g.fill(left, top, left + panelWidth, top + panelHeight, 0xFFF0E8D3);
        g.fill(left, top, left + panelWidth, top + 25, 0xFF263E63);
        g.centeredText(font, title, width / 2, top + 8, 0xFFFFFFFF);
        projectButton.visible = !entries.isEmpty() && entries.get(entryIndex).getAsJsonArray("pages").get(pageIndex).getAsJsonObject().get("type").getAsString().equals("multiblock");
        super.extractRenderState(g, mx, my, delta);
        if (entries.isEmpty()) return;
        JsonObject entry = entries.get(entryIndex);
        JsonArray pages = entry.getAsJsonArray("pages");
        JsonObject page = pages.get(pageIndex).getAsJsonObject();
        int x = left + 166, y = top + 35, w = panelWidth - 182;
        g.text(font, Component.literal(page.has("title") ? page.get("title").getAsString() : entry.get("name").getAsString()), x, y, 0xFF263E63, false);
        g.centeredText(font, Component.literal((pageIndex + 1) + " / " + pages.size()), left + 166 + w / 2, top + panelHeight - 21, 0xFF263E63);
        g.enableScissor(x, y + 15, x + w, top + panelHeight - 58);
        int contentY = y + 20 - scroll;
        String type = page.get("type").getAsString();
        if (type.equals("crafting")) contentY = crafting(g, page.get("recipe").getAsString(), x, contentY, mx, my);
        if (type.equals("spotlight")) { item(g, page.get("item").getAsString(), x, contentY, mx, my); contentY += 25; }
        if (type.equals("multiblock")) contentY = multiblock(g, page.getAsJsonObject("multiblock"), x, contentY, mx, my);
        if (page.has("text")) {
            String text = page.get("text").getAsString().replace("$(br2)", "\n\n").replace("$(br)", "\n").replace("$(li)", "\n• ").replaceAll("\\$\\([^)]*\\)", "");
            for (var line : font.split(Component.literal(text), w)) { g.text(font, line, x, contentY, 0xFF252C36, false); contentY += 11; }
        }
        g.disableScissor();
    }
    private int crafting(GuiGraphicsExtractor g, String id, int x, int y, int mx, int my) {
        try {
            var resource = minecraft.getResourceManager().getResource(Identifier.parse("alchemistry:guide/recipes/" + Identifier.parse(id).getPath() + ".json")).orElseThrow();
            try (var reader = resource.openAsReader()) {
                JsonObject recipe = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray pattern = recipe.getAsJsonArray("pattern"); JsonObject key = recipe.getAsJsonObject("key");
                for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++) {
                    int xx = x + col * 20, yy = y + row * 20;
                    g.fill(xx, yy, xx + 18, yy + 18, 0xFFB8B1A1);
                    if (row < pattern.size() && col < pattern.get(row).getAsString().length()) {
                        String symbol = pattern.get(row).getAsString().substring(col, col + 1);
                        if (key.has(symbol)) item(g, key.get(symbol).getAsString(), xx + 1, yy + 1, mx, my);
                    }
                }
                g.text(font, "→", x + 68, y + 24, 0xFF263E63);
                item(g, recipe.getAsJsonObject("result").get("id").getAsString(), x + 92, y + 21, mx, my);
            }
        } catch (Exception ex) { g.text(font, id, x, y, 0xFF263E63); }
        return y + 70;
    }
    private int multiblock(GuiGraphicsExtractor g, JsonObject block, int x, int y, int mx, int my) {
        JsonArray pattern = block.getAsJsonArray("pattern"); JsonObject mapping = block.getAsJsonObject("mapping");
        layer = Math.floorMod(layer, pattern.size());
        g.text(font, "Layer " + (pattern.size() - layer) + " / " + pattern.size() + " (top to bottom)", x, y, 0xFF263E63, false);
        g.text(font, "Click diagram: next layer • Right-click: rotate", x, y + 12, 0xFF555555, false);
        JsonArray rows = pattern.get(layer).getAsJsonArray();
        for (int row = 0; row < 5; row++) for (int col = 0; col < 5; col++) {
            int rr = row, cc = col;
            for (int n = 0; n < rotation; n++) { int tmp = rr; rr = 4 - cc; cc = tmp; }
            String symbol = rows.get(rr).getAsString().substring(cc, cc + 1);
            int xx = x + col * 22, yy = y + 30 + row * 22;
            g.fill(xx, yy, xx + 20, yy + 20, 0xFFD5CDB9);
            if (symbol.equals("0")) symbol = "C";
            if (mapping.has(symbol)) item(g, mapping.get(symbol).getAsString().split("\\[")[0], xx + 2, yy + 2, mx, my);
        }
        return y + 150;
    }
    private void item(GuiGraphicsExtractor g, String id, int x, int y, int mx, int my) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));
        g.item(stack, x, y);
        if (mx >= x && mx < x + 16 && my >= y && my < y + 16) g.setTooltipForNextFrame(font, stack, mx, my);
    }
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean twice) {
        if (!entries.isEmpty() && entries.get(entryIndex).getAsJsonArray("pages").get(pageIndex).getAsJsonObject().get("type").getAsString().equals("multiblock")
                && event.x() >= left + 166 && event.x() < left + 276 && event.y() >= top + 85 - scroll && event.y() < top + 195 - scroll) {
            if (event.button() == 1) rotation = (rotation + 1) % 4; else layer++;
            return true;
        }
        return super.mouseClicked(event, twice);
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        scroll = Math.clamp(scroll - (int)(vertical * 24), 0, 900); return true;
    }
    @Override public boolean isPauseScreen() { return false; }
}
