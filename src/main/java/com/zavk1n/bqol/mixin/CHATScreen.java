package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterChat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class CHATScreen extends Screen {

    @Unique
    private static final float BQOL_FADE_TIME = 170.0F;

    @Unique
    private static final float BQOL_FADE_OFFSET = 8.0F;

    @Unique
    private long bqol$openTime;

    @Unique
    private boolean bqol$matrixPushed = false;

    protected CHATScreen() {
        super(Text.empty());
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void bqol$initAnimation(CallbackInfo ci) {
        bqol$openTime = System.currentTimeMillis();
        bqol$matrixPushed = false;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void bqol$renderAnimation(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        bqol$matrixPushed = false;

        if (!BetterChat.isBetterChatAnimationEnabled()) {
            return;
        }

        float elapsed = Math.max(0.0F, Math.min(System.currentTimeMillis() - bqol$openTime, BQOL_FADE_TIME));
        float progress = elapsed / BQOL_FADE_TIME;
        float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0F);
        float offsetY = (1.0F - eased) * BQOL_FADE_OFFSET;

        if (offsetY <= 0.001F) {
            return;
        }

        context.getMatrices().push();

        bqol$matrixPushed = true;

        context.getMatrices().translate(0.0F, offsetY, 0.0F);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void bqol$renderAnimationEnd(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (bqol$matrixPushed) {
            context.getMatrices().pop();
            bqol$matrixPushed = false;
        }
    }
}