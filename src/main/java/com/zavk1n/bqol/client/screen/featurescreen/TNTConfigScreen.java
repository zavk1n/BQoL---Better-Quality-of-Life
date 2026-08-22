package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.features.BetterTnt;
import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TNTConfigScreen extends MainConfigScreen{

    /// Виджеты
    private ButtonWidget timerBtn, alertBtn, positionBtn, showXYZBtn;

    private static final String[] Position_NAMES = {"Center-Over", "Right-Over", "Right-Down", "Left-Over", "Left-Down"};
    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    /// Конструктор
    public TNTConfigScreen(Screen parent) {
        super(Text.literal("Better Tnt Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_tnt")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        timerBtn = null;
        alertBtn = null;
        positionBtn = null;
        showXYZBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_tnt_timer")) {
            timerBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterTntTimer() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterTntTimer();
                        config.setBetterTntTimer(state);
                        changed = true;
                        updateButton(timerBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(timerBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_tnt_alert")) {
            alertBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterTntAlert() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterTntAlert();
                        config.setBetterTntAlert(state);
                        changed = true;
                        updateButton(alertBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(alertBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_tnt_show_xyz")) {
            showXYZBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterTntAlertShowXYZ() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterTntAlertShowXYZ();
                        config.setBetterTntAlertShowXYZ(state);
                        changed = true;
                        updateButton(showXYZBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(showXYZBtn);
        }

        positionBtn = ButtonWidget.builder(
                Text.literal(Position_NAMES[config.getBetterTntAlertPosition()])
                    .styled(s -> s.withColor(ACCENT_COLOR)),
                button -> {
                    int location = (config.getBetterTntAlertPosition() + 1) % Position_NAMES.length;
                    config.setBetterTntAlertPosition(location);
                    changed = true;
                    positionBtn.setMessage(
                        Text.literal(Position_NAMES[location])
                            .styled(s -> s.withColor(ACCENT_COLOR))
                    );
                })
            .dimensions(rightX, y - 3 + SPACING, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
        addDrawableChild(positionBtn);

        addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Save & Back"),
                    button -> close()
                )
                .dimensions(width / 2 - 50, height - 40, 100, 25)
                .build()
        );

        updateAllButtons();
    }

    /// Обновление состояния кнопок
    private void updateAllButtons() {
        if (timerBtn != null) {
            updateButton(timerBtn, config.isBetterTntTimer());
        }

        if (alertBtn != null) {
            updateButton(alertBtn, config.isBetterTntAlert());
        }

        if (showXYZBtn != null) {
            updateButton(showXYZBtn, config.isBetterTntAlertShowXYZ());
        }
    }

    private void updateButton(ButtonWidget btn, boolean enabled) {
        btn.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_tnt_timer")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Tnt Timer",
                "Informative timer before the tnt explosion."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_tnt_alert")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Tnt Alert",
                "Warning window before TNT explosion."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_tnt_show_xyz")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Show XYZ",
                "Alert panel displays XYZ of TNT."
            );
            y += SPACING;
        }

        renderLabel(context, leftX, y, mouseX, mouseY,
            "Position", "Position of the alert panel on the screen."
        );
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

    /// Сохранение и закрытие
    private void save() {
        config.save();
        BetterTnt.refreshBlockedStatus();

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
// v1.0