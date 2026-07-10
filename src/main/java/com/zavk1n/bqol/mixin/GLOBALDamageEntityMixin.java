package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterSprint;
import com.zavk1n.bqol.features.CustomHealth;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class GLOBALDamageEntityMixin {

    @Unique
    private float bqol$lastHealth = -1.0F;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        float currentHealth = player.getHealth() + player.getAbsorptionAmount();

        if (bqol$lastHealth >= 0.0F && currentHealth < bqol$lastHealth) {
            BetterSprint.onDamaged();
            CustomHealth.onDamaged();
        }

        bqol$lastHealth = currentHealth;
    }
}