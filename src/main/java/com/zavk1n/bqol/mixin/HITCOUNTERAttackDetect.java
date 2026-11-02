package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterHitCounter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class HITCOUNTERAttackDetect {

    @Shadow
    public HitResult crosshairTarget;

    @Inject(
        method = "doAttack",
        at = @At("HEAD")
    )
    private void bqol$detectMiss(CallbackInfoReturnable<Boolean> cir) {
        if (!BetterHitCounter.isEnabled()) {
            return;
        }

        if (!(crosshairTarget instanceof EntityHitResult)) {
            BetterHitCounter.onMissedAttack();
        }
    }
}