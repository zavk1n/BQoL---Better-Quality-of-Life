package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorSlider;
import com.zavk1n.bqol.features.BetterLiquid;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class LiquidConfigScreen extends MainConfigScreen {

    private ColorSlider waterColorSlider;
    private ColorSlider lavaColorSlider;

    private TextFieldWidget waterHexField;
    private TextFieldWidget lavaHexField;

    private SliderWidget waterIntensitySlider;
    private SliderWidget lavaIntensitySlider;
    private SliderWidget waterTransparencySlider;
    private SliderWidget lavaTransparencySlider;

    private ButtonWidget waterColorBtn;
    private ButtonWidget lavaColorBtn;
    private ButtonWidget waterIntensityBtn;
    private ButtonWidget lavaIntensityBtn;
    private ButtonWidget waterTransparencyBtn;
    private ButtonWidget lavaTransparencyBtn;

    private boolean updatingHexField;
    private boolean updatingColorControls;
    private boolean changed;

    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 25;

    private static final int SPACING = 45;
    private static final int CONTROL_WIDTH = 180;
    private static final int HEX_WIDTH = 75;

    public LiquidConfigScreen(Screen parent) {
        super(Text.literal("Better Liquid Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_liquid")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        waterColorSlider = null;
        lavaColorSlider = null;

        waterHexField = null;
        lavaHexField = null;

        waterIntensitySlider = null;
        lavaIntensitySlider = null;
        waterTransparencySlider = null;
        lavaTransparencySlider = null;

        waterColorBtn = null;
        lavaColorBtn = null;

        waterIntensityBtn = null;
        lavaIntensityBtn = null;

        waterTransparencyBtn = null;
        lavaTransparencyBtn = null;

        int centerX = width / 2;
        int currentY = 45;

        final int enabledX = centerX + 210;

        if (!LiteApiManager.isFeatureBlocked("better_liquid_water_color")) {
            waterColorSlider = createColorSlider(centerX - 60, currentY, BetterLiquid.getWaterColor(),
                value -> {
                    if (updatingColorControls) {
                        return;
                    }

                    int hue = (int) Math.round(value * 360.0);
                    int rgb = hslToRgb(hue, 100, 50);

                    BetterLiquid.setWaterColor(rgb);
                    changed = true;

                    updateHexField(waterHexField, rgb);
                }
            );

            addDrawableChild(waterColorSlider);

            waterHexField = createHexField(centerX + 95, currentY, BetterLiquid.getWaterColor(),
                hex -> applyHexColor(hex, waterColorSlider, BetterLiquid::setWaterColor)
            );

            addDrawableChild(waterHexField);

            waterColorBtn = createToggleButton(enabledX, currentY - 3, BetterLiquid.isWaterColorEnabled(), BetterLiquid::setWaterColorEnabled);
            addDrawableChild(waterColorBtn);

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_liquid_water_transparency")) {
            waterTransparencySlider = createPercentageSlider(centerX + 20, currentY, BetterLiquid.getWaterTransparency(), BetterLiquid::setWaterTransparency);
            addDrawableChild(waterTransparencySlider);

            waterTransparencyBtn = createToggleButton(enabledX, currentY - 3, BetterLiquid.isWaterTransparencyEnabled(), BetterLiquid::setWaterTransparencyEnabled);
            addDrawableChild(waterTransparencyBtn);

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_liquid_water_color_intensity")) {
            waterIntensitySlider = createPercentageSlider(centerX + 20, currentY, BetterLiquid.getWaterColorIntensity(), BetterLiquid::setWaterColorIntensity);
            addDrawableChild(waterIntensitySlider);

            waterIntensityBtn = createToggleButton(enabledX, currentY - 3, BetterLiquid.isWaterColorIntensityEnabled(), BetterLiquid::setWaterColorIntensityEnabled);
            addDrawableChild(waterIntensityBtn);

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_liquid_lava_color")) {
            lavaColorSlider = createColorSlider(centerX - 60, currentY, BetterLiquid.getLavaColor(),
                value -> {
                    if (updatingColorControls) {
                        return;
                    }

                    int hue = (int) Math.round(value * 360.0);
                    int rgb = hslToRgb(hue, 100, 50);

                    BetterLiquid.setLavaColor(rgb);
                    changed = true;

                    updateHexField(lavaHexField, rgb);
                }
            );

            addDrawableChild(lavaColorSlider);

            lavaHexField = createHexField(centerX + 95, currentY, BetterLiquid.getLavaColor(),
                hex -> applyHexColor(hex, lavaColorSlider, BetterLiquid::setLavaColor)
            );

            addDrawableChild(lavaHexField);

            lavaColorBtn = createToggleButton(enabledX, currentY - 3, BetterLiquid.isLavaColorEnabled(), BetterLiquid::setLavaColorEnabled);
            addDrawableChild(lavaColorBtn);

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_liquid_lava_transparency")) {
            lavaTransparencySlider = createPercentageSlider(centerX + 20, currentY, BetterLiquid.getLavaTransparency(), BetterLiquid::setLavaTransparency);
            addDrawableChild(lavaTransparencySlider);

            lavaTransparencyBtn = createToggleButton(enabledX, currentY - 3, BetterLiquid.isLavaTransparencyEnabled(), BetterLiquid::setLavaTransparencyEnabled);
            addDrawableChild(lavaTransparencyBtn);

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_liquid_lava_color_intensity")) {
            lavaIntensitySlider = createPercentageSlider(centerX + 20, currentY, BetterLiquid.getLavaColorIntensity(), BetterLiquid::setLavaColorIntensity);
            addDrawableChild(lavaIntensitySlider);

            lavaIntensityBtn = createToggleButton(enabledX, currentY - 3, BetterLiquid.isLavaColorIntensityEnabled(), BetterLiquid::setLavaColorIntensityEnabled);
            addDrawableChild(lavaIntensityBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(centerX - 50, height - 40, 100, 25)
            .build()
        );

        updateAllButtons();
    }

    private ButtonWidget createToggleButton(int x, int y, boolean initialState, Consumer<Boolean> onChange) {
        return ButtonWidget.builder(Text.literal(initialState ? "Enabled" : "Disabled"),
                button -> {
                    boolean enabled = !button.getMessage()
                        .getString()
                        .equals("Enabled");

                    onChange.accept(enabled);
                    changed = true;

                    updateButton(button, enabled);
                }
            )
            .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
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

    private SliderWidget createPercentageSlider(int x, int y, int initialValue, Consumer<Integer> onValueChange) {
        int percentage = Math.max(0, Math.min(100, initialValue));

        return new SliderWidget(x, y, CONTROL_WIDTH, 20, Text.empty(), percentage / 100.0) {
            @Override
            protected void updateMessage() {
                int value = (int) Math.round(this.value * 100.0);
                value = Math.max(0, Math.min(100, value));

                setMessage(Text.literal(value + "%"));
            }

            @Override
            protected void applyValue() {
                int value = (int) Math.round(this.value * 100.0);
                value = Math.max(0, Math.min(100, value));

                onValueChange.accept(value);
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
            slider.setValue(rgbToHue(rgb & 0xFFFFFF) / 360.0);
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
        if (waterColorBtn != null) {
            updateButton(waterColorBtn, BetterLiquid.isWaterColorEnabled());
        }

        if (lavaColorBtn != null) {
            updateButton(lavaColorBtn, BetterLiquid.isLavaColorEnabled());
        }

        if (waterIntensityBtn != null) {
            updateButton(waterIntensityBtn, BetterLiquid.isWaterColorIntensityEnabled());
        }

        if (lavaIntensityBtn != null) {
            updateButton(lavaIntensityBtn, BetterLiquid.isLavaColorIntensityEnabled());
        }

        if (waterTransparencyBtn != null) {
            updateButton(waterTransparencyBtn, BetterLiquid.isWaterTransparencyEnabled());
        }

        if (lavaTransparencyBtn != null) {
            updateButton(lavaTransparencyBtn, BetterLiquid.isLavaTransparencyEnabled());
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

        if (!LiteApiManager.isFeatureBlocked("better_liquid_water_color")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "Water Color",
                "Customize the water color."
            );

            if (waterHexField != null && waterColorSlider != null) {
                int previewSize = 18;
                int previewX = waterHexField.getX() + HEX_WIDTH + 5;
                int previewY = waterColorSlider.getY();

                context.fill(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, 0xFF000000);
                context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000 | BetterLiquid.getWaterColor());
            }

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_liquid_water_transparency")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "Water Transparency",
                "Set water transparency from 0% to 100%."
            );

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_liquid_water_color_intensity")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "Water Color Intensity",
                "Set custom water color intensity from 0% to 100%."
            );

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_liquid_lava_color")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "Lava Color",
                "Customize the lava color."
            );

            if (lavaHexField != null && lavaColorSlider != null) {
                int previewSize = 18;
                int previewX = lavaHexField.getX() + HEX_WIDTH + 5;
                int previewY = lavaColorSlider.getY();

                context.fill(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, 0xFF000000);
                context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000 | BetterLiquid.getLavaColor());
            }

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_liquid_lava_transparency")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "Lava Transparency",
                "Set lava transparency from 0% to 100%."
            );

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_liquid_lava_color_intensity")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "Lava Color Intensity",
                "Set custom lava color intensity from 0% to 100%."
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
        BetterLiquid.refreshBlockedStatus();

        if (client != null) {
            client.reloadResources();
        }

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