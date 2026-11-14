package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.config.BQoLConfig.RenderMode;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderManager.class)
public class NORENDERPlayers {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void bqol$render(EntityRenderState state, CameraRenderState cameraState, double x, double y, double z, MatrixStack matrices, OrderedRenderCommandQueue queue, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.player == null) {
            return;
        }

        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderPlayersEnabled()
            || config.getNoRenderPlayers() != RenderMode.NO_RENDER
            || !(state instanceof PlayerEntityRenderState playerState)
            || playerState.id == client.player.getId()) {
            return;
        }

        ci.cancel();
    }
}