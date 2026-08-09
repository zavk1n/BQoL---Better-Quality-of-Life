package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterSprint;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class SPRINTDisableShiftStop {

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    private void bqol$ignoreWaterSprintShift(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.options == null) {
            return;
        }

        KeyBinding thisKey = (KeyBinding) (Object) this;

        if (thisKey == client.options.sneakKey && BetterSprint.shouldDisableWaterSprintShiftStop()) {
            cir.setReturnValue(false);
        }
    }
}
