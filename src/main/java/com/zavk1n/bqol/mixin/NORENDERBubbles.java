package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BubbleColumnBlock.class)
public class NORENDERBubbles {

    @Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
    private void bqol$disableBubbleParticles(net.minecraft.block.BlockState state, net.minecraft.world.World world, BlockPos pos, Random random, CallbackInfo ci) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderBubblesEnabled() || config.getNoRenderBubbles() != BQoLConfig.RenderMode.NO_RENDER) {
            return;
        }

        ci.cancel();
    }
}
// v1.0