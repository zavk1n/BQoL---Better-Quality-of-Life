package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterThirdPerson;

import net.minecraft.client.MinecraftClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class THIRDPERSONCameraSmooth {

    @Inject(method = "tick", at = @At("TAIL"))
    private void bqol$updateCamera(CallbackInfo ci) {
        BetterThirdPerson.getInstance().tick();
    }
}