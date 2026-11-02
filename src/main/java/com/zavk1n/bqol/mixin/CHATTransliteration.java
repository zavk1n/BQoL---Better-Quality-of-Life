package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterChat;

import net.minecraft.client.network.ClientPlayNetworkHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class CHATTransliteration {

    @ModifyVariable(
        method = "sendChatMessage(Ljava/lang/String;)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private String bqol$transliterate(String message) {
        return BetterChat.transliterateOutgoingMessage(message);
    }
}
