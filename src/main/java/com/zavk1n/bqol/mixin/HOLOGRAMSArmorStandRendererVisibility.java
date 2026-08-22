package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterHolograms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class HOLOGRAMSArmorStandRendererVisibility {

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    private void bqol$makeHologramVisible(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;

        if (!BetterHolograms.isVisibleArmorStandEnabled()
                || !(entity instanceof ArmorStandEntity armorStand)
                || !BetterHolograms.isHologram(armorStand)) {
            return;
        }

        cir.setReturnValue(false);
    }
}