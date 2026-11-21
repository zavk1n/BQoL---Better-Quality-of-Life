package com.zavk1n.bqol.client.screen.featurescreen.mine;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.mine.BetterMine;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MineConfigScreen extends MainConfigScreen {
    private ButtonWidget holyWorldBtn, funTimeBtn;
    private ButtonWidget holyWorldSettingsBtn, funTimeSettingsBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;

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
        funTimeBtn = null;
        funTimeSettingsBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_mine_holyworld")) {
            holyWorldBtn = ButtonWidget.builder(
                    Text.literal(BetterMine.isHolyWorldEnabled() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean enabled = !BetterMine.isHolyWorldEnabled();
                        BetterMine.setHolyWorldEnabled(enabled);
                        changed = true;
                        updateHolyWorldButton(enabled);
                    }
                )
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(holyWorldBtn);

            holyWorldSettingsBtn = ButtonWidget.builder(Text.literal("Settings"), button -> client.setScreen(new HolyWorldMineConfigScreen(this)))
                .dimensions(rightX + 108, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(holyWorldSettingsBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_mine_funtime")) {
            funTimeBtn = ButtonWidget.builder(
                    Text.literal(BetterMine.isFunTimeEnabled() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean enabled = !BetterMine.isFunTimeEnabled();
                        BetterMine.setFunTimeEnabled(enabled);
                        changed = true;
                        updateFunTimeButton(enabled);
                    }
                )
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(funTimeBtn);

            addDrawableChild(funTimeBtn);

            funTimeSettingsBtn = ButtonWidget.builder(Text.literal("Settings"), button -> client.setScreen(new FunTimeMineConfigScreen(this)))
                .dimensions(rightX + 108, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(funTimeSettingsBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(width / 2 - 50, height - 40, 100, 25)
            .build()
        );

        updateAllButtons();
    }

    private void updateAllButtons() {
        if (holyWorldBtn != null) {
            updateHolyWorldButton(BetterMine.isHolyWorldEnabled());
        }

        if (funTimeBtn != null) {
            updateFunTimeButton(BetterMine.isFunTimeEnabled());
        }
    }

    private void updateHolyWorldButton(boolean enabled) {
        if (holyWorldBtn != null) {
            holyWorldBtn.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
                .styled(style -> style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFFFF))
            );
        }
    }

    private void updateFunTimeButton(boolean enabled) {
        if (funTimeBtn != null) {
            funTimeBtn.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
                .styled(style -> style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFFFF))
            );
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_mine_holyworld")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "HolyWorld",
                "Smart assistant for auto-mines on HolyWorld."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_mine_funtime")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "FunTime",
                "Smart assistant for auto-mines on FunTime."
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