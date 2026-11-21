package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.MainConfig;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.render.Camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net.minecraft.client.particle.FireworksSparkParticle$Explosion", "net.minecraft.client.particle.FireworksSparkParticle$Flash"})
public abstract class NORENDERFireworks {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void bqol$noRenderFireworkParticle(BillboardParticleSubmittable submittable, Camera camera, float tickProgress, CallbackInfo ci) {
        MainConfig config = MainConfig.getInstance();

        if (!config.isNoRenderFireworksEnabled()) {
            return;
        }

        if (config.getNoRenderFireworks() == MainConfig.RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }
}