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
        final String title, description;
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;
        ButtonWidget button;
        SoundsModeEntry(String title, String desc, BooleanSupplier getter, Consumer<Boolean> setter) {
            this.title = title; this.description = desc;
            this.getter = getter; this.setter = setter;
        }
    }

    /// Конструктор
    public SoundsConfigScreen(Screen parent) {
        super(Text.literal("Better Sounds Settings"), parent);
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
            config::isBetterSoundsFarm,
            config::setBetterSoundsFarm
        );

        addDrawableChild(farmButton);

        mobButton = createButton(
            col2X + columnWidth - BUTTON_WIDTH,
            mobY,
            config::isBetterSoundsMob,
            config::setBetterSoundsMob
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

        column1.add(new SoundsModeEntry("Explosion Mode", "Disables explosions sounds.",
                config::isBetterSoundsExplosion, config::setBetterSoundsExplosion));
        column1.add(new SoundsModeEntry("Ender-Dragon Mode", "Disables dragon sounds.",
                config::isBetterSoundsEnderDragon, config::setBetterSoundsEnderDragon));
        column1.add(new SoundsModeEntry("Villager Mode", "Disables villagers sounds.",
                config::isBetterSoundsVillager, config::setBetterSoundsVillager));
        column1.add(new SoundsModeEntry("Thunder Mode", "Disables lightnings & thunder sounds.",
                config::isBetterSoundsThunder, config::setBetterSoundsThunder));
        column1.add(new SoundsModeEntry("Mood Mode", "Disables cave/nether/end ambience.",
                config::isBetterSoundsMood, config::setBetterSoundsMood));
        column1.add(new SoundsModeEntry("Ice Mode", "Disables ice/glass/frost sounds.",
                config::isBetterSoundsIce, config::setBetterSoundsIce));
        column1.add(new SoundsModeEntry("Pistons Mode", "Disables pistons sounds.",
                config::isBetterSoundsPiston, config::setBetterSoundsPiston));

        column2.add(new SoundsModeEntry("Fire Mode", "Disables fire sounds.",
                config::isBetterSoundsFire, config::setBetterSoundsFire));
        column2.add(new SoundsModeEntry("Eat Mode", "Disables eating sounds.",
                config::isBetterSoundsEat, config::setBetterSoundsEat));
        column2.add(new SoundsModeEntry("Drink Mode", "Disables drinking sounds.",
                config::isBetterSoundsDrink, config::setBetterSoundsDrink));
        column2.add(new SoundsModeEntry("Hit Mode", "Disables attack & hurt sounds.",
                config::isBetterSoundsHit, config::setBetterSoundsHit));
        column2.add(new SoundsModeEntry("Storage Mode", "Disables storage sounds.",
                config::isBetterSoundsStorage, config::setBetterSoundsStorage));
        column2.add(new SoundsModeEntry("Grass Mode", "Disables grass & leaves sounds.",
                config::isBetterSoundsGrass, config::setBetterSoundsGrass));
        column2.add(new SoundsModeEntry("Totem Mode", "Disables totem sounds.",
                config::isBetterSoundsTotem, config::setBetterSoundsTotem));

        column3.add(new SoundsModeEntry("Anvil Mode", "Disables anvil sounds.",
                config::isBetterSoundsAnvil, config::setBetterSoundsAnvil));
        column3.add(new SoundsModeEntry("XP Mode", "Disables XP bottles sounds.",
                config::isBetterSoundsXp, config::setBetterSoundsXp));
        column3.add(new SoundsModeEntry("Mining Mode", "Disables mining stone/ores sounds.",
                config::isBetterSoundsMining, config::setBetterSoundsMining));
        column3.add(new SoundsModeEntry("Wood Mode", "Disables wood sounds.",
                config::isBetterSoundsWood, config::setBetterSoundsWood));
        column3.add(new SoundsModeEntry("Lava & Water Entry", "Disables lava & water entry sounds.",
                config::isBetterSoundsLavaWater, config::setBetterSoundsLavaWater));
        column3.add(new SoundsModeEntry("Ender-Portal Mode", "Disables end portal & eyes sounds.",
                config::isBetterSoundsEnderPortal, config::setBetterSoundsEnderPortal));
        column3.add(new SoundsModeEntry("Achievements Mode", "Disables achievement sounds.",
                config::isBetterSoundsAchievements, config::setBetterSoundsAchievements));
    }

    private void createColumn(List<SoundsModeEntry> column, int columnX) {
        int row = 0;

        for (SoundsModeEntry entry : column) {
            int y = 60 + row * SPACING;

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
        ButtonWidget btn = ButtonWidget.builder(
                Text.literal(""),
                button -> {
                    boolean newState = !getter.getAsBoolean();
                    setter.accept(newState);
                    changed = true;
                    updateButton(button, newState);
                })
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
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
            updateButton(farmButton, config.isBetterSoundsFarm());
        }

        if (mobButton != null) {
            updateButton(mobButton, config.isBetterSoundsMob());
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
            int y = 60 + row * SPACING;

            renderLabel(context, columnX, y, mouseX, mouseY, entry.title, entry.description);

            row++;
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