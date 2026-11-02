package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterTotems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(net.minecraft.client.render.entity.EntityRenderer.class)
public abstract class TOTEMCounterName {

    @ModifyArgs(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
        )
    )
    private void bqol$appendPopCounter(Args args) {
        if (!BetterTotems.isTotemCounterEnabled()) {
            return;
        }

        Entity entity = args.get(0);
        Text originalText = args.get(1);

        if (entity == null || originalText == null) {
            return;
        }

        if (entity instanceof PlayerEntity player) {
            int pops = BetterTotems.getInstance()
                .getPops(player.getUuid());

            if (pops > 0) {
                args.set(1, appendCounter(originalText, pops));
            }

            return;
        }

        if (entity instanceof ArmorStandEntity && entity.getWorld().isClient()) {
            String label = originalText.getString();

            for (PlayerEntity player : entity.getWorld().getPlayers()) {
                String playerName = player.getGameProfile().getName();

                if (playerName.isBlank() || !label.contains(playerName)) {
                    continue;
                }

                int pops = BetterTotems.getInstance()
                    .getPops(player.getUuid());

                if (pops > 0) {
                    args.set(1, appendCounter(originalText, pops));
                }

                break;
            }
        }
    }

    private static MutableText appendCounter(Text original, int pops) {
        MutableText result = original.copy();

        result.append(Text.literal(" | ").formatted(Formatting.GRAY));
        result.append(Text.literal("-" + pops)
            .styled(style -> style.withColor(getPopColor(pops)))
        );

        return result;
    }

    private static int getPopColor(int pops) {
        if (pops <= 2) return 0xFF55AA55;
        if (pops <= 4) return 0xFF00AA00;
        if (pops <= 6) return 0xFFFFFF55;
        if (pops <= 8) return 0xFFFF8800;
        return 0xFFFF5555;
    }
}