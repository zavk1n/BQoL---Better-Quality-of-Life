package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterChat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class CHATAnimation {

    @Unique
    private static final long BQOL_FADE_TIME_NANOS = 150_000_000L;

    @Unique
    private static final float BQOL_OFFSET_SCALE = 0.8F;

    @Unique
    private float bqol$animationOffset;

    @Unique
    private float bqol$animationAlpha = 1.0F;

    @Unique
    private long bqol$lastMessageTime;

    @Shadow
    private int scrolledLines;

    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    protected abstract int getLineHeight();

    @Unique
    private float bqol$getAnimationOffset() {
        return BetterChat.isBetterChatAnimationEnabled() ? bqol$animationOffset : 0.0F;
    }

    @Unique
    private float bqol$getAnimationAlpha() {
        return BetterChat.isBetterChatAnimationEnabled() ? bqol$animationAlpha : 1.0F;
    }

    @Unique
    private void bqol$updateAnimation() {
        if (!BetterChat.isBetterChatAnimationEnabled()) {
            bqol$animationOffset = 0.0F;
            bqol$animationAlpha = 1.0F;
            return;
        }

        if (scrolledLines != 0 || bqol$lastMessageTime == 0L) {
            bqol$animationOffset = 0.0F;
            bqol$animationAlpha = 1.0F;
            return;
        }

        long elapsed = System.nanoTime() - bqol$lastMessageTime;

        if (elapsed >= BQOL_FADE_TIME_NANOS) {
            bqol$animationOffset = 0.0F;
            bqol$animationAlpha = 1.0F;
            return;
        }

        float progress = Math.clamp(elapsed / (float) BQOL_FADE_TIME_NANOS, 0.0F, 1.0F);

        float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0);

        bqol$animationOffset = getLineHeight() * BQOL_OFFSET_SCALE * (1.0F - eased);
        bqol$animationAlpha = 0.5F + eased * 0.5F;
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;IIIZZ)V", at = @At("HEAD"))
    private void bqol$renderAnimation(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer, int currentTick, int mouseX, int mouseY, boolean interactable, boolean bool, CallbackInfo ci) {
        bqol$updateAnimation();
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("TAIL"))
    private void bqol$onMessageAdded(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
        if (BetterChat.isBetterChatAnimationEnabled()) {
            bqol$lastMessageTime = System.nanoTime();
        }
    }
}
