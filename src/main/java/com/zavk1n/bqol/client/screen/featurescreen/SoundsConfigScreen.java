package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterSounds;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SoundsConfigScreen extends MainConfigScreen {
    private List<SoundsModeEntry> column1 = new ArrayList<>(), column2 = new ArrayList<>(), column3 = new ArrayList<>();

    /// Виджеты
    private ButtonWidget farmButton, mobButton;

    private int columnWidth, col1X, col2X, col3X;
    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45, COLUMN_GAP = 40, MARGIN_LEFT = 20, MARGIN_RIGHT = 20, BOTTOM_OFFSET = 40;

    private boolean changed = false;

    private static class SoundsModeEntry {
        final String title, description, featureId;
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;
        ButtonWidget button;
        SoundsModeEntry(String title, String desc, String id, BooleanSupplier getter, Consumer<Boolean> setter) {
            this.title = title; this.description = desc; this.featureId = id;
            this.getter = getter; this.setter = setter;
        }
    }

    /// Конструктор
    public SoundsConfigScreen(Screen parent) {
        super(Text.literal("BetterSounds Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_sounds")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        columnWidth = (width - MARGIN_LEFT - MARGIN_RIGHT - 2 * COLUMN_GAP) / 3;

        col1X = MARGIN_LEFT;
        col2X = col1X + columnWidth + COLUMN_GAP;
        col3X = col2X + columnWidth + COLUMN_GAP;

        fillColumns();

        createColumn(column1, col1X);
        createColumn(column2, col2X);
        createColumn(column3, col3X);

        int farmY = height - BOTTOM_OFFSET - BUTTON_HEIGHT - SPACING * 2;
        int mobY = height - BOTTOM_OFFSET - BUTTON_HEIGHT - SPACING;

        farmButton = createButton(
            col2X + columnWidth - BUTTON_WIDTH,
            farmY,
            config::isFarmMode,
            config::setFarmMode
        );

        addDrawableChild(farmButton);

        mobButton = createButton(
            col2X + columnWidth - BUTTON_WIDTH,
            mobY,
            config::isMobMode,
            config::setMobMode
        );

        addDrawableChild(mobButton);

        addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Save & Back"),
                    button -> close()
                )
                .dimensions(
                    width / 2 - 50,
                    height - BOTTOM_OFFSET,
                    100,
                    BUTTON_HEIGHT
                )
                .build()
        );

        updateAllButtons();
    }

    /// Работа с колоннами
    private void fillColumns() {
        column1.clear();
        column2.clear();
        column3.clear();

        column1.add(new SoundsModeEntry("Explosion Mode", "Disables explosions sounds.", "better_sounds_explosion",
                config::isExplosionMode, config::setExplosionMode));
        column1.add(new SoundsModeEntry("Ender-Dragon Mode", "Disables dragon sounds.", "better_sounds_dragon",
                config::isEnderDragonMode, config::setEnderDragonMode));
        column1.add(new SoundsModeEntry("Villager Mode", "Disables villagers sounds.", "better_sounds_villager",
                config::isVillagerMode, config::setVillagerMode));
        column1.add(new SoundsModeEntry("Thunder Mode", "Disables lightnings & thunder sounds.", "better_sounds_thunder",
                config::isThunderMode, config::setThunderMode));
        column1.add(new SoundsModeEntry("Mood Mode", "Disables cave/nether/end ambience.", "better_sounds_mood",
                config::isMoodMode, config::setMoodMode));
        column1.add(new SoundsModeEntry("Ice Mode", "Disables ice/glass/frost sounds.", "better_sounds_ice",
                config::isIceMode, config::setIceMode));
        column1.add(new SoundsModeEntry("Pistons Mode", "Disables pistons sounds.", "better_sounds_piston",
                config::isPistonMode, config::setPistonMode));

        column2.add(new SoundsModeEntry("Fire Mode", "Disables fire sounds.", "better_sounds_fire",
                config::isFireMode, config::setFireMode));
        column2.add(new SoundsModeEntry("Eat Mode", "Disables eating sounds.", "better_sounds_eat",
                config::isEatMode, config::setEatMode));
        column2.add(new SoundsModeEntry("Drink Mode", "Disables drinking sounds.", "better_sounds_drink",
                config::isDrinkMode, config::setDrinkMode));
        column2.add(new SoundsModeEntry("Hits Mode", "Disables attack & hurt sounds.", "better_sounds_hits",
                config::isHitsMode, config::setHitsMode));
        column2.add(new SoundsModeEntry("Storage Mode", "Disables storage sounds.", "better_sounds_storage",
                config::isStorageMode, config::setStorageMode));
        column2.add(new SoundsModeEntry("Grass Mode", "Disables grass & leaves sounds.", "better_sounds_grass",
                config::isGrassMode, config::setGrassMode));
        column2.add(new SoundsModeEntry("Totem Mode", "Disables totem sounds.", "better_sounds_totem",
                config::isTotemMode, config::setTotemMode));

        column3.add(new SoundsModeEntry("Anvil Mode", "Disables anvil sounds.", "better_sounds_anvil",
                config::isAnvilMode, config::setAnvilMode));
        column3.add(new SoundsModeEntry("XP Mode", "Disables XP bottles sounds.", "better_sounds_xp",
                config::isXpMode, config::setXpMode));
        column3.add(new SoundsModeEntry("Mining Mode", "Disables mining stone/ores sounds.", "better_sounds_mining",
                config::isMiningMode, config::setMiningMode));
        column3.add(new SoundsModeEntry("Wood Mode", "Disables wood sounds.", "better_sounds_wood",
                config::isWoodMode, config::setWoodMode));
        column3.add(new SoundsModeEntry("Lava/Water Entry", "Disables lava & water entry sounds.", "better_sounds_lavawater",
                config::isLavaWaterMode, config::setLavaWaterMode));
        column3.add(new SoundsModeEntry("Ender-Portal Mode", "Disables end portal & eyes sounds.", "better_sounds_enderportal",
                config::isEnderPortalMode, config::setEnderPortalMode));
        column3.add(new SoundsModeEntry("Achievements Mode", "Disables achievement sounds.", "better_sounds_achievements",
                config::isAchievementsMode, config::setAchievementsMode));
    }

    private void createColumn(List<SoundsModeEntry> column, int columnX) {
        int row = 0;

        for (SoundsModeEntry entry : column) {
            if (LiteApiManager.isFeatureBlocked(entry.featureId)) {
                continue;
            }

            int y = 70 + row * SPACING;

            entry.button = createButton(
                columnX + columnWidth - BUTTON_WIDTH,
                y,
                entry.getter,
                entry.setter
            );

            addDrawableChild(entry.button);

            row++;
        }
    }

    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget btn = ButtonWidget.builder(Text.literal(""), button -> {
                    boolean newState = !getter.getAsBoolean();
                    setter.accept(newState);
                    changed = true;
                    updateButton(button, newState);
                })
                .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

        updateButton(btn, getter.getAsBoolean());

        return btn;
    }

    /// Обновление состояния кнопок
    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
                .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    private void updateAllButtons() {
        for (SoundsModeEntry e : column1) {
            if (e.button != null) {
                updateButton(e.button, e.getter.getAsBoolean());
            }
        }

        for (SoundsModeEntry e : column2) {
            if (e.button != null) {
                updateButton(e.button, e.getter.getAsBoolean());
            }
        }

        for (SoundsModeEntry e : column3) {
            if (e.button != null) {
                updateButton(e.button, e.getter.getAsBoolean());
            }
        }

        if (farmButton != null) {
            updateButton(farmButton, config.isFarmMode());
        }

        if (mobButton != null) {
            updateButton(mobButton, config.isMobMode());
        }
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        drawColumnText(context, column1, col1X, mouseX, mouseY);
        drawColumnText(context, column2, col2X, mouseX, mouseY);
        drawColumnText(context, column3, col3X, mouseX, mouseY);

        if (farmButton != null) {
            int y = farmButton.getY();
            renderLabel(context, col2X, y, mouseX, mouseY, "Farm Mode", "Disables farming machines sounds.");
        }

        if (mobButton != null) {
            int y = mobButton.getY();
            renderLabel(context, col2X, y, mouseX, mouseY, "Mob Mode", "Disables all mob sounds.");
        }
    }

    private void drawColumnText(DrawContext context, List<SoundsModeEntry> column, int columnX, int mouseX, int mouseY) {
        int row = 0;

        for (SoundsModeEntry entry : column) {
            if (LiteApiManager.isFeatureBlocked(entry.featureId)) {
                continue;
            }

            int y = 70 + row * SPACING;

            renderLabel(
                context,
                columnX,
                y,
                mouseX,
                mouseY,
                entry.title,
                entry.description
            );

            row++;
        }
    }

    private void renderLabel(DrawContext context, int x, int y, int mouseX, int mouseY, String title, String desc) {
        int color = (mouseX >= x && mouseX <= x + textRenderer.getWidth(title) && mouseY >= y - 5 && mouseY <= y + 20)
                ? ACCENT_COLOR : 0xFFFFFFFF;
        context.drawText(textRenderer, Text.literal(title), x, y, color, false);
        context.drawText(textRenderer, Text.literal(desc), x, y + 12, 0xFF888888, false);
    }

    /// Сохранение и закрытие
    private void save() {
        config.save();
        BetterSounds.refreshBlockedStatus();
        BetterSounds.reloadFromConfig();

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