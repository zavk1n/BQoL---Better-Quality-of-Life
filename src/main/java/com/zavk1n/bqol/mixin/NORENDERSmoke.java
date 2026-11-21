package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.MainConfig;
import net.minecraft.block.CampfireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
public class NORENDERSmoke {

    @Inject(method = "spawnSmokeParticle", at = @At("HEAD"), cancellable = true)
    private static void bqol$cancelSmoke(World world, BlockPos pos, boolean isSignal, boolean lotsOfSmoke, CallbackInfo ci) {
        MainConfig config = MainConfig.getInstance();

        if (config.isNoRenderSmokeEnabled() || config.getNoRenderSmoke() == MainConfig.RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }
}