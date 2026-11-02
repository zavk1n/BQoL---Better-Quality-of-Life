package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterChat;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class CHATStackMessages implements BetterChat.ChatStackAccess {

    @Shadow @Final
    private List<ChatHudLine> messages;

    @Shadow
    public abstract void reset();

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    private void bqol$stackMessages(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
        if (BetterChat.isStackUpdating()) {
            return;
        }

        ChatHud hud = (ChatHud) (Object) this;

        if (BetterChat.processStackMessage(hud, message, signature, indicator)) {
            ci.cancel();
        }
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void bqol$clearStack(boolean clearHistory, CallbackInfo ci) {
        BetterChat.resetStackMessages();
    }

    @Override
    public boolean bqol$removeLastStackMessage() {
        if (messages.isEmpty()) {
            return false;
        }

        messages.remove(0);

        reset();

        return true;
    }
}
