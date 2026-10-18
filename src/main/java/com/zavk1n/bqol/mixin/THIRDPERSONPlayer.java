package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterThirdPerson;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class THIRDPERSONPlayer {

    @Unique
    private static final float TURN_SPEED = 0.5F;

    @Unique
    private boolean bqol$rotationInitialized = false;

    @Unique
    private float bqol$rotationYaw;

    @Inject(method = "tick", at = @At("TAIL"))
    private void bqol$updatePlayerRotation(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        BetterThirdPerson feature = BetterThirdPerson.getInstance();

        if (client.player != player || !feature.shouldControlCamera()) {
            bqol$rotationInitialized = false;
            return;
        }

        if (player.input == null) {
            return;
        }

        float forward = player.input.movementForward;
        float sideways = player.input.movementSideways;

        if (!bqol$rotationInitialized) {
            bqol$rotationYaw = player.getYaw();

            bqol$rotationInitialized = true;
        }

        if (Math.abs(forward) < 0.001F && Math.abs(sideways) < 0.001F) {
            player.bodyYaw = bqol$rotationYaw;
            player.setHeadYaw(bqol$rotationYaw);

            return;
        }

        float cameraYaw = player.getYaw();
        float movementOffset = (float) Math.toDegrees(Math.atan2(-sideways, forward));
        float targetYaw = MathHelper.wrapDegrees(cameraYaw + movementOffset);

        float difference = MathHelper.wrapDegrees(targetYaw - bqol$rotationYaw);

        bqol$rotationYaw = MathHelper.wrapDegrees(bqol$rotationYaw + difference * TURN_SPEED);

        player.bodyYaw = bqol$rotationYaw;
        player.setHeadYaw(bqol$rotationYaw);
    }
}
