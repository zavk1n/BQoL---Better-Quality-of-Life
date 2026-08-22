package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.utils.render.HologramRenderState;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntityRenderer.class)
public class HOLOGRAMSArmorStandAlpha {

    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/EntityModel;" + "render(" + "Lnet/minecraft/client/util/math/MatrixStack;" + "Lnet/minecraft/client/render/VertexConsumer;" + "IIFFFF" + ")V"
        ), index = 7
    )
    private float bqol$setHologramAlpha(float alpha) {
        if (HologramRenderState.isHologram()) {
            return 0.30F;
        }

        return alpha;
    }
}