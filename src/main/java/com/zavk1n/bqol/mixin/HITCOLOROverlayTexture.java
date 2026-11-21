package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.accessors.ACCESSORHitColorOverlay;
import com.zavk1n.bqol.features.BetterHitColor;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverlayTexture.class)
public abstract class HITCOLOROverlayTexture implements ACCESSORHitColorOverlay {

    @Shadow
    @Final
    private NativeImageBackedTexture texture;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bqol$init(CallbackInfo ci) {
        bqol$reload();
    }

    @Override
    public void bqol$reload() {
        NativeImage image = texture.getImage();

        if (image == null) {
            return;
        }

        boolean enabled = BetterHitColor.isHitColorColorEnabled();

        int color = enabled ? BetterHitColor.getHitColorColor() : 0xFF0000;

        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 16; x++) {
                int original = image.getColorArgb(x, y);
                int alpha = (original >>> 24) & 0xFF;

                image.setColorArgb(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }

        texture.upload();
    }
}