package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.Variables;
import com.zavk1n.bqol.config.MainConfig;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class NORENDERTotemOverlay {

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void bqol$showFloatingItem(ItemStack stack, CallbackInfo ci) {
        MainConfig config = MainConfig.getInstance();

        if (!config.isNoRenderTotemOverlayEnabled()) {
            return;
        }

        if (config.getNoRenderTotemOverlay() == Variables.RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }
}