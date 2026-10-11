package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorSlider;
import com.zavk1n.bqol.features.BetterFog;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class FogConfigScreen extends MainConfigScreen {
    private ColorSlider colorSlider;
    private TextFieldWidget hexField;

    private SliderWidget distanceSlider;

    private ButtonWidget colorBtn;
    private ButtonWidget nightVisionBtn;
    private ButtonWidget noFogBtn;
    private ButtonWidget noBlindnessBtn;

    private boolean updatingHexField;
    private boolean updatingColorControls;
    private boolean changed;

    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 25;

    private static final int SPACING = 45;
    private static final int CONTROL_WIDTH = 180;
    private static final int HEX_WIDTH = 75;

    public FogConfigScreen(Screen parent) {
        super(Text.literal("Better Fog Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_fog")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        colorSlider = null;
        hexField = null;
        distanceSlider = null;

        colorBtn = null;
        nightVisionBtn = null;
        noFogBtn = null;
        noBlindnessBtn = null;

        int centerX = width / 2;

        int currentY = 45;

        if (!LiteApiManager.isFeatureBlocked("better_fog_color")) {
            colorSlider = createColorSlider(
                centerX - 40,
                currentY,
                BetterFog.getFogColor(),
                value -> {
                    if (updatingColorControls) {
                        return;
                    }

                    int hue = (int) Math.round(value * 360.0);
                    int rgb = hslToRgb(hue, 100, 50);

                    BetterFog.setFogColor(rgb);
                    changed = true;

                    updateHexField(hexField, rgb);
                }
            );

            addDrawableChild(colorSlider);

            hexField = createHexField(centerX + 115, currentY, BetterFog.getFogColor(), hex -> applyHexColor(hex, colorSlider, BetterFog::setFogColor));

            addDrawableChild(hexField);

            colorBtn = ButtonWidget.builder(
                Text.literal(""),
                button -> {
                    boolean enabled = !BetterFog.isFogColorEnabled();

                    BetterFog.setFogColorEnabled(enabled);
                    changed = true;

                    updateButton(colorBtn, enabled);
                }).dimensions(centerX + 230, currentY - 3, BUTTON_WIDTH, BUTTON_HEIGHT).build();

            addDrawableChild(colorBtn);

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_fog_distance")) {
            distanceSlider = createDistanceSlider(centerX - 40, currentY, BetterFog.getFogDistance());

            addDrawableChild(distanceSlider);

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_fog_night_vision")) {
            nightVisionBtn = createToggleButton(centerX - 20, currentY - 3, BetterFog.isNightVisionEnabled(),
                enabled -> {
                    BetterFog.setNightVisionEnabled(enabled);
                    changed = true;
                }
            );

            addDrawableChild(nightVisionBtn);

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_fog_no_fog")) {
            noFogBtn = createToggleButton(centerX - 20, currentY - 3, BetterFog.isNoFogEnabled(),
                enabled -> {
                    BetterFog.setNoFogEnabled(enabled);
                    changed = true;
                }
            );

            addDrawableChild(noFogBtn);

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_fog_no_blindness")) {
            noBlindnessBtn = createToggleButton(centerX - 20, currentY - 3, BetterFog.isNoBlindnessEnabled(),
                enabled -> {
                    BetterFog.setNoBlindnessEnabled(enabled);
                    changed = true;
                }
            );

            addDrawableChild(noBlindnessBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(centerX - 50, height - 40, 100, 25
            ).build()
        );

        updateAllButtons();
    }

    private ButtonWidget createToggleButton(int x, int y, boolean initialState, Consumer<Boolean> onChange) {
        return ButtonWidget.builder(
            Text.literal(initialState ? "Enabled" : "Disabled"),
            button -> {
                boolean enabled = !button.getMessage()
                    .getString()
                    .equals("Enabled");

                onChange.accept(enabled);

                updateButton(button, enabled);
            }
        ).dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
    }


    private ColorSlider createColorSlider(int x, int y, int rgb, Consumer<Double> onValueChange) {
        return new ColorSlider(x, y, 150, 20, Text.literal(""), rgbToHue(rgb & 0xFFFFFF) / 360.0, onValueChange);
    }

    private TextFieldWidget createHexField(int x, int y, int initialRgb, Consumer<String> onHexChange) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, HEX_WIDTH, 20, Text.literal("#RRGGBB"));

        field.setMaxLength(7);
        field.setText(String.format("#%06X", initialRgb & 0xFFFFFF));
        field.setChangedListener(text -> {
            if (updatingHexField) {
                return;
            }

            String value = text.trim();

            if (value.startsWith("#")) {
                value = value.substring(1);
            }

            if (value.length() != 6 || parseHex(value) == -1) {
                return;
            }

            onHexChange.accept(value);
        });

        field.setEditable(true);
        field.setFocusUnlocked(true);

        return field;
    }

    private SliderWidget createDistanceSlider(int x, int y, int initialChunks) {
        int chunks = Math.max(1, Math.min(32, initialChunks));

        return new SliderWidget(x, y, CONTROL_WIDTH, 20, Text.empty(), (chunks - 1) / 31.0) {
            @Override
            protected void updateMessage() {
                int value = 1 + (int) Math.round(this.value * 31.0);

                value = Math.max(1, Math.min(32, value));

                setMessage(Text.literal(value + " chunks"));
            }

            @Override
            protected void applyValue() {
                int value = 1 + (int) Math.round(this.value * 31.0);

                value = Math.max(1, Math.min(32, value));

                BetterFog.setFogDistance(value);

                changed = true;
            }
        };
    }

    private void updateColorSlider(ColorSlider slider, int rgb) {
        if (slider == null) {
            return;
        }

        updatingColorControls = true;

        try {
            slider.setValue(
                rgbToHue(rgb & 0xFFFFFF) / 360.0
            );
        } finally {
            updatingColorControls = false;
        }
    }

    private void applyHexColor(String hex, ColorSlider slider, Consumer<Integer> onColorChange) {
        int rgb = parseHex(hex);

        if (rgb == -1) {
            return;
        }

        rgb &= 0xFFFFFF;

        onColorChange.accept(rgb);
        changed = true;

        updateColorSlider(slider, rgb);
    }

    private void updateHexField(TextFieldWidget field, int rgb) {
        if (field == null) {
            return;
        }

        String formatted = String.format("#%06X", rgb & 0xFFFFFF);

        if (formatted.equalsIgnoreCase(field.getText())) {
            return;
        }

        updatingHexField = true;

        try {
            field.setText(formatted);
        } finally {
            updatingHexField = false;
        }
    }

    private void updateAllButtons() {
        if (colorBtn != null) {
            updateButton(colorBtn, BetterFog.isFogColorEnabled());
        }

        if (nightVisionBtn != null) {
            updateButton(nightVisionBtn, BetterFog.isNightVisionEnabled());
        }

        if (noFogBtn != null) {
            updateButton(noFogBtn, BetterFog.isNoFogEnabled());
        }

        if (noBlindnessBtn != null) {
            updateButton(noBlindnessBtn, BetterFog.isNoBlindnessEnabled());
        }
    }

    private void updateButton(ButtonWidget button, boolean enabled) {
        if (button == null) {
            return;
        }

        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(style -> style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF))
        );
    }

    private int parseHex(String hex) {
        if (hex == null) {
            return -1;
        }

        hex = hex.trim();

        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        if (!hex.matches("[0-9a-fA-F]{6}")) {
            return -1;
        }

        try {
            return Integer.parseInt(hex, 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private int rgbToHue(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));

        int delta = max - min;

        if (delta == 0) {
            return 0;
        }

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

        return Math.round(hue) % 360;
    }

    private int hslToRgb(int h, int s, int l) {
        float hue = (h % 360) / 360.0f;
        float sat = s / 100.0f;
        float light = l / 100.0f;

        float c = (1 - Math.abs(2 * light - 1)) * sat;
        float x = c * (1 - Math.abs((hue * 6) % 2 - 1));
        float m = light - c / 2;

        float r = 0;
        float g = 0;
        float b = 0;

        if (hue < 1.0f / 6.0f) {
            r = c;
            g = x;
        } else if (hue < 2.0f / 6.0f) {
            r = x;
            g = c;
        } else if (hue < 3.0f / 6.0f) {
            g = c;
            b = x;
        } else if (hue < 4.0f / 6.0f) {
            g = x;
            b = c;
        } else if (hue < 5.0f / 6.0f) {
            r = x;
            b = c;
        } else {
            r = c;
            b = x;
        }

        int red = Math.round((r + m) * 255);
        int green = Math.round((g + m) * 255);
        int blue = Math.round((b + m) * 255);

        return (red << 16) | (green << 8) | blue;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int currentY = 45;

        if (!LiteApiManager.isFeatureBlocked("better_fog_color")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "Color",
                "Customize the fog color."
            );

            if (hexField != null && colorSlider != null) {
                int previewSize = 18;

                int previewX = hexField.getX() + HEX_WIDTH + 5;
                int previewY = colorSlider.getY();

                context.fill(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, 0xFF000000);
                context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000 | BetterFog.getFogColor());
            }

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_fog_distance")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "Fog Distance",
                "Set fog distance from 1 to 32 chunks."
            );

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_fog_night_vision")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "Night Vision",
                "Adds client-side night vision."
            );

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_fog_no_fog")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "No Fog",
                "Disables fog when allowed by effects."
            );

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_fog_no_blindness")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "No Blindness",
                "Disables the visual blindness effect."
            );
        }
    }

    private void renderLabel(DrawContext context, int x, int y, int mouseX, int mouseY, String title, String desc) {
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
        BetterFog.refreshBlockedStatus();

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