package com.zavk1n.bqol.client.screen;

import com.zavk1n.bqol.client.screen.featurescreen.*;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.features.*;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

class FeaturePanel {

    String title;
    String description;
    String configKey;

    Text titleText;
    Text descriptionText;

    int titleWidth;

    boolean enabled;
    boolean hasConfig;

    int x;
    int y;
    int originalY;

    ButtonWidget toggleButton;
    ButtonWidget configButton;

    FeaturePanel(
        String title,
        String description,
        String configKey,
        boolean enabled,
        int x,
        int y,
        boolean hasConfig
    ) {
        this.title = title;
        this.description = description;
        this.configKey = configKey;

        this.enabled = enabled;

        this.x = x;
        this.y = y;
        this.originalY = y;

        this.hasConfig = hasConfig;

        this.titleText = Text.literal(title);
        this.descriptionText = Text.literal(description);
    }
}

public class BQoLConfigScreen extends MainConfigScreen {
    private final Screen parent;
    private final BQoLConfig config = BQoLConfig.getInstance();
    private final List<FeaturePanel> featurePanels = new ArrayList<>();

    private long openTime;

    private float animationProgress = 0F;

    private static final int PANEL_START_Y = 60;
    private static final int PANEL_SPACING = 45;

    /// Конструктор
    public BQoLConfigScreen(Screen parent) {
        super(Text.literal("BQoL Configuration"), parent);

        this.parent = parent;
        this.openTime = Util.getMeasuringTimeMs();
    }

