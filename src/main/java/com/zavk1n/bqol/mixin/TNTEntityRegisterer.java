package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterTnt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntity.class)
public class TNTEntityRegisterer {

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    private void bqol$register(EntityType<? extends TntEntity> type, World world, CallbackInfo ci) {
        BetterTnt.registerTnt((TntEntity) (Object) this);
    }
}