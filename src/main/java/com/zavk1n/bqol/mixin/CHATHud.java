package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterChat;

import net.minecraft.client.gui.hud.ChatHud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatHud.class)
public abstract class CHATHud {
    private static final int MAX_HISTORY = 16384;

    @ModifyConstant(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", constant = @Constant(intValue = 100))
    private int bqol$moreHistory(int original) {
        if (!BetterChat.isEnabled() || !BetterChat.isBetterChatMoreHistoryEnabled()) {
            return original;
        }

        return MAX_HISTORY;
    }
}