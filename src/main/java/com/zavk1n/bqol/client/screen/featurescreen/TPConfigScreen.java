package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterTP;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TPConfigScreen extends MainConfigScreen{
    private ButtonWidget autoAcceptBtn, autoDenyBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    public TPConfigScreen(Screen parent) {
        super(Text.literal("Better TP Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_tp")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        autoAcceptBtn = null;
        autoDenyBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_tp_auto_accept")) {
            autoAcceptBtn = ButtonWidget.builder(Text.empty(), button -> toggleAutoAccept())
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(autoAcceptBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_tp_auto_deny")) {

            autoDenyBtn = ButtonWidget.builder(Text.empty(), button -> toggleAutoDeny())
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(autoDenyBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(width / 2 - 50, height - 40, 100, 25)
            .build()
        );

        updateAllButtons();
    }

    private void toggleAutoAccept() {
        boolean newState = !config.isBetterTPAutoAccept();

        config.setBetterTPAutoAccept(newState);

        if (newState) {
            config.setBetterTPAutoDeny(false);
        }

        changed = true;

        save();
        updateAllButtons();
    }

    private void toggleAutoDeny() {
        boolean newState = !config.isBetterTPAutoDeny();

        config.setBetterTPAutoDeny(newState);

        if (newState) {
            config.setBetterTPAutoAccept(false);
        }

        changed = true;

        save();
        updateAllButtons();
    }

    private void updateAllButtons() {
        if (autoAcceptBtn != null) {
            boolean enabled = config.isBetterTPAutoAccept();

            updateButton(autoAcceptBtn, enabled);
        }

        if (autoDenyBtn != null) {
            boolean enabled = config.isBetterTPAutoDeny();

            updateButton(autoDenyBtn, enabled);
        }
    }

    private void updateButton(ButtonWidget btn, boolean enabled) {
        if (btn == null) {
            return;
        }

        int color = enabled ? ACCENT_COLOR : 0xFFFFFF;

        btn.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(style -> style.withColor(color))
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_tp_auto_accept")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Auto Accept",
                "Automatic acceptance of requests."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_tp_auto_deny")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Auto Deny",
                "Automatically reject requests."
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
        BetterTP.refreshBlockedStatus();

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