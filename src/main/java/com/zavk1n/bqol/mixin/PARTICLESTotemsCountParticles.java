package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterParticles;

import net.minecraft.client.network.ClientPlayNetworkHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class PARTICLESTotemsCountParticles {

    @ModifyConstant(method = "onEntityStatus", constant = @Constant(intValue = 30), require = 0)
    private int bqol$modifyTotemParticleCount(int original) {
        if (!BetterParticles.isTotemsEnabled() || !BetterParticles.isTotemsCountEnabled()) {
            return original;
        }

        return Math.max(1, Math.round(original * BetterParticles.getTotemsParticleMultiplier()));
    }
}