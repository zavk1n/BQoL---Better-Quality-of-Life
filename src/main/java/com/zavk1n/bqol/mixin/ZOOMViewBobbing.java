package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterZoom;

import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public abstract class ZOOMViewBobbing {

    @Redirect(
        method = "renderWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"
        )
    )
    private Object bqol$disableViewBobbing(SimpleOption<Boolean> option) {
        if (option == net.minecraft.client.MinecraftClient.getInstance().options.getBobView() && BetterZoom.isZoomActive()) {
            return false;
        }

        return option.getValue();
    }
}