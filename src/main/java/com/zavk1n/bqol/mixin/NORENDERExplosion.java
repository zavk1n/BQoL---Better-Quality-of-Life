package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class NORENDERExplosion {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void bqol$disableExplosionParticles(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderExplosionEnabled() || config.getNoRenderExplosion() != BQoLConfig.RenderMode.NO_RENDER) {
            return;
        }

        if (parameters.getType() == ParticleTypes.EXPLOSION
                || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            cir.setReturnValue(null);
        }
    }
}