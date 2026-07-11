package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterSpheres;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SpheresConfigScreen extends MainConfigScreen {

    /// Виджеты
    private ButtonWidget holyWorldSpheresBtn, holyWorldSpheresSettingsBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    /// Конструктор
    public SpheresConfigScreen(Screen parent) {
        super(Text.literal("BetterSpheres Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_spheres")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        holyWorldSpheresBtn = null;
        holyWorldSpheresSettingsBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_spheres_holyworld")) {
            holyWorldSpheresBtn = createButton(
                rightX,
                y,
                config::isHolyWorldSpheresEnabled,
                config::setHolyWorldSpheresEnabled
            );

            holyWorldSpheresSettingsBtn = ButtonWidget.builder(
                    Text.literal("Settings"),
                    button -> {
                        if (client != null &&
                            !LiteApiManager.isFeatureBlocked("better_spheres_holyworld")) {
                            client.setScreen(new HolyWorldSpheresConfigScreen(this));
                        }
                    })
                .dimensions(
                    rightX + BUTTON_WIDTH + 10,
                    y - 3,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT
                )
                .build();

            addDrawableChild(holyWorldSpheresBtn);
            addDrawableChild(holyWorldSpheresSettingsBtn);

            y += SPACING;
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

    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget button = ButtonWidget.builder(
                Text.literal(""),
                btn -> {
                    boolean enabled = !getter.getAsBoolean();

                    setter.accept(enabled);
                    changed = true;

                    updateButton(btn, enabled);
                })
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        updateButton(button, getter.getAsBoolean());

        return button;
    }

    /// Обновление состояния кнопок
    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
                .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    private void updateAllButtons() {
        if (holyWorldSpheresBtn != null) {
            updateButton(
                holyWorldSpheresBtn,
                config.isHolyWorldSpheresEnabled()
            );
        }
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_spheres_holyworld")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "HolyWorld Spheres",
                "Customization spheres for the HolyWorld."
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

        context.drawTextWithShadow(textRenderer, Text.literal(title), x, y, color);
        context.drawTextWithShadow(textRenderer, Text.literal(description), x, y + 12, 0xFF888888);
    }

    /// Сохранение и закрытие
    private void save() {
        config.save();
        BetterSpheres.refreshBlockedStatus();

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