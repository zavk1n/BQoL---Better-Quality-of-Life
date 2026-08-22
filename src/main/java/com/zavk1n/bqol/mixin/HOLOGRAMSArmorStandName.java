package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterHolograms;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntityRenderer.class)
public class HOLOGRAMSArmorStandName {

    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void bqol$hideHologramLabel(ArmorStandEntity armorStand, CallbackInfoReturnable<Boolean> cir) {
        if (!BetterHolograms.isEnabled() || !BetterHolograms.isHologram(armorStand)) {
            return;
        }

        if (BetterHolograms.isAntiHologramsEnabled()) {
            cir.setReturnValue(false);
            return;
        }
    }
}