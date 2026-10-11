package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterMine;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MineConfigScreen extends MainConfigScreen {
    private ButtonWidget holyWorldBtn;
    private ButtonWidget holyWorldSettingsBtn;

    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 25;

    private boolean changed = false;

    public MineConfigScreen(Screen parent) {
        super(Text.literal("Better Mine Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_mine")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        holyWorldBtn = null;
        holyWorldSettingsBtn = null;

        int rightX = width / 2 + 50;

        if (!LiteApiManager.isFeatureBlocked("better_mine_holyworld")) {
            holyWorldBtn = ButtonWidget.builder(
                    Text.literal(BetterMine.isEnabled() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean enabled = !BetterMine.isHolyWorldEnabled();
                        BetterMine.setHolyWorldEnabled(enabled);
                        changed = true;
                        updateEnabledButton(enabled);
                    }
                )
                .dimensions(rightX, 57, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(holyWorldBtn);

            holyWorldSettingsBtn = ButtonWidget.builder(Text.literal("Settings"), button -> client.setScreen(new HolyWorldMineConfigScreen(this)))
                .dimensions(rightX + 108, 57, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(holyWorldSettingsBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
                .dimensions(width / 2 - 50, height - 40, 100, 25)
                .build()
        );

        updateAllButtons();
    }

    private void updateAllButtons() {
        if (holyWorldBtn != null) {
            updateEnabledButton(BetterMine.isEnabled());
        }
    }

    private void updateEnabledButton(boolean enabled) {
        if (holyWorldBtn == null) {
            return;
        }

        holyWorldBtn.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
                .styled(style -> style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFFFF))
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;

        if (!LiteApiManager.isFeatureBlocked("better_mine_holyworld")) {
            renderLabel(context, leftX, 60, mouseX, mouseY, "HolyWorld", "Smart assistants for auto-mines on servers.");
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

        BetterMine.refreshBlockedStatus();

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