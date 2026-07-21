package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorCheckbox;
import com.zavk1n.bqol.features.CustomFog;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BiomeFogConfigScreen extends MainConfigScreen {

    /// Записи
    private final List<BiomeFogEntry> entries = new ArrayList<>();

    private static class BiomeFogEntry {
        final String group;
        final ColorCheckbox checkbox;
        BiomeFogEntry(String group, ColorCheckbox checkbox) {
            this.group = group;
            this.checkbox = checkbox;
        }
    }

    private static final int ENTRY_WIDTH = 125, ENTRY_HEIGHT = 36, COLUMNS = 3, COL_SPACING = 140, ROW_SPACING = 44;
    private boolean changed = false;

    /// Конструктор
    public BiomeFogConfigScreen(Screen parent) {
        super(Text.literal("Biome Fog Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("custom_fog_biome_fog")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();
        entries.clear();

        String[] groups = {
            "KrimsonForest", "NetherWastes", "BasaltDeltas",
            "WarpedForest", "SoulSand", "GrowthTaiga",
            "Mushrooms", "Snow", "Desert",
            "Savanna", "Mesa", "End"
        };

        int gridWidth = COLUMNS * COL_SPACING;
        int startX = (width - gridWidth) / 2;
        int startY = 50;

        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];

            int col = i % COLUMNS;
            int row = i / COLUMNS;

            int x = startX + col * COL_SPACING;
            int y = startY + row * ROW_SPACING;

            boolean enabled = config.isBiomeGroupEnabled(group);

            ColorCheckbox cb = new ColorCheckbox(
                x + (ENTRY_WIDTH - 15) / 2,
                y + 5,
                Text.empty(),
                enabled,
                button -> {
                    boolean newValue = !config.isBiomeGroupEnabled(group);
                    config.setBiomeGroupEnabled(group, newValue);
                    ((ColorCheckbox) button).setChecked(newValue);
                    changed = true;
                });

            cb.setColor(ACCENT_COLOR);

            entries.add(new BiomeFogEntry(group, cb));
            addDrawableChild(cb);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(width / 2 - 50, height - 40, 100, 25)
            .build());

        updateAllButtons();
    }

    /// Обновление состояния кнопок
    private void updateAllButtons() {
        for (BiomeFogEntry e : entries) {
            e.checkbox.setChecked(config.isBiomeGroupEnabled(e.group));
        }
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int gridWidth = COLUMNS * COL_SPACING;
        int startX = (width - gridWidth) / 2;
        int startY = 50;

        for (int i = 0; i < entries.size(); i++) {
            BiomeFogEntry e = entries.get(i);

            int col = i % COLUMNS, row = i / COLUMNS;
            int x = startX + col * COL_SPACING;
            int y = startY + row * ROW_SPACING;

            if (y + ENTRY_HEIGHT > 0 && y < height - 40) {
                String displayName = beautifyName(e.group);

                int textWidth = textRenderer.getWidth(displayName);
                int textX = x + (ENTRY_WIDTH - textWidth) / 2;

                boolean hover = mouseX >= textX && mouseX <= textX + textWidth && mouseY >= y - 6 && mouseY <= y + 15;

                int color = hover ? ACCENT_COLOR : 0xFFFFFFFF;

                context.drawText(textRenderer, Text.literal(displayName), textX, y - 6, color, false);
            }
        }
    }

    private String beautifyName(String key) {
        return switch (key) {
            case "KrimsonForest" -> "Krimson Forest";
            case "NetherWastes" -> "Nether Wastes";
            case "BasaltDeltas" -> "Basalt Deltas";
            case "WarpedForest" -> "Warped Forest";
            case "SoulSand" -> "Soul Sand Valley";
            case "GrowthTaiga" -> "Growth Taiga";
            case "Mushrooms" -> "Mushroom Fields";
            case "Snow" -> "Snowy Biomes";
            case "Desert" -> "Desert";
            case "Savanna" -> "Savanna";
            case "Mesa" -> "Badlands";
            case "End" -> "The End";
            default -> key;
        };
    }

    private void save() {
        config.save();
        CustomFog.refreshBlockedStatus();
        CustomFog.reloadFromConfig();

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