package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterParticles;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ParticlesConfigScreen extends MainConfigScreen{
    private ButtonWidget shulkersBtn, shulkersSettingsBtn, totemsBtn, totemsSettingsBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    public ParticlesConfigScreen(Screen parent) {
        super(Text.literal("Better Particles Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_particles")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        shulkersBtn = null;
        shulkersSettingsBtn = null;
        totemsBtn = null;
        totemsSettingsBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_particles_shulkers")) {
            shulkersBtn = createButton(rightX, y, config::isBetterParticlesShulkersEnabled, config::setBetterParticlesShulkersEnabled);
            shulkersSettingsBtn = ButtonWidget.builder(
                    Text.literal("Settings"),
                    button -> {
                        if (client != null &&
                            !LiteApiManager.isFeatureBlocked("better_particles_shulkers")) {
                            client.setScreen(new ShulkersParticlesConfigScreen(this));
                        }
                    })
                .dimensions(rightX + BUTTON_WIDTH + 10, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(shulkersBtn);
            addDrawableChild(shulkersSettingsBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_particles_totems")) {
            totemsBtn = createButton(rightX, y, config::isBetterParticlesTotemsEnabled, config::setBetterParticlesTotemsEnabled);
            totemsSettingsBtn = ButtonWidget.builder(
                    Text.literal("Settings"),
                    button -> {
                        if (client != null &&
                            !LiteApiManager.isFeatureBlocked("better_particles_totems")) {
                            client.setScreen(new TotemsParticlesConfigScreen(this));
                        }
                    })
                .dimensions(rightX + BUTTON_WIDTH + 10, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(totemsBtn);
            addDrawableChild(totemsSettingsBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
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

    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    private void updateAllButtons() {
        if (shulkersBtn != null) {
            updateButton(shulkersBtn, config.isBetterParticlesShulkersEnabled());
        }

        if (totemsBtn != null) {
            updateButton(totemsBtn, config.isBetterParticlesTotemsEnabled());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_particles_shulkers")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Shulkers Particles",
                "Customizes shulker particles."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_particles_totems")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Totems Particles",
                "Customizes totems particles."
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