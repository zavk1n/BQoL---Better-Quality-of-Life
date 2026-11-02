package com.zavk1n.bqol.client.screen.featurescreen.totems;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterTotems;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class TotemsConfigScreen extends MainConfigScreen {
    private ButtonWidget totemCounter, holyWorldTotemsBtn, holyWorldTotemsSettingsBtn, reallyWorldTotemsBtn, reallyWorldTotemsSettingsBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;

    private boolean changed = false;

    public TotemsConfigScreen(Screen parent) {
        super(Text.literal("Better Totems Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_totems")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        totemCounter = null;

        holyWorldTotemsBtn = null;
        holyWorldTotemsSettingsBtn = null;

        reallyWorldTotemsBtn = null;
        reallyWorldTotemsSettingsBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_totems_holyworld")) {
            holyWorldTotemsBtn = createButton(rightX, y, config::isHolyWorldTotemsEnabled, config::setHolyWorldTotemsEnabled);
            holyWorldTotemsSettingsBtn = ButtonWidget.builder(Text.literal("Settings"),
                    button -> {
                        if (client != null && !LiteApiManager.isFeatureBlocked("better_totems_holyworld")) {
                            saveIfChanged();
                            client.setScreen(new HolyWorldTotemsConfigScreen(this));
                        }
                    }
                )
                .dimensions(rightX + BUTTON_WIDTH + 10, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(holyWorldTotemsBtn);
            addDrawableChild(holyWorldTotemsSettingsBtn);

            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_totems_reallyworld")) {
            reallyWorldTotemsBtn = createButton(rightX, y, config::isReallyWorldTotemsEnabled, config::setReallyWorldTotemsEnabled);
            reallyWorldTotemsSettingsBtn = ButtonWidget.builder(Text.literal("Settings"),
                    button -> {
                        if (client != null && !LiteApiManager.isFeatureBlocked("better_totems_reallyworld")) {
                            saveIfChanged();
                            client.setScreen(new ReallyWorldTotemsConfigScreen(this));
                        }
                    }
                )
                .dimensions(rightX + BUTTON_WIDTH + 10, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(reallyWorldTotemsBtn);
            addDrawableChild(reallyWorldTotemsSettingsBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_totems_counter")) {
            totemCounter = ButtonWidget.builder(
                    Text.literal(config.isBetterTotemsCounterEnabled() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterTotemsCounterEnabled();
                        config.setBetterTotemsCounterEnabled(state);
                        changed = true;
                        updateButton(totemCounter, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(totemCounter);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(width / 2 - 50, height - 40, 100, 25)
            .build()
        );

        updateAllButtons();
    }

    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget button = ButtonWidget.builder(Text.empty(),
                btn -> {
                    boolean enabled = !getter.getAsBoolean();
                    setter.accept(enabled);
                    changed = true;
                    updateButton(btn, enabled);
                }
            )
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        updateButton(button, getter.getAsBoolean());

        return button;
    }

    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(style -> style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF))
        );
    }

    private void updateAllButtons() {
        if (holyWorldTotemsBtn != null) {
            updateButton(holyWorldTotemsBtn, config.isHolyWorldTotemsEnabled());
        }

        if (reallyWorldTotemsBtn != null) {
            updateButton(reallyWorldTotemsBtn, config.isReallyWorldTotemsEnabled());
        }

        if (totemCounter != null) {
            updateButton(totemCounter, config.isBetterTotemsCounterEnabled());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_totems_holyworld")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "HolyWorld Totems",
                "Customization totems for the HolyWorld."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_totems_reallyworld")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "ReallyWorld Totems",
                "Customization totems for the ReallyWorld."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_totems_counter")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Totem Counter",
                "Displays number of your totems and enemies totems."
            );
        }
    }

    private void renderLabel(DrawContext context, int x, int y, int mouseX, int mouseY, String title, String description) {
        int titleWidth = textRenderer.getWidth(title);

        boolean hovered = mouseX >= x &&
            mouseX <= x + titleWidth &&
            mouseY >= y &&
            mouseY <= y + textRenderer.fontHeight;

        int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

        context.drawText(textRenderer, Text.literal(title), x, y, color, false);
        context.drawText(textRenderer, Text.literal(description), x, y + 12, 0xFF888888, false);
    }

    private void saveIfChanged() {
        if (changed) {
            save();
        }
    }

    private void save() {
        config.save();

        BetterTotems.refreshBlockedStatus();

        changed = false;
    }

    @Override
    public void close() {
        saveIfChanged();

        if (client != null) {
            client.setScreen(parent);
        }
    }
}