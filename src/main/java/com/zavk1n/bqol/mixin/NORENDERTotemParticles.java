package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.config.BQoLConfig.RenderMode;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class NORENDERTotemParticles {

    @Unique
    private static boolean bqol$skip;

    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
        at = @At("HEAD"),
        cancellable = true
    )

    private void bqol$addParticle(ParticleEffect effect, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
        if (effect != ParticleTypes.TOTEM_OF_UNDYING) {
            return;
        }

        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderTotemParticlesEnabled()) {
            return;
        }

        RenderMode mode = config.getNoRenderTotemParticles();

        switch (mode) {
            case FULL:
                return;

            case SMALL:
                bqol$skip = !bqol$skip;

                if (bqol$skip) {
                    cir.setReturnValue(null);
                    cir.cancel();
                }

                return;

            case NO_RENDER:
                cir.setReturnValue(null);
                cir.cancel();
                return;
        }
    }
}
// v1.0