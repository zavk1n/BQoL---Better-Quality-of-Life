package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterSounds;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SOUNDSPlaySound {

    @Inject(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onPlaySound(SoundInstance soundInstance, CallbackInfo ci) {
        if (!BetterSounds.isEnabled() || soundInstance == null) {
            return;
        }

        Identifier soundId;

        try {
            soundId = soundInstance.getId();
        } catch (Exception e) {
            return;
        }

        if (soundId == null) {
            return;
        }

        BetterSounds betterSounds = BetterSounds.getInstance();

        if (betterSounds == null) {
            return;
        }

        String path = soundId.getPath();

        if (!betterSounds.shouldPlaySound(path)) {
            ci.cancel();
        }
    }
}
// v1.0