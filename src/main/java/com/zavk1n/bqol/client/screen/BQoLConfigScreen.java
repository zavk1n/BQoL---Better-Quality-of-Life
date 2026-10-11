package com.zavk1n.bqol.client.screen;

import com.zavk1n.bqol.client.KeybindManager;
import com.zavk1n.bqol.client.screen.featurescreen.*;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.features.*;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
    ButtonWidget keybindButton;

    KeyBinding keyBinding;

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
    private final List<FeaturePanel> filteredPanels = new ArrayList<>();

    private TextFieldWidget searchField;

    private long openTime;

    private float animationProgress = 0F;

    private static final int PANEL_START_Y = 55;
    private static final int PANEL_SPACING = 45;

    private static final int TEXT_COLUMN_WIDTH = 260;

    private static final int TOGGLE_WIDTH = 80;
    private static final int CONFIG_WIDTH = 80;
    private static final int KEYBIND_WIDTH = 100;

    private static final int COLUMN_GAP = 12;
    private static final int BUTTON_GAP = 8;

    private static final int SEARCH_WIDTH = 150;
    private static final int SEARCH_HEIGHT = 20;

    private static final int GRID_OFFSET_X = -70;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SEARCH_GAP_FROM_SCROLLBAR = 40;

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
        filteredPanels.clear();

        resetScroll();

        boolean isConnected = client != null
            && client.world != null
            && client.player != null;

        if (!isConnected) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Not connected to server"), button -> {})
                .dimensions(width / 2 - 100, height / 2 - 10, 200, 20)
                .build()
            );

            return;
        }

        createFeaturePanels();
        createFeatureButtons();
        createSearchField();

        applySearchFilter();

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

    private void createSearchField() {
        int scrollbarX = getScrollbarX();

        int searchX = scrollbarX + SCROLLBAR_WIDTH + SEARCH_GAP_FROM_SCROLLBAR;
        int searchY = getContentTop();

        searchField = new TextFieldWidget(textRenderer, searchX, searchY, SEARCH_WIDTH, SEARCH_HEIGHT, Text.literal("Search"));
        searchField.setMaxLength(100);
        searchField.setPlaceholder(Text.literal("Search...")
            .styled(style -> style.withColor(0xFF888888))
        );
        searchField.setChangedListener(query -> {
            applySearchFilter();
        });

        addDrawableChild(searchField);
    }

    private void createFeaturePanels() {
        featurePanels.add(new FeaturePanel(
                "Better Sprint",
                "Advanced Auto-sprint.",
                "better_sprint",
                config.isBetterSprintEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Better RCT",
                "Auto Reconnect util",
                "better_rct",
                config.isBetterRCTEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Better Sounds",
                "Sound management.",
                "better_sounds",
                config.isBetterSoundsEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Better Interact",
                "Various interaction improvements.",
                "better_interact",
                config.isBetterInteractEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Better Tnt",
                "Smart addons for TNT.",
                "better_tnt",
                config.isBetterTntEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Better Prefixes",
                "Customize your nickname.",
                "better_prefixes",
                config.isBetterPrefixesEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Better Holograms",
                "Tweaks for holograms.",
                "better_holograms",
                config.isBetterHologramsEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Better Spheres",
                "Customization of server spheres.",
                "better_spheres",
                config.isBetterSpheresEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Better Sky",
                "Customization of sky.",
                "better_sky",
                config.isBetterSkyEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Shulker Particles",
                "Custom particles for shulkers.",
                "shulker_particles",
                config.isShulkerParticlesEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Better Fog",
                "Variably customizable fog.",
                "better_fog",
                config.isBetterFogEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Custom Health",
                "Custom indicator displaying players health.",
                "custom_health",
                config.isCustomHealthEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "No Render",
                "Disables the rendering of various things.",
                "no_render",
                config.isNoRenderEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.add(new FeaturePanel(
                "Better Mine",
                "A smart assistant for auto-mines on servers.",
                "better_mine",
                config.isBetterMineEnabled(),
                getTextColumnX(),
                PANEL_START_Y,
                true
            )
        );

        featurePanels.removeIf(panel -> isModuleBlocked(panel.configKey));

        for (FeaturePanel panel : featurePanels) {
            panel.keyBinding = KeybindManager.getFeatureKeyBinding(panel.configKey);
        }
    }

    private void applySearchFilter() {
        if (searchField == null) {
            return;
        }

        String query = searchField.getText()
            .trim()
            .toLowerCase(Locale.ROOT);

        filteredPanels.clear();

        if (query.isEmpty()) {
            filteredPanels.addAll(featurePanels);
        } else {
            for (FeaturePanel panel : featurePanels) {
                String title = panel.title.toLowerCase(Locale.ROOT);

                if (title.contains(query)) {
                    filteredPanels.add(panel);
                }
            }

            filteredPanels.sort(Comparator.comparingInt(panel -> getSearchPriority(panel, query)));
        }

        resetScroll();

        rebuildFilteredButtons();

        updateMaxScrollForFilteredPanels();

        rebuildUI();
    }

    private int getSearchPriority(FeaturePanel panel, String query) {
        String title = panel.title.toLowerCase(Locale.ROOT);

        if (title.equals(query)) {
            return 0;
        }

        if (title.startsWith(query)) {
            return 1;
        }

        for (String word : title.split("\\s+")) {
            if (word.startsWith(query)) {
                return 2;
            }
        }

        if (title.contains(query)) {
            return 3;
        }

        return 4;
    }

    private void createFeatureButtons() {
        for (FeaturePanel panel : featurePanels) {
            panel.x = getTextColumnX();

            panel.titleWidth = textRenderer.getWidth(panel.titleText);

            createButtons(panel);
        }
    }

    private void rebuildFilteredButtons() {
        for (FeaturePanel panel : featurePanels) {
            boolean visible = filteredPanels.contains(panel);

            if (panel.toggleButton != null) {
                panel.toggleButton.visible = visible;
            }

            if (panel.configButton != null) {
                panel.configButton.visible = visible;
            }

            if (panel.keybindButton != null) {
                panel.keybindButton.visible = visible;
            }
        }
    }

    private int getGridWidth() {
        return TEXT_COLUMN_WIDTH
            + COLUMN_GAP
            + TOGGLE_WIDTH
            + BUTTON_GAP
            + CONFIG_WIDTH
            + BUTTON_GAP
            + KEYBIND_WIDTH;
    }

    private int getGridStartX() {
        return (width - getGridWidth()) / 2 + GRID_OFFSET_X;
    }

    private int getTextColumnX() {
        return getGridStartX();
    }

    private int getButtonsStartX() {
        return getTextColumnX() + TEXT_COLUMN_WIDTH + COLUMN_GAP;
    }

    public int getScrollbarX() {
        return getGridStartX() + getGridWidth() + 15;
    }

    private void openConfigScreen(FeaturePanel panel) {
        if (client == null) {
            return;
        }

        switch (panel.configKey) {
            case "better_sprint" -> client.setScreen(new SprintConfigScreen(this));
            case "better_rct" -> client.setScreen(new RCTConfigScreen(this));
            case "better_sounds" -> client.setScreen(new SoundsConfigScreen(this));
            case "better_interact" -> client.setScreen(new InteractConfigScreen(this));
            case "better_tnt" -> client.setScreen(new TNTConfigScreen(this));
            case "better_prefixes" -> client.setScreen(new PrefixesConfigScreen(this));
            case "better_holograms" -> client.setScreen(new HologramsConfigScreen(this));
            case "better_spheres" -> client.setScreen(new SpheresConfigScreen(this));
            case "better_sky" -> client.setScreen(new SkyConfigScreen(this));
            case "shulker_particles" -> client.setScreen(new ShulkerParticlesConfigScreen(this));
            case "better_fog" -> client.setScreen(new FogConfigScreen(this));
            case "custom_health" -> client.setScreen(new CustomHealthConfigScreen(this));
            case "no_render" -> client.setScreen(new NoRenderConfigScreen(this));
            case "better_mine" -> client.setScreen(new MineConfigScreen(this));
        }
    }

    private boolean isModuleBlocked(String configKey) {
        String liteId = switch (configKey) {

            case "better_sprint" -> "better_sprint";
            case "better_rct" -> "better_rct";
            case "better_sounds" -> "better_sounds";
            case "better_interact" -> "better_interact";
            case "better_tnt" -> "better_tnt";
            case "better_prefixes" -> "better_prefixes";
            case "better_holograms" -> "better_holograms";
            case "better_spheres" -> "better_spheres";
            case "better_sky" -> "better_sky";
            case "shulker_particles" -> "shulker_particles";
            case "better_fog" -> "better_fog";
            case "custom_health" -> "custom_health";
            case "no_render" -> "no_render";
            case "better_mine" -> "better_mine";

            default -> null;
        };

        return liteId != null && LiteApiManager.isFeatureBlocked(liteId);
    }

    private void rebuildUI() {
        int y = PANEL_START_Y - (int) scrollOffset;

        for (FeaturePanel panel : filteredPanels) {
            panel.x = getTextColumnX();
            panel.y = y;

            if (panel.toggleButton != null) {
                panel.toggleButton.setY(y - 3);
            }

            if (panel.configButton != null) {
                panel.configButton.setY(y - 3);
            }

            if (panel.keybindButton != null) {
                panel.keybindButton.setY(y - 3);
            }

            y += PANEL_SPACING;
        }

        updateMaxScrollForFilteredPanels();
    }

    private void updateMaxScrollForFilteredPanels() {
        int contentBottomY = PANEL_START_Y;

        if (!filteredPanels.isEmpty()) {
            contentBottomY = PANEL_START_Y + (filteredPanels.size() - 1) * PANEL_SPACING + 30;
        }

        setMaxScroll(Math.max(0F, contentBottomY - getContentBottom()));
    }

    private void toggleFeature(FeaturePanel panel) {
        switch (panel.configKey) {

            case "better_sprint" -> {
                panel.enabled = !panel.enabled;
                config.setBetterSprintEnabled(panel.enabled);
                BetterSprint.setEnabled(panel.enabled);
            }

            case "better_rct" -> {
                panel.enabled = !panel.enabled;
                config.setBetterRCTEnabled(panel.enabled);
                BetterRCT.setEnabled(panel.enabled);
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

            case "better_prefixes" -> {
                panel.enabled = !panel.enabled;
                config.setBetterPrefixesEnabled(panel.enabled);
                BetterPrefixes.setEnabled(panel.enabled);
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

            case "better_mine" -> {
                panel.enabled = !panel.enabled;
                config.setBetterMineEnabled(panel.enabled);
                BetterMine.setEnabled(panel.enabled);
            }
        }

        config.save();

        updateButtonsText(panel);
    }

    private void createButtons(FeaturePanel panel) {
        int buttonHeight = 25;

        int buttonsStartX = getButtonsStartX();

        panel.toggleButton = ButtonWidget.builder(panel.enabled ? Text.literal("Enabled")
                    .styled(style -> style.withColor(ACCENT_COLOR)) : Text.literal("Disabled"),
                button -> {
                    toggleFeature(panel);
                    updateAllButtons();
                })
            .dimensions(buttonsStartX, panel.y - 3, TOGGLE_WIDTH, buttonHeight)
            .build();

        addDrawableChild(panel.toggleButton);

        int nextX = buttonsStartX + TOGGLE_WIDTH + BUTTON_GAP;

        if (panel.hasConfig && !isModuleBlocked(panel.configKey)) {
            panel.configButton = ButtonWidget.builder(Text.literal("Config"),
                    button -> openConfigScreen(panel))
                .dimensions(nextX, panel.y - 3, CONFIG_WIDTH, buttonHeight)
                .build();

            addDrawableChild(panel.configButton);

            nextX += CONFIG_WIDTH + BUTTON_GAP;
        }

        if (panel.keyBinding != null) {
            int keybindWidth = getKeybindButtonWidth(panel);

            panel.keybindButton = ButtonWidget.builder(getKeybindButtonText(panel),
                    button -> {
                        KeybindManager.startKeybindListening(panel.configKey);
                        updateAllButtons();
                    })
                .dimensions(nextX, panel.y - 3, keybindWidth, buttonHeight)
                .build();

            addDrawableChild(panel.keybindButton);
        }
    }

    private int getKeybindButtonWidth(FeaturePanel panel) {
        Text text = getKeybindButtonText(panel);

        int textWidth = textRenderer.getWidth(text);

        return MathHelper.clamp(textWidth + 20, 55, 130);
    }

    private Text getKeybindButtonText(FeaturePanel panel) {
        if (KeybindManager.isKeybindListening(panel.configKey)) {
            return Text.literal("Press key...")
                .styled(style -> style.withColor(ACCENT_COLOR));
        }

        if (panel.keyBinding == null) {
            return Text.literal("Key Unbound");
        }

        return Text.literal("Bind: " + getEnglishKeyName(panel.keyBinding));
    }

    private String getEnglishKeyName(KeyBinding keyBinding) {
        String translationKey = keyBinding.getBoundKeyTranslationKey();

        if (translationKey.startsWith("key.keyboard.")) {
            String key = translationKey.substring("key.keyboard.".length());

            return key.toUpperCase(Locale.ROOT);
        }

        if (translationKey.startsWith("key.mouse.")) {
            String mouseKey = translationKey.substring("key.mouse.".length());

            return switch (mouseKey) {
                case "left" -> "Mouse 1";
                case "right" -> "Mouse 2";
                case "middle" -> "Mouse 3";
                case "4" -> "Mouse 4";
                case "5" -> "Mouse 5";
                case "6" -> "Mouse 6";
                case "7" -> "Mouse 7";
                case "8" -> "Mouse 8";
                default -> "Mouse";
            };
        }

        return translationKey
            .replace("key.", "")
            .toUpperCase(Locale.ROOT);
    }

    private void updateButtonsText(FeaturePanel panel) {
        if (panel.toggleButton == null) {
            return;
        }

        if (panel.enabled) {
            panel.toggleButton.setMessage(Text.literal("Enabled")
                    .styled(style -> style.withColor(ACCENT_COLOR))
            );
        } else {
            panel.toggleButton.setMessage(Text.literal("Disabled"));
        }
    }

    private void updateAllButtons() {
        for (FeaturePanel panel : featurePanels) {

            if (panel.toggleButton != null) {
                updateButtonsText(panel);
            }

            if (panel.keybindButton != null) {
                panel.keybindButton.setMessage(
                    getKeybindButtonText(panel)
                );

                panel.keybindButton.setWidth(
                    getKeybindButtonWidth(panel)
                );
            }
        }
    }

    private void renderButtonIfVisible(ButtonWidget button, DrawContext context, int mouseX, int mouseY, float delta) {
        int top = button.getY();
        int bottom = top + button.getHeight();

        boolean visible = bottom > getContentTop() && top < getContentBottom();

        button.visible = visible;
        button.active = visible;

        if (!visible) {
            return;
        }

        context.enableScissor(0, getContentTop(), width, getContentBottom());

        button.render(context, mouseX, mouseY, delta);

        context.disableScissor();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long currentTime = Util.getMeasuringTimeMs();

        animationProgress = MathHelper.clamp((currentTime - openTime) / 500F, 0F, 1F);

        rebuildUI();

        updateAllButtons();

        for (FeaturePanel panel : featurePanels) {
            if (panel.toggleButton != null) {
                panel.toggleButton.visible = false;
            }

            if (panel.configButton != null) {
                panel.configButton.visible = false;
            }

            if (panel.keybindButton != null) {
                panel.keybindButton.visible = false;
            }
        }

        super.render(context, mouseX, mouseY, delta);

        context.enableScissor(0, getContentTop(), width, getContentBottom());

        for (FeaturePanel panel : filteredPanels) {
            boolean hovered = mouseX >= panel.x
                && mouseX <= panel.x + panel.titleWidth
                && mouseY >= panel.y
                && mouseY <= panel.y + textRenderer.fontHeight;

            int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

            context.drawText(textRenderer, panel.titleText, panel.x, panel.y, color, false);
            context.drawText(textRenderer, panel.descriptionText, panel.x, panel.y + 12, 0xFF888888, false);
        }

        context.disableScissor();

        for (FeaturePanel panel : filteredPanels) {
            if (panel.toggleButton != null) {
                renderButtonIfVisible(panel.toggleButton, context, mouseX, mouseY, delta);
            }

            if (panel.configButton != null) {
                renderButtonIfVisible(panel.configButton, context, mouseX, mouseY, delta);
            }

            if (panel.keybindButton != null) {
                renderButtonIfVisible(panel.keybindButton, context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public void close() {
        config.save();

        KeybindManager.stopKeybindListening();

        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (KeybindManager.isKeybindListening()) {
            return false;
        }

        return true;
    }
}