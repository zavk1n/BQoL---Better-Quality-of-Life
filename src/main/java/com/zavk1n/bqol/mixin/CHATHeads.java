package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterChat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.OrderedText;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ChatHud.class, priority = 1100)
public abstract class CHATHeads {

    @Unique
    private DrawContext bqol$currentContext;

    @Inject(method = "render", at = @At("HEAD"))
    private void bqol$captureContext(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        bqol$currentContext = context;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void bqol$releaseContext(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        bqol$currentContext = null;
    }

    @ModifyArgs(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"
        )
    )
    private void bqol$renderChatHead(Args args) {
        if (!BetterChat.isBetterChatHeadsEnabled() || bqol$currentContext == null) {
            return;
        }

        OrderedText orderedText = args.get(1);

        if (orderedText == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();

        orderedText.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });

        String message = builder.toString();

        int x = args.get(2);
        int y = args.get(3);

        int color = args.get(4);

        int alpha = (color >>> 24) & 0xFF;

        float opacity = alpha == 0 ? 1.0F : alpha / 255.0F;

        boolean rendered = BetterChat.renderHeadForMessage(bqol$currentContext, x, y - 1, message, opacity);

        if (rendered) {
            args.set(2, x + 10);
        }
    }
}