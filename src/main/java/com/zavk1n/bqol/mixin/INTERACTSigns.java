package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.features.BetterInteract;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class INTERACTSigns {

    @Inject(method = "onSignEditorOpen", at = @At("HEAD"), cancellable = true)
    private void bqol$skipSignEditor(SignEditorOpenS2CPacket packet, CallbackInfo ci) {
        /// AUTO-SIGNS
        if (BetterInteract.isAutoSignsEnabled()) {
            BQoLConfig config = BQoLConfig.getInstance();

            String text = config.getBetterInteractAutoSignsText();

            if (text != null && !text.isBlank()) {
                String[] lines = text.split("\\R", -1);

                String line1 = lines.length > 0 ? lines[0] : "";
                String line2 = lines.length > 1 ? lines[1] : "";
                String line3 = lines.length > 2 ? lines[2] : "";
                String line4 = lines.length > 3 ? lines[3] : "";

                MinecraftClient client = MinecraftClient.getInstance();

                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendPacket(
                        new UpdateSignC2SPacket(
                            packet.getPos(),
                            packet.isFront(),
                            line1,
                            line2,
                            line3,
                            line4
                        )
                    );
                }

                ci.cancel();
                return;
            }
        }

        /// ANTI-SIGNS
        if (!BetterInteract.isAntiSignsEnabled()) {
            return;
        }

        ci.cancel();
    }
}
// v1.0