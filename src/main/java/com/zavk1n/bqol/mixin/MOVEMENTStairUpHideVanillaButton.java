package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterMovement;

import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ControlsOptionsScreen.class)
public abstract class MOVEMENTStairUpHideVanillaButton {

    @Inject(method = "getOptions", at = @At("RETURN"), cancellable = true)
    private static void bqol$hideAutoJumpButton(GameOptions gameOptions, CallbackInfoReturnable<SimpleOption<?>[]> cir) {
        if (!BetterMovement.isStairUpActive()) {
            return;
        }

        SimpleOption<?>[] original = cir.getReturnValue();
        SimpleOption<?> autoJump = gameOptions.getAutoJump();

        int count = 0;

        for (SimpleOption<?> option : original) {
            if (option != autoJump) {
                count++;
            }
        }

        if (count == original.length) {
            return;
        }

        SimpleOption<?>[] filtered = new SimpleOption<?>[count];
        int index = 0;

        for (SimpleOption<?> option : original) {
            if (option != autoJump) {
                filtered[index++] = option;
            }
        }

        cir.setReturnValue(filtered);
    }
}