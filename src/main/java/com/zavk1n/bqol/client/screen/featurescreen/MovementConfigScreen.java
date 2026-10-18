package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterMovement;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;

public class MovementConfigScreen extends MainConfigScreen {
    private ButtonWidget defaultModeBtn, pvpModeBtn, stairupBtn, watersprintBtn, scaffoldBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    private enum SprintMode {
        DEFAULT,
        PVP,
    }

    public MovementConfigScreen(Screen parent) {
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

    private void rebuildUI() {
        clearChildren();

        defaultModeBtn = null;
        pvpModeBtn = null;
        stairupBtn = null;
        watersprintBtn = null;
        scaffoldBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_movement_default")) {
            defaultModeBtn = createExclusiveButton(rightX, y,
                "Default Mode",
                config::isBetterMovementDefaultMode,
                SprintMode.DEFAULT
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_movement_pvp")) {
            pvpModeBtn = createExclusiveButton(rightX, y,
                "PvP Mode",
                config::isBetterMovementPvPMode,
                SprintMode.PVP
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_movement_stair_up")) {
            stairupBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterMovementStairUp() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterMovementStairUp();
                        config.setBetterMovementStairUp(state);
                        changed = true;
                        updateButton(stairupBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(stairupBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_movement_water_sprint")) {
            watersprintBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterMovementWaterSprint() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterMovementWaterSprint();
                        config.setBetterMovementWaterSprint(state);
                        changed = true;
                        updateButton(watersprintBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(watersprintBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_movement_scaffold")) {
            scaffoldBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterMovementScaffold() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterMovementScaffold();
                        config.setBetterMovementScaffold(state);
                        changed = true;
                        updateButton(scaffoldBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(scaffoldBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
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
                        disableExclusiveMode();
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

    private void enableExclusiveMode(SprintMode mode) {
        disableExclusiveMode();

        switch (mode) {
            case DEFAULT -> config.setBetterMovementDefaultMode(true);
            case PVP -> config.setBetterMovementPvPMode(true);
        }
    }

    private void disableExclusiveMode() {
        config.setBetterMovementDefaultMode(false);
        config.setBetterMovementPvPMode(false);
    }

    private void updateAllButtons() {
        if (defaultModeBtn != null) {
            updateButton(defaultModeBtn, config.isBetterMovementDefaultMode());
        }

        if (pvpModeBtn != null) {
            updateButton(pvpModeBtn, config.isBetterMovementPvPMode());
        }

        if (stairupBtn != null) {
            updateButton(stairupBtn, config.isBetterMovementStairUp());
        }

        if (watersprintBtn != null) {
            updateButton(watersprintBtn, config.isBetterMovementWaterSprint());
        }

        if (scaffoldBtn != null) {
            updateButton(scaffoldBtn, config.isBetterMovementScaffold());
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

        if (!LiteApiManager.isFeatureBlocked("better_movement_default")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Default Mode",
                "Auto-sprint forward."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_movement_pvp")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "PvP Mode",
                "Auto-sprint in PvP conditions. (Timer 30s)."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_movement_stair_up")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Stair Up",
                "Improved stair climbing."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_movement_water_sprint")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Water Sprint",
                "Auto-sprint forward in water."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_movement_scaffold")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Scaffold",
                "Auto-walking almost on air!"
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
        BetterMovement.refreshBlockedStatus();
        BetterMovement.reloadFromConfig();

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