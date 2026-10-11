package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterPrefixes;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class PREFIXESAddName {

    @Unique
    private boolean bqol$processingMessage;

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void bqol$formatChatName(Text message, CallbackInfo ci) {
        if (bqol$processingMessage || !BetterPrefixes.isEnabled() || message == null) {
            return;
        }

        Text formatted = BetterPrefixes.getInstance().formatMessage(message);

        if (formatted == message) {
            return;
        }

        ci.cancel();

        bqol$processingMessage = true;

        try {
            ((ChatHud) (Object) this).addMessage(formatted);
        } finally {
            bqol$processingMessage = false;
        }
    }
}
