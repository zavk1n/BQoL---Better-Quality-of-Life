package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterHolograms;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntityRenderer.class)
public class HOLOGRAMSArmorStandRenderLayer {

    @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
    private void bqol$useTranslucentLayer(ArmorStandEntity armorStand, boolean showBody, boolean translucent, boolean showOutline, CallbackInfoReturnable<RenderLayer> cir) {
        if (!BetterHolograms.isVisibleArmorStandEnabled() || !BetterHolograms.isHologram(armorStand)) {
            return;
        }

        cir.setReturnValue(RenderLayer.getEntityTranslucent(ArmorStandEntityRenderer.TEXTURE, false));
    }
}