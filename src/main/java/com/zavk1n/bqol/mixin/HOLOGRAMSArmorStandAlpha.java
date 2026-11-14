package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterHolograms;
import net.minecraft.client.render.entity.LivingEntityRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntityRenderer.class)
public abstract class HOLOGRAMSArmorStandAlpha {

    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitModel(" +
                "Lnet/minecraft/client/model/Model;" +
                "Ljava/lang/Object;" +
                "Lnet/minecraft/client/util/math/MatrixStack;" +
                "Lnet/minecraft/client/render/RenderLayer;" +
                "III" +
                "Lnet/minecraft/client/texture/Sprite;" +
                "I" +
                "Lnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;" +
                ")V"
        ),
        index = 6
    )
    private int bqol$setHologramAlpha(int color) {
        return BetterHolograms.isCurrentHologram()
            ? (color & 0x00FFFFFF) | 0x4D000000
            : color;
    }
}