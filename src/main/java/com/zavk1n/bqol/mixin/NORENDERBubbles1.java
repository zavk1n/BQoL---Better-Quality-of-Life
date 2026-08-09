package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class NORENDERBubbles1 {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void bqol$removeWaterParticles(ParticleEffect particle, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderBubblesEnabled() || config.getNoRenderBubbles() != BQoLConfig.RenderMode.NO_RENDER) {
            return;
        }

        if (particle == ParticleTypes.BUBBLE
            || particle == ParticleTypes.BUBBLE_POP
            || particle == ParticleTypes.SPLASH
            || particle == ParticleTypes.CURRENT_DOWN) {
            ci.cancel();
        }
    }
}
// v1.0