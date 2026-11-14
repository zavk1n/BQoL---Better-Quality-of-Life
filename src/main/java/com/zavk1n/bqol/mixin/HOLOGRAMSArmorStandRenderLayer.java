package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.accessors.ACCESSORHologramRenderState;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntityRenderer.class)
public abstract class HOLOGRAMSArmorStandRenderLayer {

    private static final Identifier HOLOGRAM_TEXTURE = Identifier.ofVanilla("textures/entity/armorstand/wood.png");

    @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
    private void bqol$useTranslucentLayer(ArmorStandEntityRenderState state, boolean showBody, boolean translucent, boolean showOutline, CallbackInfoReturnable<RenderLayer> cir) {
        if (((ACCESSORHologramRenderState) state).bqol$isHologram()) {
            cir.setReturnValue(RenderLayers.entityTranslucent(HOLOGRAM_TEXTURE, false));
        }
    }
}
