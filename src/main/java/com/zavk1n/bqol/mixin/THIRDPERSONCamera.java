package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterThirdPerson;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class THIRDPERSONCamera {

    @Redirect(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getYaw(F)F"
        )
    )
    private float bqol$cameraYaw(Entity entity, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        BetterThirdPerson feature = BetterThirdPerson.getInstance();

        if (entity == client.player && feature.shouldControlCamera()) {
            return feature.getCameraYaw(tickDelta);
        }

        return entity.getYaw(tickDelta);
    }

    @Redirect(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getPitch(F)F"
        )
    )
    private float bqol$cameraPitch(Entity entity, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        BetterThirdPerson feature = BetterThirdPerson.getInstance();

        if (entity == client.player && feature.shouldControlCamera()) {
            return feature.getCameraPitch(tickDelta);
        }

        return entity.getPitch(tickDelta);
    }
}
