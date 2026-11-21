package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterHitboxes;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class HITBOXEntityRenderer {

    private static final float BQOL_HITBOX_LINE_WIDTH = 3.5F;

    @ModifyVariable(method = "drawHitbox", at = @At("STORE"), ordinal = 0)
    private int bqol$modifyHitboxColor(int original) {
        if (!BetterHitboxes.isEnabled() || !BetterHitboxes.isHitboxesColorEnabled()) {
            return original;
        }

        if (original == -16711936) {
            return original;
        }

        return 0xFF000000 | BetterHitboxes.getHitboxesColor();
    }

    @ModifyConstant(method = "drawHitbox", constant = @Constant(intValue = -16776961))
    private int bqol$modifyLookDirectionColor(int original) {
        if (!BetterHitboxes.isEnabled() || !BetterHitboxes.isHitboxesColorEnabled()) {
            return original;
        }

        return 0xFF000000 | BetterHitboxes.getHitboxesColor();
    }

    @Redirect(
        method = "drawHitbox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/DrawStyle;stroked(I)Lnet/minecraft/client/render/DrawStyle;"
        )
    )
    private DrawStyle bqol$modifyHitboxLineWidth(int color) {
        if (!BetterHitboxes.isEnabled()) {
            return DrawStyle.stroked(color);
        }

        return DrawStyle.stroked(color, BQOL_HITBOX_LINE_WIDTH);
    }
}
