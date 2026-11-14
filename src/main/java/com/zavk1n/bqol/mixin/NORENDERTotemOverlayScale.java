package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;

import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameOverlayRenderer.class)
public class NORENDERTotemOverlayScale {

    @Redirect(
        method = "renderFloatingItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"
        )
    )
    private void bqol$scaleFloatingItem(MatrixStack matrices, float x, float y, float z) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderTotemOverlayEnabled()) {
            matrices.scale(x, y, z);
            return;
        }

        switch (config.getNoRenderTotemOverlay()) {
            case SMALL -> matrices.scale(x * 0.5F, y * 0.5F, z * 0.5F);
            case FULL, NO_RENDER -> matrices.scale(x, y, z);
        }
    }
}