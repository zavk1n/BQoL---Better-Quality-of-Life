package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterRCT;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class RCTConfigScreen extends MainConfigScreen{
    private ButtonWidget holyWorldRCTBtn, reallyWorldBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    public RCTConfigScreen(Screen parent) {
        super(Text.literal("Better RCT Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_rct")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        holyWorldRCTBtn = null;
        reallyWorldBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_rct_holyworld")) {
            holyWorldRCTBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterRCTHolyWorld() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterRCTHolyWorld();
                        config.setBetterRCTHolyWorld(state);
                        changed = true;
                        updateButton(holyWorldRCTBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(holyWorldRCTBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_rct_reallyworld")) {
            reallyWorldBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterRCTReallyWorld() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterRCTReallyWorld();
                        config.setBetterRCTReallyWorld(state);
                        changed = true;
                        updateButton(reallyWorldBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
            addDrawableChild(reallyWorldBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
                .dimensions(width / 2 - 50, height - 40, 100, 25)
                .build()
        );

        updateAllButtons();
    }

    private void updateAllButtons() {
        if (holyWorldRCTBtn != null) {
            updateButton(holyWorldRCTBtn, config.isBetterRCTHolyWorld());
        }

        if (reallyWorldBtn != null) {
            updateButton(reallyWorldBtn, config.isBetterRCTReallyWorld());
        }
    }

    private void updateButton(ButtonWidget btn, boolean enabled) {
        btn.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_rct_holyworld")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "RCT HolyWorld ",
                "Fast switching between anarchy servers on HolyWorld."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_rct_reallyworld")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "RCT ReallyWorld ",
                "Fast switching between anarchy servers on ReallyWorld."
            );
        }
    }

    private void renderLabel(DrawContext context, int x, int y, int mouseX, int mouseY, String title, String desc) {
        int titleWidth = textRenderer.getWidth(title);

        boolean hovered = mouseX >= x &&
            mouseX <= x + titleWidth &&
            mouseY >= y &&
            mouseY <= y + textRenderer.fontHeight;

        int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

        context.drawText(textRenderer, Text.literal(title), x, y, color, false);
        context.drawText(textRenderer, Text.literal(desc), x, y + 12, 0xFF888888, false);
    }

    private void save() {
        config.save();
        BetterRCT.refreshBlockedStatus();

        changed = false;
    }

    @Override
    public void close() {
        if (changed) {
            save();
        }

        if (client != null) {
            client.setScreen(parent);
        }
    }
}