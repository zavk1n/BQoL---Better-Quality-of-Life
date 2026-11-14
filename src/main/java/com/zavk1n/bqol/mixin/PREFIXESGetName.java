package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterPrefixes;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class PREFIXESGetName {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void bqol$formatPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (entry == null || !BetterPrefixes.isEnabled()) {
            return;
        }

        BetterPrefixes prefixes = BetterPrefixes.getInstance();
        String name = entry.getProfile().name();

        if (prefixes.isLocalPlayerName(name)) {
            cir.setReturnValue(prefixes.formatName(name));
        }
    }
}