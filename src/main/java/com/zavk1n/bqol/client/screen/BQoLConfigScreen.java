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

    private float animationProgress = 0f;

    private float scrollOffset = 0f;
    private float maxScroll = 0f;

    private final int visibleAreaTop = 50;
    private final int visibleAreaBottom = 40;

    public BQoLConfigScreen(Screen parent) {
        super(
            Text.literal("BQoL Configuration"),
            parent
        );

        this.parent = parent;
        this.openTime = Util.getMeasuringTimeMs();
    }

    @Override
    protected void init() {
        super.init();

        clearChildren();
        featurePanels.clear();

        scrollOffset = 0f;

        boolean isConnected = client != null &&
                client.world != null &&
                client.player != null;

        if (!isConnected) {
            addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Not connected to server"),
                        button -> {}
                    )
                    .dimensions(
                        width / 2 - 100,
                        height / 2 - 10,
                        200,
                        20
                    )
                    .build()
            );
            return;
        }

        int panelStartY = 60;
        int panelSpacing = 45;

        featurePanels.add(new FeaturePanel(
            "Better Sprint",
            "Auto sprint when pressing W",
            "better_sprint",
            config.isBetterSprintEnabled(),
            width / 4,
            panelStartY,
            true
        ));

        featurePanels.add(new FeaturePanel(
            "Better Sounds",
            "Sound management modes",
            "better_sounds",
            config.isBetterSoundsEnabled(),
            width / 4,
            panelStartY + panelSpacing * 2,
            true
        ));

        featurePanels.add(new FeaturePanel(
            "Better Spheres",
            "Visual sphere indicators",
            "better_spheres",
            config.isBetterSpheresEnabled(),
            width / 4,
            panelStartY + panelSpacing * 3,
            true
        ));

        featurePanels.add(new FeaturePanel(
            "Shulker Particles",
            "Custom shulker box particles",
            "shulker_particles",
            config.isShulkerParticlesEnabled(),
            width / 4,
            panelStartY + panelSpacing * 4,
            true
        ));

        featurePanels.add(new FeaturePanel(
            "Custom Fog",
            "Fog distance and biome settings",
            "custom_fog",
            config.isCustomFogEnabled(),
            width / 4,
            panelStartY + panelSpacing * 5,
            true
        ));

        featurePanels.add(new FeaturePanel(
            "Custom Health",
            "Health rendering options",
            "custom_health",
            config.isCustomHealthEnabled(),
            width / 4,
            panelStartY + panelSpacing * 6,
            true
        ));

        featurePanels.add(new FeaturePanel(
            "No Render",
            "No render",
            "no_render",
            config.isNoRenderEnabled(),
            width / 4,
            panelStartY + panelSpacing * 7,
            true
        ));

        featurePanels.removeIf(panel -> isModuleBlocked(panel.configKey));

        int index = 0;

        for (FeaturePanel panel : featurePanels) {
            panel.originalY = panelStartY + index * panelSpacing;
            panel.y = panel.originalY;

            panel.titleWidth = textRenderer.getWidth(panel.titleText);

            createButtons(panel);

            index++;
        }

        float totalHeight = panelStartY + index * panelSpacing + 40;
        float screenHeight = height - visibleAreaTop - visibleAreaBottom;

        maxScroll = Math.max(0, totalHeight - screenHeight);

        addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Save & Back"),
                    button -> {
                        config.save();

                        if (client != null) {
                            client.setScreen(parent);
                        }
                    }
                )
                .dimensions(
                    width / 2 - 50,
                    height - 40,
                    100,
                    25
                )
                .build()
        );

        updateAllButtons();
    }

    private void openConfigScreen(FeaturePanel panel) {
        if (this.client == null) {
            return;
        }

        switch (panel.configKey) {
            case "better_sprint" -> this.client.setScreen(new SprintConfigScreen(this));
            case "better_sounds" -> this.client.setScreen(new SoundsConfigScreen(this));
            case "better_spheres" -> this.client.setScreen(new SpheresConfigScreen(this));
            case "shulker_particles" -> this.client.setScreen(new ShulkerParticlesConfigScreen(this));
            case "custom_fog" -> this.client.setScreen(new CustomFogConfigScreen(this));
            case "custom_health" -> this.client.setScreen(new CustomHealthConfigScreen(this));
            case "no_render" -> this.client.setScreen(new NoRenderConfigScreen(this));
        }
    }

    /// Проверка на блокировку
    private boolean isModuleBlocked(String configKey) {
        String liteId = switch (configKey) {

            case "better_sprint" -> "better_sprint";
            case "better_sounds" -> "better_sounds";
            case "better_spheres" -> "better_spheres";
            case "shulker_particles" -> "shulker_particles";
            case "custom_fog" -> "custom_fog";
            case "custom_health" -> "custom_health";
            case "no_render" -> "no_render";

            default -> null;
        };

        return liteId != null && LiteApiManager.isFeatureBlocked(liteId);
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

            case "better_spheres" -> {
                panel.enabled = !panel.enabled;
                config.setBetterSpheresEnabled(panel.enabled);
                BetterSpheres.setEnabled(panel.enabled);
            }

            case "shulker_particles" -> {
                panel.enabled = !panel.enabled;
                config.setShulkerParticlesEnabled(panel.enabled);
                ShulkerParticles.setEnabled(panel.enabled);
            }

            case "custom_fog" -> {
                panel.enabled = !panel.enabled;
                config.setCustomFogEnabled(panel.enabled);
                CustomFog.setEnabled(panel.enabled);
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

    private void rebuildUI() {
        for (FeaturePanel panel : featurePanels) {
            panel.y = (int) (panel.originalY - scrollOffset);

            boolean visible = panel.y + 30 >= visibleAreaTop && panel.y <= height - visibleAreaBottom;

            if (panel.toggleButton != null) {
                panel.toggleButton.setY(panel.y - 3);
                panel.toggleButton.visible = visible;
            }

            if (panel.configButton != null) {
                panel.configButton.setY(panel.y - 3);
                panel.configButton.visible = visible;
            }
        }
    }

    private void createButtons(FeaturePanel panel) {
        int buttonWidth = 80;
        int buttonHeight = 25;
        int buttonsStartX = width / 2 + 50;

        panel.toggleButton = ButtonWidget.builder(
                panel.enabled ? Text.literal("Enabled").styled(s -> s.withColor(ACCENT_COLOR)) : Text.literal("Disabled"),
                    button -> {
                        toggleFeature(panel);
                        updateAllButtons();
                    }

                )
                .dimensions(
                    buttonsStartX,
                    panel.y - 3,
                    buttonWidth,
                    buttonHeight
                )
                .build();

        this.addDrawableChild(panel.toggleButton);

        if (panel.hasConfig && !isModuleBlocked(panel.configKey)) {
            panel.configButton = ButtonWidget.builder(
                        Text.literal("Config"),
                        button ->
                            openConfigScreen(panel)
                    )
                    .dimensions(
                        buttonsStartX + buttonWidth + 10,
                        panel.y - 3,

                        buttonWidth,
                        buttonHeight
                    )
                    .build();

            this.addDrawableChild(panel.configButton);
        }
    }

    private void updateButtonsText(FeaturePanel panel) {
        if (panel.toggleButton == null) {
            return;
        }

        if (panel.enabled) {
            panel.toggleButton.setMessage(
                Text.literal("Enabled")
                    .styled(
                        style ->
                            style.withColor(
                                ACCENT_COLOR
                            )
                    )
            );

        } else {
            panel.toggleButton.setMessage(
                Text.literal("Disabled")
            );
        }
    }

    private void updateAllButtons() {
        for (FeaturePanel panel : featurePanels) {
            if (panel.toggleButton != null) {
                updateButtonsText(panel);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long currentTime = Util.getMeasuringTimeMs();

        animationProgress = MathHelper.clamp(
            (currentTime - openTime) / 500F,
            0F,
            1F
        );

        super.render(context, mouseX, mouseY, delta);

        int titleColor = mouseX >= width / 2 - 100 && mouseX <= width / 2 + 100 && mouseY >= 25 && mouseY <= 45
                ? ACCENT_COLOR
                : 0xFFFFFFFF;

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 30, titleColor);

        for (FeaturePanel panel : featurePanels) {
            if (panel.y + 30 < visibleAreaTop ||
                panel.y > height - visibleAreaBottom) {
                continue;
            }

            boolean hovered = mouseX >= panel.x &&
                    mouseX <= panel.x + panel.titleWidth &&
                    mouseY >= panel.y &&
                    mouseY <= panel.y + textRenderer.fontHeight;

            int color = hovered
                ? ACCENT_COLOR
                : 0xFFFFFFFF;

            context.drawText(textRenderer, panel.titleText, panel.x, panel.y, color, false);
            context.drawText(textRenderer, panel.descriptionText, panel.x, panel.y + 12, 0xFF888888, false);
        }
    }

    /// Скролл
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount != 0) {
            float oldScrollOffset = scrollOffset;

            scrollOffset -= amount * 20;
            scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);

            if (Math.abs(scrollOffset - oldScrollOffset) > 0.1f) {
                rebuildUI();

                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    /// Закрытие
    @Override
    public void close() {
        config.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}