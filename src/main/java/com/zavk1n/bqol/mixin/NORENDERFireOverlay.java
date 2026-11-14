package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.config.BQoLConfig.RenderMode;

import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class NORENDERFireOverlay {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void bqol$renderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Sprite sprite, CallbackInfo ci) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (config.isNoRenderFireOverlayEnabled() && config.getNoRenderFireOverlay() == RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }

    @Overwrite
    private static void renderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Sprite sprite) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (config.isNoRenderFireOverlayEnabled() && config.getNoRenderFireOverlay() == RenderMode.NO_RENDER) {
            return;
        }

        float scale = 1.0F;

        if (config.isNoRenderFireOverlayEnabled() && config.getNoRenderFireOverlay() == RenderMode.SMALL) {
            scale = 0.33F;
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayers.fireScreenEffect(sprite.getAtlasId()));

        float f = sprite.getMinU();
        float g = sprite.getMaxU();
        float h = sprite.getMinV();
        float i = sprite.getMaxV();

        for (int k = 0; k < 2; k++) {
            matrices.push();

            matrices.translate(-(k * 2 - 1) * 0.24F, -0.3F, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((k * 2 - 1) * 10.0F));
            matrices.scale(scale, scale, scale);

            Matrix4f matrix4f = matrices.peek().getPositionMatrix();

            vertexConsumer.vertex(matrix4f, -0.5F, -0.5F, -0.5F)
                .texture(g, i).color(1.0F, 1.0F, 1.0F, 0.9F);
            vertexConsumer.vertex(matrix4f, 0.5F, -0.5F, -0.5F)
                .texture(f, i).color(1.0F, 1.0F, 1.0F, 0.9F);
            vertexConsumer.vertex(matrix4f, 0.5F, 0.5F, -0.5F)
                .texture(f, h).color(1.0F, 1.0F, 1.0F, 0.9F);
            vertexConsumer.vertex(matrix4f, -0.5F, 0.5F, -0.5F)
                .texture(g, h).color(1.0F, 1.0F, 1.0F, 0.9F);

            matrices.pop();
        }
    }
}