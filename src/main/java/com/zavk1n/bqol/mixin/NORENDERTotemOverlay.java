package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.config.BQoLConfig.RenderMode;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class NORENDERTotemOverlay {

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void bqol$showFloatingItem(ItemStack stack, CallbackInfo ci) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderEnabled()) {
            return;
        }

        if (config.getNoRenderTotemOverlay() == RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "renderFloatingItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V",
            ordinal = 0
        )
    )
    private void bqol$scaleFloatingItem(MatrixStack matrices, float x, float y, float z) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (config.isNoRenderEnabled() && config.getNoRenderTotemOverlay() == RenderMode.SMALL) {
            matrices.scale(x * 0.5F, y * 0.5F, z * 0.5F);
        } else {
            matrices.scale(x, y, z);
        }
    }
}