package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterChat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.hud.ChatHud$Interactable")
public abstract class CHATHeadsInteractable {

    @Shadow
    @Final
    private DrawContext context;

    @Unique
    private boolean bqol$headRendered;

    @Inject(method = "text", at = @At("HEAD"))
    private void bqol$renderHead(int y, float opacity, OrderedText text, CallbackInfoReturnable<Boolean> cir) {
        bqol$headRendered = false;

        if (!BetterChat.isBetterChatHeadsEnabled() || text == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();

        text.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });

        bqol$headRendered = BetterChat.renderHeadForMessage(context, 4, y - 1, builder.toString(), opacity);
    }

    @ModifyArg(
        method = "text",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/font/DrawnTextConsumer;text(Lnet/minecraft/client/font/Alignment;IILnet/minecraft/client/font/DrawnTextConsumer$Transformation;Lnet/minecraft/text/OrderedText;)V"
        ),
        index = 1
    )
    private int bqol$moveText(int x) {
        return bqol$headRendered ? x + 18 : x;
    }
}