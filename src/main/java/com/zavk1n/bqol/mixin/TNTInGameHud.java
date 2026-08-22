package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterTnt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(InGameHud.class)
public class TNTInGameHud {

    @Unique
    private float bqol$alertAlpha = 0.0F;

    @Unique
    private long bqol$lastTime = System.currentTimeMillis();

    @Unique
    private List<BetterTnt.AlertInfo> bqol$lastAlerts = Collections.emptyList();

    @Unique
    private static AlertPosition bqol$calculateAlertPosition(MinecraftClient mc, int textWidth, int panelHeight) {
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        int margin = 10;

        int x;
        int y;

        switch (BetterTnt.getAlertPosition()) {
            case 0 -> {
                // Верх-центр
                x = (screenWidth - textWidth) / 2;
                y = margin;
            }

            case 1 -> {
                // Право-верх
                x = screenWidth - textWidth - margin;
                y = margin;
            }

            case 2 -> {
                // Право-низ
                x = screenWidth - textWidth - margin;
                y = screenHeight - panelHeight - margin;
            }

            case 3 -> {
                // Лево-верх
                x = margin;
                y = margin;
            }

            case 4 -> {
                // Лево-низ
                x = margin;
                y = screenHeight - panelHeight - margin;
            }

            default -> {
                x = (screenWidth - textWidth) / 2;
                y = margin;
            }
        }

        return new AlertPosition(x, y);
    }

    @Unique
    private static class AlertPosition {
        final int x;
        final int y;

        AlertPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void bqol$renderAlert(DrawContext context, float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null || mc.world == null) {
            bqol$alertAlpha = 0.0F;
            bqol$lastAlerts = Collections.emptyList();
            return;
        }

        long now = System.currentTimeMillis();

        float delta = Math.min(0.1F, (now - bqol$lastTime) / 1000.0F);

        bqol$lastTime = now;

        List<BetterTnt.AlertInfo> alerts = BetterTnt.getAlertTnts(tickDelta);

        if (!alerts.isEmpty()) {
            bqol$lastAlerts = new ArrayList<>(alerts);

            bqol$alertAlpha += delta * 5.0F;
        } else {
            bqol$alertAlpha -= delta * 5.0F;
        }

        bqol$alertAlpha = Math.max(0.0F, Math.min(1.0F, bqol$alertAlpha));

        if (bqol$alertAlpha <= 0.0F || bqol$lastAlerts.isEmpty()) {
            if (bqol$alertAlpha <= 0.0F) {
                bqol$lastAlerts = Collections.emptyList();
            }

            return;
        }

        int lineHeight = mc.textRenderer.fontHeight + 8;
        int visibleCount = Math.min(bqol$lastAlerts.size(), 5);

        int panelHeight = visibleCount * lineHeight;

        if (bqol$lastAlerts.size() > 5) {
            panelHeight = visibleCount * lineHeight;
        }

        for (int i = 0; i < visibleCount; i++) {
            BetterTnt.AlertInfo alert = bqol$lastAlerts.get(i);

            String text = alert.getText();

            boolean lastLine = i == visibleCount - 1;
            boolean hasMore = bqol$lastAlerts.size() > 5;

            String suffix = "";

            if (lastLine && hasMore) {
                int extra = bqol$lastAlerts.size() - 5;
                suffix = " (и еще " + extra + " ТNТ)";
            }

            int textWidth = mc.textRenderer.getWidth(text);
            int suffixWidth = suffix.isEmpty() ? 0 : mc.textRenderer.getWidth(suffix);

            int totalWidth = textWidth + suffixWidth;

            AlertPosition position = bqol$calculateAlertPosition(mc, totalWidth, panelHeight);

            int x = position.x;
            boolean bottom = BetterTnt.getAlertPosition() == 2 || BetterTnt.getAlertPosition() == 4;
            int y;

            if (bottom) {
                y = position.y + (visibleCount - 1 - i) * lineHeight;
            } else {
                y = position.y + i * lineHeight;
            }

            int alpha = (int) (bqol$alertAlpha * 170.0F);
            int background = alpha << 24;

            context.fill(x - 6, y - 3, x + totalWidth + 6, y + mc.textRenderer.fontHeight + 3, background);

            int baseColor = BetterTnt.getTimerColor(alert.getType(), alert.getSeconds());
            int textAlpha = (int) (bqol$alertAlpha * 255.0F);
            int color = (textAlpha << 24) | (baseColor & 0x00FFFFFF);

            context.drawText(mc.textRenderer, Text.literal(text), x, y, color, true);

            if (!suffix.isEmpty()) {
                int suffixX = x + textWidth;
                int gray = (textAlpha << 24) | 0x00AAAAAA;

                context.drawText(mc.textRenderer, Text.literal(suffix), suffixX, y, gray, true);
            }
        }
    }

    @Unique
    private static int getTotalAlertTntCount(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            return 0;
        }

        var playerPos = mc.player.getPos();

        var searchBox = new net.minecraft.util.math.Box(
            playerPos.x - 32.0D,
            playerPos.y - 32.0D,
            playerPos.z - 32.0D,
            playerPos.x + 32.0D,
            playerPos.y + 32.0D,
            playerPos.z + 32.0D
        );

        return mc.world.getEntitiesByClass(net.minecraft.entity.TntEntity.class, searchBox, entity -> entity != null
                    && entity.isAlive()
                    && entity.getFuse() >= 0).size();
    }
}