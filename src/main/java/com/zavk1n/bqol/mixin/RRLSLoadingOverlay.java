package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterRRLS;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(SplashOverlay.class)
public abstract class RRLSLoadingOverlay {

    @Unique
    private boolean bqol$hidden;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bqol$init(MinecraftClient client, ResourceReload reload, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading, CallbackInfo ci) {
        BetterRRLS feature = BetterRRLS.getInstance();

        feature.beginReload(!reloading);

        bqol$hidden = feature.shouldHideOverlay();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void bqol$beforeRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!bqol$hidden || !BetterRRLS.isEnabled()) {
            return;
        }

        context.enableScissor(0, 0, 0, 0);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void bqol$afterRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (bqol$hidden) {
            context.disableScissor();
        }
    }
}