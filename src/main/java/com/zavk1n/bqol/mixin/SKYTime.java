package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterSky;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.Properties.class)
public class SKYTime {

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void bqol$getTime(CallbackInfoReturnable<Long> cir) {

        if (!BetterSky.isEnabled()) {
            return;
        }

        cir.setReturnValue(BetterSky.getTime());
    }
}

