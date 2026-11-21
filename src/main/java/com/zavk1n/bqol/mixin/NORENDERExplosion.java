package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.MainConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class NORENDERExplosion {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void bqol$disableExplosionParticles(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
        MainConfig config = MainConfig.getInstance();

        if (!config.isNoRenderExplosionEnabled()
            || config.getNoRenderExplosion() != MainConfig.RenderMode.NO_RENDER
            || parameters == null) {
            return;
        }

        if (parameters.getType() == ParticleTypes.EXPLOSION
            || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER
            || parameters.getType() == ParticleTypes.GUST_EMITTER_LARGE
            || parameters.getType() == ParticleTypes.GUST_EMITTER_SMALL
            || parameters.getType() == ParticleTypes.GUST
            || parameters.getType() == ParticleTypes.POOF
            || parameters.getType() == ParticleTypes.SMOKE
            || parameters.getType() == ParticleTypes.LARGE_SMOKE
            || parameters.getType() == ParticleTypes.CAMPFIRE_COSY_SMOKE) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void bqol$disableExplosionParticle(Particle particle, CallbackInfo ci) {
        MainConfig config = MainConfig.getInstance();

        if (!config.isNoRenderExplosionEnabled() || config.getNoRenderExplosion() != MainConfig.RenderMode.NO_RENDER) {
            return;
        }

        if (particle instanceof net.minecraft.client.particle.ExplosionEmitterParticle
            || particle instanceof net.minecraft.client.particle.ExplosionLargeParticle
            || particle instanceof net.minecraft.client.particle.ExplosionSmokeParticle
            || particle instanceof net.minecraft.client.particle.GustParticle
            || particle instanceof net.minecraft.client.particle.GustEmitterParticle
            || particle instanceof net.minecraft.client.particle.FireSmokeParticle) {
            ci.cancel();
        }
    }
}