package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StuckObjectsFeatureRenderer.class)
public class NORENDERArrows {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void hidePlayerArrows(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float f, float g, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) {
            return;
        }

        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderArrowsEnabled() || config.getNoRenderArrows() != BQoLConfig.RenderMode.NO_RENDER) {
            return;
        }

        ci.cancel();
    }
}