package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterSprint;

import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class SPRINTDisableShiftStops {

    @Inject( method = "adjustMovementForSneaking", at = @At("HEAD"), cancellable = true )
    private void bqol$disableSneakEdgeRestriction( Vec3d movement, MovementType type, CallbackInfoReturnable<Vec3d> cir ) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!BetterSprint.shouldDisableWaterSprintShiftStops(player)) {
            return;
        }

        cir.setReturnValue(movement);
    }
}