    @Override
    protected void init() {
        super.init();

        clearChildren();
        featurePanels.clear();

        resetScroll();

        boolean isConnected = client != null &&
                client.world != null &&
                client.player != null;

        if (!isConnected) {addDrawableChild(ButtonWidget.builder(Text.literal("Not connected to server"),
                button -> {}
            )
                    .dimensions(width / 2 - 100, height / 2 - 10, 200, 20)
                    .build());
            return;
        }

        createFeaturePanels();

        int index = 0;

        for (FeaturePanel panel : featurePanels) {
            panel.originalY = PANEL_START_Y + index * PANEL_SPACING;
            panel.y = panel.originalY;
            panel.titleWidth = textRenderer.getWidth(panel.titleText);

            createButtons(panel);

            index++;
        }

        int contentBottomY = PANEL_START_Y;

        if (!featurePanels.isEmpty()) {
            contentBottomY = PANEL_START_Y + (featurePanels.size() - 1) * PANEL_SPACING + 30;
        }

        setMaxScroll(Math.max(0F, contentBottomY - getContentBottom()));

        rebuildUI();

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"),
                    button -> {
                        config.save();

                        if (client != null) {
                            client.setScreen(parent);
                        }
                    }
                )
                .dimensions(width / 2 - 50, height - 40, 100, 25)
                .build()
        );

        updateAllButtons();
    }

    /// Модули
    private void createFeaturePanels() {
        featurePanels.add(new FeaturePanel("Better Sprint", "Advanced Auto-sprint.", "better_sprint",
                config.isBetterSprintEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING, true
            )
        );

        featurePanels.add(new FeaturePanel("Better Sounds", "Sound management.", "better_sounds",
                config.isBetterSoundsEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING * 2, true
            )
        );

        featurePanels.add(new FeaturePanel("Better Interact", "Various interaction improvements.", "better_interact",
                config.isBetterInteractEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING * 3, true
            )
        );

        featurePanels.add(new FeaturePanel("Better Tnt", "Smart addons for TNT.", "better_tnt",
                config.isBetterTntEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING * 4, true
            )
        );

        featurePanels.add(new FeaturePanel("Better Holograms", "Tweaks for holograms.", "better_holograms",
                config.isBetterHologramsEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING * 5, true
            )
        );

        featurePanels.add(new FeaturePanel("Better Spheres", "Customization of server spheres.", "better_spheres",
                config.isBetterSpheresEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING * 6, true
            )
        );

        featurePanels.add(new FeaturePanel("Better Sky", "Customization of sky.", "better_sky",
                config.isBetterSkyEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING * 7, true
            )
        );

        featurePanels.add(new FeaturePanel("Shulker Particles", "Custom particles for shulkers.", "shulker_particles",
                config.isShulkerParticlesEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING * 8, true
            )
        );

        featurePanels.add(new FeaturePanel("Better Fog", "Variably customizable fog.", "better_fog",
                config.isBetterFogEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING * 9, true
            )
        );

        featurePanels.add(new FeaturePanel("Custom Health", "Custom indicator displaying players health.", "custom_health",
                config.isCustomHealthEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING * 10, true
            )
        );

        featurePanels.add(new FeaturePanel("No Render", "Disables the rendering of various things.", "no_render",
                config.isNoRenderEnabled(),
                width / 4,
                PANEL_START_Y + PANEL_SPACING * 11, true
            )
        );

        featurePanels.removeIf(panel -> isModuleBlocked(panel.configKey));
    }

    private void openConfigScreen(FeaturePanel panel) {
        if (client == null) {
            return;
        }

        switch (panel.configKey) {
            case "better_sprint" -> client.setScreen(new SprintConfigScreen(this));
            case "better_sounds" -> client.setScreen(new SoundsConfigScreen(this));
            case "better_interact" -> client.setScreen(new InteractConfigScreen(this));
            case "better_tnt" -> client.setScreen(new TNTConfigScreen(this));
            case "better_holograms" -> client.setScreen(new HologramsConfigScreen(this));
            case "better_spheres" -> client.setScreen(new SpheresConfigScreen(this));
            case "better_sky" -> client.setScreen(new SkyConfigScreen(this));
            case "shulker_particles" -> client.setScreen(new ShulkerParticlesConfigScreen(this));
            case "better_fog" -> client.setScreen(new FogConfigScreen(this));
            case "custom_health" -> client.setScreen(new CustomHealthConfigScreen(this));
            case "no_render" -> client.setScreen(new NoRenderConfigScreen(this));
        }
    }

    private boolean isModuleBlocked(String configKey) {
        String liteId = switch (configKey) {

            case "better_sprint" -> "better_sprint";
            case "better_sounds" -> "better_sounds";
            case "better_interact" -> "better_interact";
            case "better_tnt" -> "better_tnt";
            case "better_holograms" -> "better_holograms";
            case "better_spheres" -> "better_spheres";
            case "better_sky" -> "better_sky";
            case "shulker_particles" -> "shulker_particles";
            case "better_fog" -> "better_fog";
            case "custom_health" -> "custom_health";
            case "no_render" -> "no_render";

            default -> null;
        };

        return liteId != null &&
            LiteApiManager.isFeatureBlocked(liteId);
    }

    /// Ядро
    private void rebuildUI() {
        int y = PANEL_START_Y - (int) scrollOffset;

        for (FeaturePanel panel : featurePanels) {
            panel.y = y;

            if (panel.toggleButton != null) {
                panel.toggleButton.setY(y - 3);
            }

            if (panel.configButton != null) {
                panel.configButton.setY(y - 3);
            }

            y += PANEL_SPACING;
        }
    }

    private void toggleFeature(FeaturePanel panel) {
        switch (panel.configKey) {
            case "better_sprint" -> {
                panel.enabled = !panel.enabled;
                config.setBetterSprintEnabled(panel.enabled);
                BetterSprint.setEnabled(panel.enabled);
            }

            case "better_sounds" -> {
                panel.enabled = !panel.enabled;
                config.setBetterSoundsEnabled(panel.enabled);
                BetterSounds.setEnabled(panel.enabled);
            }

            case "better_interact" -> {
                panel.enabled = !panel.enabled;
                config.setBetterInteractEnabled(panel.enabled);
                BetterInteract.setEnabled(panel.enabled);
            }

            case "better_tnt" -> {
                panel.enabled = !panel.enabled;
                config.setBetterTntEnabled(panel.enabled);
                BetterTnt.setEnabled(panel.enabled);
            }

            case "better_holograms" -> {
                panel.enabled = !panel.enabled;
                config.setBetterHologramsEnabled(panel.enabled);
                BetterHolograms.setEnabled(panel.enabled);
            }

            case "better_spheres" -> {
                panel.enabled = !panel.enabled;
                config.setBetterSpheresEnabled(panel.enabled);
                BetterSpheres.setEnabled(panel.enabled);
            }

            case "better_sky" -> {
                panel.enabled = !panel.enabled;
                config.setBetterSkyEnabled(panel.enabled);
                BetterSky.setEnabled(panel.enabled);
            }

            case "shulker_particles" -> {
                panel.enabled = !panel.enabled;
                config.setShulkerParticlesEnabled(panel.enabled);
                ShulkerParticles.setEnabled(panel.enabled);
            }

            case "better_fog" -> {
                panel.enabled = !panel.enabled;
                config.setBetterFogEnabled(panel.enabled);
                BetterFog.setEnabled(panel.enabled);
            }

            case "custom_health" -> {
                panel.enabled = !panel.enabled;
                config.setCustomHealthEnabled(panel.enabled);
                CustomHealth.setEnabled(panel.enabled);
                if (!panel.enabled) {
                    CustomHealth.resetDisplay();
                }
            }

            case "no_render" -> {
                panel.enabled = !panel.enabled;
                config.setNoRenderEnabled(panel.enabled);
                NoRender.setEnabled(panel.enabled);
            }
        }

        config.save();

        updateButtonsText(panel);
    }

    /// Создание элементов
    private void createButtons(FeaturePanel panel) {
        int buttonWidth = 80;
        int buttonHeight = 25;

        int buttonsStartX = width / 2 + 50;

        panel.toggleButton = ButtonWidget.builder(panel.enabled
                        ? Text.literal("Enabled")
                        .styled(style -> style.withColor(ACCENT_COLOR)) : Text.literal("Disabled"),
                    button -> {
                        toggleFeature(panel);
                        updateAllButtons();
                    })
                .dimensions(buttonsStartX, panel.y - 3, buttonWidth, buttonHeight)
                .build();

        addDrawableChild(panel.toggleButton);

        if (panel.hasConfig && !isModuleBlocked(panel.configKey)) {
            panel.configButton = ButtonWidget.builder(
                        Text.literal("Config"),
                        button ->
                            openConfigScreen(panel))
                    .dimensions(buttonsStartX + buttonWidth + 10, panel.y - 3, buttonWidth, buttonHeight)
                    .build();

            addDrawableChild(
                panel.configButton
            );
        }
    }

    /// Обновление элементов
    private void updateButtonsText(FeaturePanel panel) {
        if (panel.toggleButton == null) {
            return;
        }

        if (panel.enabled) {
            panel.toggleButton.setMessage(Text.literal("Enabled").styled(style -> style.withColor(ACCENT_COLOR)));
        } else {
            panel.toggleButton.setMessage(Text.literal("Disabled"));
        }
    }

    private void updateAllButtons() {
        for (FeaturePanel panel : featurePanels) {

            if (panel.toggleButton != null) {
                updateButtonsText(panel);
            }
        }
    }

    /// Рендер
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long currentTime = Util.getMeasuringTimeMs();

        animationProgress = MathHelper.clamp((currentTime - openTime) / 500F, 0F, 1F);

        rebuildUI();

        for (FeaturePanel panel : featurePanels) {
            if (panel.toggleButton != null) {
                panel.toggleButton.visible = false;
            }

            if (panel.configButton != null) {
                panel.configButton.visible = false;
            }
        }

        super.render(context, mouseX, mouseY, delta);

        context.enableScissor(0, getContentTop(), width, getContentBottom());

        for (FeaturePanel panel : featurePanels) {
            boolean hovered = mouseX >= panel.x &&
                    mouseX <= panel.x + panel.titleWidth &&
                    mouseY >= panel.y &&
                    mouseY <= panel.y + textRenderer.fontHeight;

            int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

            context.drawText(textRenderer, panel.titleText, panel.x, panel.y, color, false);
            context.drawText(textRenderer, panel.descriptionText, panel.x, panel.y + 12, 0xFF888888, false);
        }

        for (FeaturePanel panel : featurePanels) {
            if (panel.toggleButton != null) {
                panel.toggleButton.visible = true;
                panel.toggleButton.active = true;
            }

            if (panel.configButton != null) {
                panel.configButton.visible = true;
                panel.configButton.active = true;
            }
        }

        for (FeaturePanel panel : featurePanels) {
            if (panel.toggleButton != null) {
                panel.toggleButton.render(context, mouseX, mouseY, delta);
            }

            if (panel.configButton != null) {
                panel.configButton.render(context, mouseX, mouseY, delta);
            }
        }

        context.disableScissor();

        for (FeaturePanel panel : featurePanels) {
            if (panel.toggleButton != null) {

                int top = panel.toggleButton.getY();
                int bottom = top + panel.toggleButton.getHeight();

                boolean visible = bottom > getContentTop() &&
                    top < getContentBottom();

                panel.toggleButton.visible = visible;
                panel.toggleButton.active = visible;
            }

            if (panel.configButton != null) {
                int top = panel.configButton.getY();
                int bottom = top + panel.configButton.getHeight();

                boolean visible = bottom > getContentTop() &&
                    top < getContentBottom();

                panel.configButton.visible = visible;
                panel.configButton.active = visible;
            }
        }
    }

    /// Закрытие
    @Override
    public void close() {
        config.save();

        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}