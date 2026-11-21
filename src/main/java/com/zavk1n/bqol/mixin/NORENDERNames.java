package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.MainConfig;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class NORENDERNames {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void bqol$hideNames(PlayerEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        MainConfig config = MainConfig.getInstance();

        if (!config.isNoRenderNamesEnabled() || config.getNoRenderNames() != MainConfig.RenderMode.NO_RENDER) {
            return;
        }

        ci.cancel();
    }
}
