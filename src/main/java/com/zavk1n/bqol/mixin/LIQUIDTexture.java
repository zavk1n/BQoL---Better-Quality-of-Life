package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterLiquid;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.class)
public abstract class LIQUIDTexture {

    @Shadow @Final private NativeImage image;
    @Shadow @Final private Identifier id;

    @Inject(method = "<init>(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/texture/SpriteDimensions;Lnet/minecraft/client/texture/NativeImage;Ljava/util/Optional;Ljava/util/List;Ljava/util/Optional;)V", at = @At("TAIL"))
    private void bqol$recolorLiquid(CallbackInfo ci) {
        if (!id.getNamespace().equals("minecraft")) {
            return;
        }

        String path = id.getPath();

        boolean water = path.equals("block/water_still") || path.equals("block/water_flow") || path.equals("block/water_overlay");
        boolean lava = path.equals("block/lava_still") || path.equals("block/lava_flow");

        if (!water && !lava) {
            return;
        }

        boolean colorEnabled = water ? BetterLiquid.isWaterColorEnabled() : BetterLiquid.isLavaColorEnabled();
        boolean alphaEnabled = water ? BetterLiquid.isWaterTransparencyEnabled() : BetterLiquid.isLavaTransparencyEnabled();

        if (!colorEnabled && !alphaEnabled) {
            return;
        }

        int customColor = water ? BetterLiquid.getWaterColor() : BetterLiquid.getLavaColor();

        int targetRed = (customColor >> 16) & 255;
        int targetGreen = (customColor >> 8) & 255;
        int targetBlue = customColor & 255;

        float intensity = 1.0F;

        if (colorEnabled) {
            boolean intensityEnabled = water ? BetterLiquid.isWaterColorIntensityEnabled() : BetterLiquid.isLavaColorIntensityEnabled();

            if (intensityEnabled) {
                intensity = (water ? BetterLiquid.getWaterColorIntensity() : BetterLiquid.getLavaColorIntensity()) / 100.0F;
            }
        } else {
            intensity = 0.0F;
        }

        float alphaMultiplier = alphaEnabled ? (water ? BetterLiquid.getWaterAlpha() : BetterLiquid.getLavaAlpha()) : 1.0F;

        intensity = Math.max(0.0F, Math.min(1.0F, intensity));
        alphaMultiplier = Math.max(0.0F, Math.min(1.0F, alphaMultiplier));

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int original = image.getColorArgb(x, y);

                int alpha = (original >>> 24) & 255;

                if (alpha == 0) {
                    continue;
                }

                int red = (original >> 16) & 255;
                int green = (original >> 8) & 255;
                int blue = original & 255;

                if (colorEnabled) {
                    float luminance = (red * 0.2126F + green * 0.7152F + blue * 0.0722F) / 255.0F;

                    int recoloredRed = Math.round(targetRed * luminance);
                    int recoloredGreen = Math.round(targetGreen * luminance);
                    int recoloredBlue = Math.round(targetBlue * luminance);

                    red = Math.round(red + (recoloredRed - red) * intensity);
                    green = Math.round(green + (recoloredGreen - green) * intensity);
                    blue = Math.round(blue + (recoloredBlue - blue) * intensity);
                }

                if (alphaEnabled) {
                    alpha = Math.round(alpha * alphaMultiplier);
                }

                int result = (alpha << 24) | (red << 16) | (green << 8) | blue;

                image.setColorArgb(x, y, result);
            }
        }
    }
}