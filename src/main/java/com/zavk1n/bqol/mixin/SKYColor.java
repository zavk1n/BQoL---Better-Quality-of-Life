package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterSky;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.WeightedAttributeList;
import net.minecraft.world.attribute.WorldEnvironmentAttributeAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldEnvironmentAttributeAccess.class)
public abstract class SKYColor {

    @Inject(method = "getAttributeValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/attribute/WeightedAttributeList;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private void bqol$skyColor(EnvironmentAttribute<?> attribute, Vec3d pos, WeightedAttributeList pool, CallbackInfoReturnable<Object> cir) {
        if (attribute != EnvironmentAttributes.SKY_COLOR_VISUAL || !BetterSky.isSkyColorEnabled()) {
            return;
        }

        int red = MathHelper.clamp((int) (BetterSky.getRed() * 255.0), 0, 255);
        int green = MathHelper.clamp((int) (BetterSky.getGreen() * 255.0), 0, 255);
        int blue = MathHelper.clamp((int) (BetterSky.getBlue() * 255.0), 0, 255);

        cir.setReturnValue(0xFF000000 | (red << 16) | (green << 8) | blue);
    }
}