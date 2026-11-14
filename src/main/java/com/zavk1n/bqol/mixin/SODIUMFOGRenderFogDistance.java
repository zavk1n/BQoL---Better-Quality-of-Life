package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterFog;

import net.fabricmc.loader.api.FabricLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.lang.reflect.Constructor;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface", remap = false)
public abstract class SODIUMFOGRenderFogDistance {

    @ModifyArgs(
        method = "setupState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/shader/ChunkShaderFogComponent;setup(Lnet/caffeinemc/mods/sodium/client/util/FogParameters;)V",
            remap = false
        ),
        remap = false
    )
    private void bqol$modifySodiumFog(Args args) {
        if (!FabricLoader.getInstance().isModLoaded("sodium")) {
            return;
        }

        Object parameters = args.get(0);

        try {
            Class<?> fogClass = parameters.getClass();

            if (!BetterFog.isNoFogEnabled() && !BetterFog.isFogDistanceEnabled()) {
                return;
            }

            float red = (float) fogClass.getMethod("red").invoke(parameters);
            float green = (float) fogClass.getMethod("green").invoke(parameters);
            float blue = (float) fogClass.getMethod("blue").invoke(parameters);
            float alpha = (float) fogClass.getMethod("alpha").invoke(parameters);

            Constructor<?> constructor = fogClass.getConstructor(float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class);

            Object modified;

            if (BetterFog.isNoFogEnabled()) {
                float start = 1.0E20F;
                float end = 1.0E21F;

                modified = constructor.newInstance(red, green, blue, alpha, start, end, start, end);
            } else {
                float distance = Math.max(1.0F, BetterFog.getFogDistance() * 16.0F);

                float start = distance * 0.75F;
                float end = distance;

                modified = constructor.newInstance(red, green, blue, alpha, start, end, start, end);
            }

            args.set(0, modified);

        } catch (ReflectiveOperationException exception) {}
    }
}