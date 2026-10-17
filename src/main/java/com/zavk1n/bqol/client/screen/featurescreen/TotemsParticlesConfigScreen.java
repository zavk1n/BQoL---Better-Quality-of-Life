package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorSlider;
import com.zavk1n.bqol.features.BetterParticles;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class TotemsParticlesConfigScreen extends MainConfigScreen {

    private ButtonWidget colorBtn, countBtn, countModeBtn;
    private ColorSlider colorSlider;
    private TextFieldWidget colorHexField;

    private int currentColor,currentCountMode;

    private boolean updatingColor;
    private boolean changed = false;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 55, LABEL_WIDTH = 220, SLIDER_WIDTH = 150, HEX_WIDTH = 70, MODE_WIDTH = 100;

    private static final String[] COUNT_MODES = {
        "Little",
        "Medium",
        "Many"
    };

    public TotemsParticlesConfigScreen(Screen parent) {
        super(Text.literal("Better Particles Totems Settings"), parent);

        currentColor = config.getBetterParticlesTotemsColor();
        currentCountMode = Math.floorMod(config.getBetterParticlesTotemsCountMode(), 3);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_particles_totems")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        colorBtn = null;
        countBtn = null;
        countModeBtn = null;

        colorSlider = null;
        colorHexField = null;

        int centerX = width / 2;
        int leftX = width / 6;

        int controlX = leftX + LABEL_WIDTH + 30;
        int sliderX = controlX + BUTTON_WIDTH + 15;

        int row = 0;

        {
            int y = 60 + row * SPACING;

            colorBtn = createButton(controlX, y, config::isBetterParticlesTotemsColorEnabled,
                value -> {
                    config.setBetterParticlesTotemsColorEnabled(value);
                    changed = true;
                    updateColorControls();
                }
            );

            addDrawableChild(colorBtn);

            colorSlider = createColorSlider(
                sliderX,
                y,
                currentColor,
                newColor -> {
                    if (updatingColor) return;

                    currentColor = newColor;
                    config.setBetterParticlesTotemsColor(newColor);

                    if (colorHexField != null) {
                        updatingColor = true;
                        updateHexField(colorHexField, newColor);
                        updatingColor = false;
                    }

                    changed = true;
                }
            );

            colorHexField = createHexField(sliderX + SLIDER_WIDTH + 5, y, currentColor,
                hex -> {
                    if (updatingColor) return;

                    int rgb = parseHex(hex);

                    if (rgb != -1) {
                        currentColor = rgb;
                        config.setBetterParticlesTotemsColor(rgb);

                        updatingColor = true;
                        updateColorSlider(colorSlider, rgb);
                        updatingColor = false;

                        changed = true;
                    }
                }
            );

            addDrawableChild(colorSlider);
            addDrawableChild(colorHexField);

            row++;
        }

        {
            int y = 60 + row * SPACING;

            countBtn = createButton(controlX, y, config::isBetterParticlesTotemsCountEnabled,
                value -> {
                    config.setBetterParticlesTotemsCountEnabled(value);
                    changed = true;
                    updateCountControls();
                }
            );

            addDrawableChild(countBtn);

            countModeBtn = ButtonWidget.builder(Text.literal(COUNT_MODES[currentCountMode]),
                    button -> {
                        currentCountMode = (currentCountMode + 1) % COUNT_MODES.length;

                        config.setBetterParticlesTotemsCountMode(currentCountMode);

                        updateCountModeButton();
                        changed = true;
                    }
                )
                .dimensions(sliderX, y - 3, MODE_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(countModeBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(centerX - 50, height - 40, 100, 25)
            .build()
        );

        updateColorControls();
        updateCountControls();
    }

    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget btn = ButtonWidget.builder(
                Text.literal(getter.getAsBoolean() ? "Enabled" : "Disabled"),
                button -> {
                    setter.accept(!getter.getAsBoolean());
                    updateButton(button, getter.getAsBoolean());
                }
            )
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        updateButton(btn, getter.getAsBoolean());

        return btn;
    }

    private void updateButton(ButtonWidget button, boolean enabled) {
        if (button == null) return;

        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(style -> style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF))
        );
    }

    private void updateColorControls() {
        if (colorSlider != null) {
            colorSlider.active = config.isBetterParticlesTotemsColorEnabled();
        }

        if (colorHexField != null) {
            colorHexField.setEditable(config.isBetterParticlesTotemsColorEnabled());
        }
    }

    private void updateCountControls() {
        if (countModeBtn != null) {
            countModeBtn.active = config.isBetterParticlesTotemsCountEnabled();
        }

        updateCountModeButton();
    }

    private void updateCountModeButton() {
        if (countModeBtn == null) return;

        countModeBtn.setMessage(Text.literal(COUNT_MODES[currentCountMode])
            .styled(style -> style.withColor(config.isBetterParticlesTotemsCountEnabled() ? ACCENT_COLOR : 0xFFFFFF))
        );
    }

    private ColorSlider createColorSlider(int x, int y, int rgb, Consumer<Integer> onColorChange) {
        int hue = rgbToHue(rgb);

        ColorSlider slider = new ColorSlider(x, y, SLIDER_WIDTH, 20, Text.literal(""), hue / 360.0,
            value -> {
                int newHue = (int) Math.round(value * 360);
                int newRgb = hslToRgb(newHue, 100, 50);

                onColorChange.accept(newRgb);
            }
        );

        slider.setY(y);

        return slider;
    }

    private void updateColorSlider(ColorSlider slider, int rgb) {
        if (slider != null) {
            slider.setValue(rgbToHue(rgb) / 360.0);
        }
    }

    private TextFieldWidget createHexField(int x, int y, int initialRgb, Consumer<String> onHexChange) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, HEX_WIDTH, 20, Text.literal("#RRGGBB"));

        field.setMaxLength(7);
        field.setText(String.format("#%06X", initialRgb & 0xFFFFFF));
        field.setChangedListener(text -> {
            if (text.startsWith("#") && text.length() == 7) {
                onHexChange.accept(text);
            }
        });

        field.setEditable(true);
        field.setFocusUnlocked(true);

        return field;
    }

    private void updateHexField(TextFieldWidget field, int rgb) {
        if (field == null) return;

        String formatted = String.format("#%06X", rgb & 0xFFFFFF);

        if (!formatted.equals(field.getText())) {
            field.setText(formatted);
        }
    }

    private int parseHex(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }

            if (hex.length() == 6) {
                return Integer.parseInt(hex, 16);
            }
        } catch (NumberFormatException ignored) {
        }

        return -1;
    }

    private int rgbToHue(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        int delta = max - min;

        if (delta == 0) return 0;

        float hue;

        if (max == r) {
            hue = (g - b) / (float) delta;
        } else if (max == g) {
            hue = (b - r) / (float) delta + 2;
        } else {
            hue = (r - g) / (float) delta + 4;
        }

        hue *= 60;

        if (hue < 0) {
            hue += 360;
        }

        return Math.round(hue);
    }

    private int hslToRgb(int h, int s, int l) {
        float hue = (h % 360) / 360f;
        float sat = s / 100f;
        float light = l / 100f;

        float c = (1 - Math.abs(2 * light - 1)) * sat;
        float x = c * (1 - Math.abs((hue * 6) % 2 - 1));
        float m = light - c / 2;

        float r = 0;
        float g = 0;
        float b = 0;

        if (hue < 1 / 6f) {
            r = c;
            g = x;
        } else if (hue < 2 / 6f) {
            r = x;
            g = c;
        } else if (hue < 3 / 6f) {
            g = c;
            b = x;
        } else if (hue < 4 / 6f) {
            g = x;
            b = c;
        } else if (hue < 5 / 6f) {
            r = x;
            b = c;
        } else {
            r = c;
            b = x;
        }

        return (Math.round((r + m) * 255) << 16) | (Math.round((g + m) * 255) << 8) | Math.round((b + m) * 255);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 6;

        renderLabel(context, leftX, 60,
            "Color",
            "Changes the color of totem particles.", mouseX, mouseY
        );

        renderLabel(context, leftX, 60 + SPACING,
            "Count",
            "Changes the amount of totem particles.", mouseX, mouseY
        );

        if (colorHexField != null
            && colorSlider != null
            && colorHexField.visible) {

            renderColorPreview(context, colorHexField.getX() + HEX_WIDTH + 5, colorSlider.getY(), currentColor);
        }
    }

    private void renderColorPreview(DrawContext context, int x, int y, int rgb) {
        int previewSize = 18;

        context.fill(x - 1, y - 1, x + previewSize + 1, y + previewSize + 1, 0xFF000000);
        context.fill(x, y, x + previewSize, y + previewSize, 0xFF000000 | (rgb & 0xFFFFFF));
    }

    private void renderLabel(DrawContext context, int x, int y, String title, String desc, int mouseX, int mouseY) {
        int titleWidth = textRenderer.getWidth(title);

        boolean hovered = mouseX >= x
            && mouseX <= x + titleWidth
            && mouseY >= y
            && mouseY <= y + textRenderer.fontHeight;

        int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

        context.drawText(textRenderer, Text.literal(title), x, y, color, false);
        context.drawText(textRenderer, Text.literal(desc), x, y + 12, 0xFF888888, false);
    }

    private void save() {
        config.save();
        BetterParticles.refreshBlockedStatus();

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