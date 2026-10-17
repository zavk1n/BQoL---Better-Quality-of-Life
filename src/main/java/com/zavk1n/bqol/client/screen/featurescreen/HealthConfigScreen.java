package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorCheckbox;
import com.zavk1n.bqol.features.BetterHealth;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class HealthConfigScreen extends MainConfigScreen {

    private SliderWidget durationSlider;
    private ButtonWidget positionBtn, hoveringBtn, scalingBtn, pvpModeBtn, decimalBtn, goldenHeartsBtn;
    private ColorCheckbox goldenHeartsPlusCheck;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    private static final String[] Position_NAMES = {"Over", "Left", "Right", "Under"};

    public HealthConfigScreen(Screen parent) {
        super(Text.literal("Better Health Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_health")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        durationSlider = null;
        positionBtn = null;
        hoveringBtn = null;
        scalingBtn = null;
        pvpModeBtn = null;
        decimalBtn = null;
        goldenHeartsBtn = null;
        goldenHeartsPlusCheck = null;

        int centerX = width / 2;
        int startY = 60;
        int controlX = centerX + 50;

        int durationSec = config.betterHealthDuration / 1000;

        durationSlider = new SliderWidget(controlX, startY, 180, 20, Text.literal(durationSec + " s"), (durationSec - 3) / 27.0) {
            @Override
            protected void updateMessage() {
                int value = (int) Math.round(3 + this.value * 27);
                setMessage(Text.literal(value + " s"));
                config.betterHealthDuration = value * 1000;
            }

            @Override
            protected void applyValue() {
                int value = (int) Math.round(3 + this.value * 27);
                config.betterHealthDuration = value * 1000;
                changed = true;
            }
        };

        addDrawableChild(durationSlider);

        positionBtn = ButtonWidget.builder(
                Text.literal(Position_NAMES[config.getBetterHealthPosition()])
                    .styled(s -> s.withColor(ACCENT_COLOR)),
                button -> {
                    int location = (config.getBetterHealthPosition() + 1) % Position_NAMES.length;
                    config.setBetterHealthPosition(location);
                    changed = true;
                    positionBtn.setMessage(
                        Text.literal(Position_NAMES[location])
                            .styled(s -> s.withColor(ACCENT_COLOR))
                    );
                })
            .dimensions(controlX, startY + SPACING, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        addDrawableChild(positionBtn);

        if (!LiteApiManager.isFeatureBlocked("better_health_hovering")) {
            hoveringBtn = createButton(
                controlX,
                startY + SPACING * 2,
                config::isBetterHealthHovering,
                config::setBetterHealthHovering
            );
        }

        if (!LiteApiManager.isFeatureBlocked("better_health_scaling")) {
            scalingBtn = createButton(
                controlX,
                startY + SPACING * 3,
                config::isBetterHealthScaling,
                config::setBetterHealthScaling
            );
        }

        if (!LiteApiManager.isFeatureBlocked("better_health_pvp")) {
            pvpModeBtn = createButton(
                controlX,
                startY + SPACING * 4,
                config::isBetterHealthPvPMode,
                config::setBetterHealthPvPMode
            );
        }

        if (!LiteApiManager.isFeatureBlocked("better_health_decimal")) {
            decimalBtn = createButton(
                controlX,
                startY + SPACING * 5,
                config::isBetterHealthDecimal,
                config::setBetterHealthDecimal
            );
        }

        if (!LiteApiManager.isFeatureBlocked("better_health_golden_hearts")) {
            goldenHeartsBtn = createButton(
                controlX,
                startY + SPACING * 6,
                config::isBetterHealthGoldenHearts,
                value -> {
                    config.setBetterHealthGoldenHearts(value);
                    changed = true;

                    updateGoldenHeartsPlusState();

                    if (goldenHeartsPlusCheck != null) {
                        goldenHeartsPlusCheck.setChecked(config.isBetterHealthGoldenHeartsPlus());
                    }
                }
            );
        }

        if (!LiteApiManager.isFeatureBlocked("better_health_golden_plus") && goldenHeartsBtn != null) {
            goldenHeartsPlusCheck = new ColorCheckbox(
                controlX,
                startY + SPACING * 7 + 6,
                Text.empty(),
                config.isBetterHealthGoldenHeartsPlus(),
                button -> {
                    boolean enabled = !config.isBetterHealthGoldenHeartsPlus();
                    config.setBetterHealthGoldenHeartsPlus(enabled);
                    ((ColorCheckbox) button).setChecked(enabled);
                    changed = true;
                }
            );

            addDrawableChild(goldenHeartsPlusCheck);
            updateGoldenHeartsPlusState();
        }

        addDrawableChild(
            ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
                .dimensions(width / 2 - 50, height - 40, 100, BUTTON_HEIGHT)
                .build()
        );

        updateAllButtons();
    }

    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget button = ButtonWidget.builder(
                Text.empty(),
                widget -> {
                    boolean enabled = !getter.getAsBoolean();
                    setter.accept(enabled);
                    changed = true;
                    updateButton(widget, enabled);
                })
            .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        updateButton(button, getter.getAsBoolean());
        addDrawableChild(button);

        return button;
    }

    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    private void updateAllButtons() {
        if (hoveringBtn != null) updateButton(hoveringBtn, config.isBetterHealthHovering());
        if (scalingBtn != null) updateButton(scalingBtn, config.isBetterHealthScaling());
        if (pvpModeBtn != null) updateButton(pvpModeBtn, config.isBetterHealthPvPMode());
        if (decimalBtn != null) updateButton(decimalBtn, config.isBetterHealthDecimal());
        if (goldenHeartsBtn != null) updateButton(goldenHeartsBtn, config.isBetterHealthGoldenHearts());
    }

    private void updateGoldenHeartsPlusState() {
        if (goldenHeartsPlusCheck != null) {
            goldenHeartsPlusCheck.active = config.isBetterHealthGoldenHearts();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int labelX = width / 4;
        int startY = 60;

        renderLabel(context, labelX, startY, mouseX, mouseY,
            "Duration", "Indicator display duration."
        );

        renderLabel(context, labelX, startY + SPACING, mouseX, mouseY,
            "Position", "Position of the indicator on the screen."
        );

        if (!LiteApiManager.isFeatureBlocked("better_health_hovering"))
            renderLabel(context, labelX, startY + SPACING * 2, mouseX, mouseY,
                "Hovering", "Displaying indicator when aiming at player."
            );

        if (!LiteApiManager.isFeatureBlocked("better_health_scaling"))
            renderLabel(context, labelX, startY + SPACING * 3, mouseX, mouseY,
                "Scaling", "Displaying indicator with scaling effect."
            );

        if (!LiteApiManager.isFeatureBlocked("better_health_pvp"))
            renderLabel(context, labelX, startY + SPACING * 4, mouseX, mouseY,
                "PvP Mode", "Displaying indicator only in PvP."
            );

        if (!LiteApiManager.isFeatureBlocked("better_health_decimal"))
            renderLabel(context, labelX, startY + SPACING * 5, mouseX, mouseY,
                "Decimal Format", "Displaying indicator with float part."
            );

        if (!LiteApiManager.isFeatureBlocked("better_health_golden_hearts"))
            renderLabel(context, labelX, startY + SPACING * 6, mouseX, mouseY,
                "Golden Hearts", "Displaying indicator shows additional hearts."
            );

        if (!LiteApiManager.isFeatureBlocked("better_health_golden_plus"))
            renderLabel(context, labelX, startY + SPACING * 7, mouseX, mouseY,
                "Golden Hearts Plus", "Show golden hearts with a '+' sign."
            );
    }

    private void renderLabel(DrawContext ctx, int x, int y, int mousex, int mousey, String title, String desc) {
        int titleWidth = textRenderer.getWidth(title);

        boolean hovered = mousex >= x &&
                mousex <= x + titleWidth &&
                mousey >= y &&
                mousey <= y + textRenderer.fontHeight;

        int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

        ctx.drawTextWithShadow(textRenderer, Text.literal(title), x, y, color);
        ctx.drawTextWithShadow(textRenderer, Text.literal(desc), x, y + 12, 0xFF888888);
    }

    private void save() {
        config.save();
        BetterHealth.refreshBlockedStatus();
        BetterHealth.reloadFromConfig();
        BetterHealth.resetDisplay();

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