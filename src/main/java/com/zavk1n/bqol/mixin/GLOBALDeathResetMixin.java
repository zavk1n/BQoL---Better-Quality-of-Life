package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.CustomHealth;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class GLOBALDeathResetMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (player == client.player || player.equals(CustomHealth.getLastAttackedPlayer())) {
                CustomHealth.resetDisplay();
            }
        }
    }
}

// v1.0