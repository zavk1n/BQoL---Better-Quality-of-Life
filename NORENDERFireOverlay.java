package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.config.BQoLConfig.RenderMode;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class NORENDERFireOverlay {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void bqol$renderFireOverlay(CallbackInfo ci) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderEnabled()) {
            return;
        }

        if (config.getNoRenderFireOverlay() == RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }

    @ModifyArg(
        method = "renderFireOverlay",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/BufferBuilder;vertex(Lorg/joml/Matrix4f;FFF)Lnet/minecraft/client/render/VertexConsumer;"
        ),
        index = 2 // y
    )
    private static float bqol$modifyFireOverlayY(float y) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderEnabled()) {
            return y;
        }

        return config.getNoRenderFireOverlay() == RenderMode.SMALL
            ? y * 0.33F
            : y;
    }
}
// v1.0