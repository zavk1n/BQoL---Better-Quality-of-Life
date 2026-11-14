package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterPrefixes;

import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Team.class)
public abstract class PREFIXESScoreboard {

    @Inject(method = "decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;", at = @At("RETURN"), cancellable = true)
    private static void bqol$formatScoreboardText(AbstractTeam team, Text original, CallbackInfoReturnable<MutableText> cir) {
        if (!BetterPrefixes.isEnabled()) {
            return;
        }

        MutableText decorated = cir.getReturnValue();

        if (decorated == null) {
            return;
        }

        Text formatted = BetterPrefixes.getInstance()
            .formatScoreboardText(decorated);

        if (formatted instanceof MutableText mutableText) {
            cir.setReturnValue(mutableText);
        }
    }
}