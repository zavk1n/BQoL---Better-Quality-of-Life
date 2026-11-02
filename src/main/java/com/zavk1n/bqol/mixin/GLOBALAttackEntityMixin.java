package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterChat;
import com.zavk1n.bqol.features.BetterMovement;
import com.zavk1n.bqol.features.BetterHealth;
import com.zavk1n.bqol.features.BetterHitCounter;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class GLOBALAttackEntityMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (target == null
            || target == player
            || !(target instanceof PlayerEntity victim)) {
            return;
        }

        BetterHitCounter.onAttack(target);
        BetterMovement.onAttack(victim);
        BetterHealth.onAttack(victim);
        BetterChat.onAttack(victim);
    }
}