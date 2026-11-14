package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterLiquid;

import net.minecraft.client.render.fog.LavaFogModifier;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.RenderTickCounter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LavaFogModifier.class)
public abstract class LIQUIDLavaFogModifier {

    @Unique
    private static final AtmosphericFogModifier BQOL_ATMOSPHERIC_FOG = new AtmosphericFogModifier();

    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void bqol$modifyLavaFog(FogData data, Camera camera, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!BetterLiquid.isLavaTransparencyEnabled()) {
            return;
        }

        float transparency = Math.max(0.0F, Math.min(1.0F, BetterLiquid.getLavaTransparency() / 100.0F));

        float lavaStart = data.environmentalStart;
        float lavaEnd = data.environmentalEnd;

        BQOL_ATMOSPHERIC_FOG.applyStartEndModifier(data, camera, world, viewDistance, tickCounter);

        float atmosphericStart = data.environmentalStart;
        float atmosphericEnd = data.environmentalEnd;

        data.environmentalStart = bqol$lerp(atmosphericStart, lavaStart, transparency);
        data.environmentalEnd = bqol$lerp(atmosphericEnd, lavaEnd, transparency);
    }

    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private void bqol$modifyLavaFogColor(ClientWorld world, Camera camera, int viewDistance, float skyDarkness, CallbackInfoReturnable<Integer> cir) {
        int lavaColor = cir.getReturnValue() & 0xFFFFFF;
        float transparency = BetterLiquid.isLavaTransparencyEnabled() ? BetterLiquid.getLavaTransparency() / 100.0F : 1.0F;

        transparency = Math.max(0.0F, Math.min(1.0F, transparency));

        int atmosphericColor = BQOL_ATMOSPHERIC_FOG.getFogColor(world, camera, viewDistance, skyDarkness) & 0xFFFFFF;
        int resultColor = bqol$blendColor(atmosphericColor, lavaColor, transparency);

        if (BetterLiquid.isLavaColorEnabled()) {
            float intensity = BetterLiquid.isLavaColorIntensityEnabled() ? BetterLiquid.getLavaColorIntensity() / 100.0F : 1.0F;

            intensity = Math.max(0.0F, Math.min(1.0F, intensity));
            intensity *= transparency;

            resultColor = bqol$blendColor(resultColor, BetterLiquid.getLavaColor() & 0xFFFFFF, intensity);
        }

        cir.setReturnValue(resultColor);
    }

    @Unique
    private static float bqol$lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    @Unique
    private static int bqol$blendColor(int original, int custom, float strength) {
        int red = bqol$blendChannel((original >> 16) & 0xFF, (custom >> 16) & 0xFF, strength);

        int green = bqol$blendChannel((original >> 8) & 0xFF, (custom >> 8) & 0xFF, strength);
        int blue = bqol$blendChannel(original & 0xFF, custom & 0xFF, strength);

        return (red << 16) | (green << 8) | blue;
    }

    @Unique
    private static int bqol$blendChannel(int original, int custom, float strength) {
        return Math.round(original + (custom - original) * strength);
    }
}