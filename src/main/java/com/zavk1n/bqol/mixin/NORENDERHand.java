package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.config.BQoLConfig.RenderMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class NORENDERHand {

    @Inject(method = "renderArmHoldingItem", at = @At("HEAD"), cancellable = true)
    private void bqol$renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (MinecraftClient.getInstance().player != null
            && config.isNoRenderEnabled()
            && config.getNoRenderHand() == RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }

    @Inject(method = "renderArm", at = @At("HEAD"), cancellable = true)
    private void bqol$renderArm(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        Arm arm,
        CallbackInfo ci) {

        BQoLConfig config = BQoLConfig.getInstance();

        if (MinecraftClient.getInstance().player != null
            && config.isNoRenderEnabled()
            && config.getNoRenderHand() == RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }
}