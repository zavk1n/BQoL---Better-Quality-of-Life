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

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatHud.class)
public abstract class CHATAnimation {

    @Unique
    private static final float BQOL_FADE_TIME = 150.0F;

    @Unique
    private static final float BQOL_OFFSET_SCALE = 0.8F;

    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    private int scrolledLines;

    @Shadow
    protected abstract int getLineHeight();

    @Unique
    private final List<Long> bqol$messageTimestamps = new ArrayList<>();

    @Unique
    private float bqol$animationOffset;

    @Unique
    private float bqol$animationAlpha = 1.0F;

    @Unique
    private void bqol$updateAnimation() {
        if (!BetterChat.isBetterChatAnimationEnabled()) {
            bqol$animationOffset = 0.0F;
            bqol$animationAlpha = 1.0F;
            bqol$messageTimestamps.clear();
            return;
        }

        bqol$animationOffset = 0.0F;
        bqol$animationAlpha = 1.0F;

        if (scrolledLines != 0 || bqol$messageTimestamps.isEmpty()) {
            return;
        }

        long timestamp = bqol$messageTimestamps.get(0);

        float elapsed = Math.max(0.0F, Math.min(System.currentTimeMillis() - timestamp, BQOL_FADE_TIME));
        float progress = elapsed / BQOL_FADE_TIME;
        float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0F);

        bqol$animationOffset = getLineHeight() * BQOL_OFFSET_SCALE * (1.0F - eased);
        bqol$animationAlpha = 0.5F + eased * 0.5F;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void bqol$renderAnimation(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        bqol$updateAnimation();
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("TAIL"))
    private void bqol$onMessageAdded(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        if (refresh || !BetterChat.isBetterChatAnimationEnabled()) {
            return;
        }

        bqol$messageTimestamps.add(0, System.currentTimeMillis());

        while (bqol$messageTimestamps.size() > visibleMessages.size()) {
            bqol$messageTimestamps.remove(bqol$messageTimestamps.size() - 1);
        }
    }

    @Unique
    private float bqol$getAnimationOffset() {
        return BetterChat.isBetterChatAnimationEnabled() ? bqol$animationOffset : 0.0F;
    }

    @Unique
    private float bqol$getAnimationAlpha() {
        return BetterChat.isBetterChatAnimationEnabled() ? bqol$animationAlpha : 1.0F;
    }
}
