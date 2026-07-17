package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.config.BQoLConfig.RenderMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class NORENDERFireworks {

    @Inject(method = "addParticle", at = @At("HEAD"), cancellable = true)
    private void bqol$addParticle(ParticleEffect effect, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) {
            return;
        }

        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderEnabled() || config.getNoRenderFireworks() != RenderMode.NO_RENDER) {
            return;
        }

        ParticleType<?> type = effect.getType();

        if (type == ParticleTypes.FIREWORK) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}