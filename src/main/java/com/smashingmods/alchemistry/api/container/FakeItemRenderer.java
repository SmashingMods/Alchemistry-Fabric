package com.smashingmods.alchemistry.api.container;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smashingmods.chemlib.client.ElementRenderer;
import com.smashingmods.chemlib.common.items.ElementItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.Vec3f;

public class FakeItemRenderer {

    private static final MinecraftClient MINECRAFT = MinecraftClient.getInstance();
    private static final ItemRenderer ITEM_RENDERER = MINECRAFT.getItemRenderer();
    private static final TextureManager TEXTURE_MANAGER = MINECRAFT.getTextureManager();

    public static void renderFakeItem(ItemStack pItemStack, int pX, int pY, float pAlpha) {

        if (!pItemStack.isEmpty()) {
            BakedModel model;

            if (pItemStack.getItem() instanceof ElementItem element) {
                ModelIdentifier elementModel =
                        switch (element.getMatterState()) {
                            case LIQUID -> ElementRenderer.LIQUID_MODEL_LOCATION;
                            case SOLID -> ElementRenderer.SOLID_MODEL_LOCATION;
                            case GAS -> ElementRenderer.GAS_MODEL_LOCATION;
                        };

                model = ITEM_RENDERER.getModels().getModelManager().getModel(elementModel);
            } else {
                model = ITEM_RENDERER.getModel(pItemStack, null, MINECRAFT.player, 0);
            }

            TEXTURE_MANAGER.getTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).setFilter(true, false);
            RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.enableBlend();

            MatrixStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.push();
            modelViewStack.translate(pX + 8.0D, pY + 8.0D, 0F);
            modelViewStack.scale(16.0F, -16.0F, 16.0F);
            RenderSystem.applyModelViewMatrix();

            if (!model.isSideLit()) {
                DiffuseLighting.disableGuiDepthLighting();
            }

            VertexConsumerProvider.Immediate bufferSource = MINECRAFT.getBufferBuilders().getEntityVertexConsumers();
            MatrixStack stack = new MatrixStack();
            ITEM_RENDERER.renderItem(pItemStack,
                    ModelTransformation.Mode.GUI,
                    false,
                    stack,
                    getWrappedBuffer(bufferSource, pAlpha),
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV,
                    model);
            bufferSource.draw();

            if (pItemStack.getItem() instanceof ElementItem element) {
                stack.push();
                stack.multiply(Vec3f.NEGATIVE_X.getRadialQuaternion(180));
                stack.translate(-0.16D, 0, -0.55D);
                stack.scale(0.05F, 0.08F, 0.08F);
                MINECRAFT.textRenderer.drawWithShadow(stack, element.getAbbreviation(), -5, 0, 0xFFFFFF);
                stack.pop();
            }

            RenderSystem.enableDepthTest();

            if (!model.isSideLit()) {
                DiffuseLighting.enableGuiDepthLighting();
            }

            modelViewStack.pop();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public static VertexConsumerProvider getWrappedBuffer(VertexConsumerProvider bufferSource, float alpha) {
        return pRenderType -> new WrappedVertexConsumer(bufferSource.getBuffer(RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)), 1F, 1F, 1F, alpha);
    }
}

class WrappedVertexConsumer implements VertexConsumer {

    protected final VertexConsumer consumer;
    protected final float red;
    protected final float green;
    protected final float blue;
    protected final float alpha;

    public WrappedVertexConsumer(VertexConsumer consumer, float red, float green, float blue, float alpha) {
        this.consumer = consumer;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer vertex(double pX, double pY, double pZ) {
        return consumer.vertex(pX, pY, pZ);
    }

    @Override
    public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
        return consumer.color((int) (pRed * red), (int) (pGreen * green), (int) (pBlue * blue), (int) (pAlpha * alpha));
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return consumer.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return consumer.overlay(u, v);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return consumer.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return consumer.normal(x, y, z);
    }

    @Override
    public void next() {
        consumer.next();
    }

    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {
        consumer.fixedColor(red, green, blue, alpha);
    }

    @Override
    public void unfixColor() {
        consumer.unfixColor();
    }
}
