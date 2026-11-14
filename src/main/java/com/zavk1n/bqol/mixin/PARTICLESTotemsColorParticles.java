package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterParticles;

import net.minecraft.client.particle.TotemParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TotemParticle.class)
public abstract class PARTICLESTotemsColorParticles {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bqol$customTotemColor(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider, CallbackInfo ci) {
        if (!BetterParticles.isTotemsEnabled()
            || !BetterParticles.isTotemsColorEnabled()) {
            return;
        }

        int color = BetterParticles.getRandomTotemsColor();

        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        ((TotemParticle) (Object) this).setColor(red, green, blue);
    }
}