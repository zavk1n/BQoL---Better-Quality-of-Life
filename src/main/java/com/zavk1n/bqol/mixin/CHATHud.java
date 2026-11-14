package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterChat;

import net.minecraft.client.gui.hud.ChatHud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatHud.class)
public abstract class CHATHud {

    private static final int BQOL_MAX_HISTORY = 16384;

    @ModifyConstant(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", constant = @Constant(intValue = 100), require = 0)
    private int bqol$increaseMessageHistory(int original) {
        return BetterChat.isBetterChatMoreHistoryEnabled() ? BQOL_MAX_HISTORY : original;
    }

    @ModifyConstant(method = "addVisibleMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", constant = @Constant(intValue = 100),require = 0)
    private int bqol$increaseVisibleHistory(int original) {
        return BetterChat.isBetterChatMoreHistoryEnabled() ? BQOL_MAX_HISTORY : original;
    }
}
