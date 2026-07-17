package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterSprint;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;

public class SprintConfigScreen extends MainConfigScreen {

    /// Виджеты
    private ButtonWidget defaultModeBtn, pvpModeBtn, treeModeBtn, stairupBtn, watersprintBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    private enum SprintMode {
        DEFAULT,
        PVP,
        TREE
    }

    /// Конструктор
    public SprintConfigScreen(Screen parent) {
        super(Text.literal("Better Sprint Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_sprint")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        defaultModeBtn = null;
        pvpModeBtn = null;
        treeModeBtn = null;
        stairupBtn = null;
        watersprintBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_sprint_default")) {
            defaultModeBtn = createExclusiveButton(
                rightX,
                y,
                "Default Mode",
                config::isBetterSprintDefaultModeEnabled,
                SprintMode.DEFAULT
            );

            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_sprint_pvp")) {
            pvpModeBtn = createExclusiveButton(
                rightX,
                y,
                "PvP Mode",
                config::isBetterSprintPvPModeEnabled,
                SprintMode.PVP
            );

            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_sprint_tree")) {
            treeModeBtn = createExclusiveButton(
                rightX,
                y,
                "Tree Mode",
                config::isBetterSprintTreeModeEnabled,
                SprintMode.TREE
            );

            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_sprint_stair_up")) {
            stairupBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterSprintStairUpEnabled() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterSprintStairUpEnabled();
                        config.setBetterSprintStairUpEnabled(state);
                        changed = true;
                        updateButton(stairupBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(stairupBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_sprint_water_sprint")) {
            watersprintBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterSprintWaterSprintEnabled() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterSprintWaterSprintEnabled();
                        config.setBetterSprintWaterSprintEnabled(state);
                        changed = true;
                        updateButton(watersprintBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(watersprintBtn);
        }

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

    private ButtonWidget createExclusiveButton(int x, int y, String label, BooleanSupplier isActive, SprintMode mode) {
        ButtonWidget btn = ButtonWidget.builder(
                Text.literal(isActive.getAsBoolean() ? "Enabled" : "Disabled"),
                button -> {
                    if (isActive.getAsBoolean()) {
                        disableAllModes();
                    } else {
                        enableExclusiveMode(mode);
                    }

                    changed = true;
                    updateAllButtons();
                    save();
                })
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        addDrawableChild(btn);

        return btn;
    }

    /// Работа с режимами
    private void enableExclusiveMode(SprintMode mode) {
        disableAllModes();

        switch (mode) {
            case DEFAULT -> config.setBetterSprintDefaultModeEnabled(true);
            case PVP -> config.setBetterSprintPvPModeEnabled(true);
            case TREE -> config.setBetterSprintTreeModeEnabled(true);
        }
    }

    private void disableAllModes() {
        config.setBetterSprintDefaultModeEnabled(false);
        config.setBetterSprintPvPModeEnabled(false);
        config.setBetterSprintTreeModeEnabled(false);
    }

    /// Обновление состояния кнопок
    private void updateAllButtons() {
        if (defaultModeBtn != null) {
            updateButton(defaultModeBtn, config.isBetterSprintDefaultModeEnabled());
        }

        if (pvpModeBtn != null) {
            updateButton(pvpModeBtn, config.isBetterSprintPvPModeEnabled());
        }

        if (treeModeBtn != null) {
            updateButton(treeModeBtn, config.isBetterSprintTreeModeEnabled());
        }

        if (stairupBtn != null) {
            updateButton(stairupBtn, config.isBetterSprintStairUpEnabled());
        }

        if (watersprintBtn != null) {
            updateButton(watersprintBtn, config.isBetterSprintWaterSprintEnabled());
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

        if (!LiteApiManager.isFeatureBlocked("better_sprint_default")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Default Mode",
                "Auto-sprint forward."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_sprint_pvp")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "PvP Mode",
                "Auto-sprint in PvP conditions. (Timer 30s)."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_sprint_tree")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Tree Mode",
                "Auto-sprint when there is foliage overhead."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_sprint_stair_up")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Stair Up",
                "Improved stair climbing."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_sprint_water_sprint")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Water Sprint",
                "Auto-sprint forward in water."
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

    /// Сохранение и закрытие
    private void save() {
        config.save();
        BetterSprint.refreshBlockedStatus();
        BetterSprint.reloadFromConfig();

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