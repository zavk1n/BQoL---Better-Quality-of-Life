package com.zavk1n.bqol.mixin;


import com.zavk1n.bqol.features.BetterSky;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class SKYColor {

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void bqol$skyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {

        if (!BetterSky.isEnabled()) {
            return;
        }

        cir.setReturnValue(new Vec3d(
            BetterSky.getRed(),
            BetterSky.getGreen(),
            BetterSky.getBlue()
        ));
    }
}

