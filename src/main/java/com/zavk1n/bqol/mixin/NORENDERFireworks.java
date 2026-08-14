package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworksSparkParticle.class)
public class NORENDERFireworks {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void bqol$noRenderFireworkParticle(VertexConsumer vertexConsumer, Camera camera, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) {
            return;
        }

        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderFireworksEnabled() || config.getNoRenderFireworks() != BQoLConfig.RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }
}
// v1.0