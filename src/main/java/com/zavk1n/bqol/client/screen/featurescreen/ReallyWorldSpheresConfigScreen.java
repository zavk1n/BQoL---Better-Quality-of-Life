package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorCheckbox;
import com.zavk1n.bqol.features.BetterSpheres;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ReallyWorldSpheresConfigScreen extends MainConfigScreen {

    /// Записи сфер
    private final List<SphereToggleEntry> sphereEntries = new ArrayList<>();

    private static class SphereToggleEntry {

        final String label;
        final int labelX;
        final int baseY;
        final ColorCheckbox checkbox;

        SphereToggleEntry(
            String label,
            int labelX,
            int baseY,
            ColorCheckbox checkbox
        ) {
            this.label = label;
            this.labelX = labelX;
            this.baseY = baseY;
            this.checkbox = checkbox;
        }
    }

    private static class SphereEntry {

        final String displayName;
        final BooleanSupplier enabledGetter;
        final Consumer<Boolean> enabledSetter;

        SphereEntry(
            String displayName,
            BooleanSupplier enabledGetter,
            Consumer<Boolean> enabledSetter
        ) {
            this.displayName = displayName;
            this.enabledGetter = enabledGetter;
            this.enabledSetter = enabledSetter;
        }
    }

    private static final int COLUMNS = 3;
    private static final int COL_SPACING = 130;
    private static final int ROW_SPACING = 65;

    private static final int GRID_START_Y = 60;

    private static final int FOOTER_HEIGHT = 40;

    private boolean changed = false;

    /// Конструктор
    public ReallyWorldSpheresConfigScreen(Screen parent) {
        super(Text.literal("ReallyWorld Spheres Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_spheres_reallyworld")) {
            close();
            return;
        }

        super.init();

        resetScroll();

        rebuildUI();
    }

    /// Ядро
    private void rebuildUI() {
        clearChildren();

        sphereEntries.clear();

        createSphereGrid();
        createFooter();

        updateSphereElements();
    }

    /// Создание элементов
    private void createSphereGrid() {
        int gridWidth = COLUMNS * COL_SPACING;
        int startX = (width - gridWidth) / 2;

        List<SphereEntry> spheres = List.of(
            new SphereEntry("Sphere Air", config::isSphereAir, config::setSphereAir),
            new SphereEntry("Sphere Shine", config::isSphereShine, config::setSphereShine),
            new SphereEntry("Sphere Fire", config::isSphereFire, config::setSphereFire),
            new SphereEntry("Sphere Water", config::isSphereWater, config::setSphereWater),
            new SphereEntry("Sphere Chaos", config::isSphereChaos, config::setSphereChaos),
            new SphereEntry("Sphere Ground", config::isSphereGround, config::setSphereGround),
            new SphereEntry("Sphere CocaCola", config::isSphereCocaCola, config::setSphereCocaCola),
            new SphereEntry("Sphere of GOD", config::isSphereGOD, config::setSphereGOD),
            new SphereEntry("Sphere Pepsi", config::isSpherePepsi, config::setSpherePepsi),
            new SphereEntry("Sphere RedBull", config::isSphereRedBull, config::setSphereRedBull),
            new SphereEntry("Sphere Sprite", config::isSphereSprite, config::setSphereSprite),
            new SphereEntry("Sphere Fanta", config::isSphereFanta, config::setSphereFanta),
            new SphereEntry("Sphere Poseidon", config::isSpherePoseidon, config::setSpherePoseidon),
            new SphereEntry("Sphere Hades", config::isSphereHades, config::setSphereHades),
            new SphereEntry("Sphere Armadillo", config::isSphereArmadillo, config::setSphereArmadillo),
            new SphereEntry("Sphere D.HELPER", config::isSphereDHELPER, config::setSphereDHELPER),
            new SphereEntry("Sphere Discipline", config::isSphereDiscipline, config::setSphereDiscipline),
            new SphereEntry("Sphere BUNNY", config::isSphereBUNNY, config::setSphereBUNNY),
            new SphereEntry("Batman Head", config::isHeadBatman, config::setHeadBatman),
            new SphereEntry("Vampire Head", config::isHeadVampire, config::setHeadVampire),
            new SphereEntry("Jack's Head", config::isHeadJack, config::setHeadJack),
            new SphereEntry("Grinch Head", config::isHeadGrinch, config::setHeadGrinch),
            new SphereEntry("Hydra Head", config::isHeadHydra, config::setHeadHydra),
            new SphereEntry("IronMan Head", config::isHeadIronMan, config::setHeadIronMan),
            new SphereEntry("Cobra Head", config::isHeadCobra, config::setHeadCobra),
            new SphereEntry("Bunny Head", config::isHeadBunny, config::setHeadBunny),
            new SphereEntry("Pegasus Head", config::isHeadPegasus, config::setHeadPegasus),
            new SphereEntry("Penguin Head", config::isHeadPenguin, config::setHeadPenguin),
            new SphereEntry("Gingerbread Head", config::isHeadGingerbread, config::setHeadGingerbread),
            new SphereEntry("Rudolph's Head", config::isHeadRudolph, config::setHeadRudolph),
            new SphereEntry("Santa Head", config::isHeadSanta, config::setHeadSanta),
            new SphereEntry("HULK HEAD", config::isHeadHulk, config::setHeadHulk),
            new SphereEntry("Thor Head", config::isHeadThor, config::setHeadThor),
            new SphereEntry("Nutcracker Head", config::isHeadNutcracker, config::setHeadNutcracker),
            new SphereEntry("Elf Head", config::isHeadElf, config::setHeadElf),
            new SphereEntry("Easter Egg", config::isEasterEgg, config::setEasterEgg)
        );

        int col = 0;
        int row = 0;

        for (SphereEntry entry : spheres) {
            int x = startX + col * COL_SPACING;
            int baseY = GRID_START_Y + row * ROW_SPACING;

            int labelWidth = textRenderer.getWidth(entry.displayName);
            int labelX = x + (COL_SPACING - labelWidth) / 2;

            ColorCheckbox checkbox = new ColorCheckbox(
                    x + (COL_SPACING - 15) / 2,
                    baseY + 20,
                    Text.empty(),
                    entry.enabledGetter.getAsBoolean(),
                    button -> {
                        boolean enabled = !entry.enabledGetter.getAsBoolean();
                        entry.enabledSetter.accept(enabled);
                        ((ColorCheckbox) button).setChecked(enabled);
                        changed = true;
                        save();
                    }
                );

            checkbox.setColor(ACCENT_COLOR);

            sphereEntries.add(new SphereToggleEntry(entry.displayName, labelX, baseY, checkbox));

            addDrawableChild(checkbox);
            col++;

            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }

        int rows = (spheres.size() + COLUMNS - 1) / COLUMNS;
        int lastRowY = GRID_START_Y + (rows - 1) * ROW_SPACING;

        int contentBottom = lastRowY + 45;

        setMaxScroll(Math.max(0F, contentBottom - getContentBottom()));
    }

    private void createFooter() {
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
                .dimensions(width / 2 - 50, height - FOOTER_HEIGHT, 100, 25)
                .build()
        );
    }

    /// Обнолвление элементов
    @Override
    protected void updateScroll() {
        super.updateScroll();

        updateSphereElements();
    }

    private void updateSphereElements() {
        for (SphereToggleEntry entry : sphereEntries) {
            int y = entry.baseY - (int) scrollOffset;

            entry.checkbox.setY(y + 20);

            int elementTop = y;
            int elementBottom = y + 40;

            boolean inside = elementBottom > getContentTop() && elementTop < getContentBottom();

            entry.checkbox.visible = inside;
            entry.checkbox.active = inside;
        }
    }

    private void renderSphereLabels(DrawContext context, int mouseX, int mouseY) {
        for (SphereToggleEntry entry : sphereEntries) {
            int y = entry.baseY - (int) scrollOffset;

            int titleBottom = y + textRenderer.fontHeight;

            if (titleBottom < getContentTop() || y > getContentBottom()) {
                continue;
            }

            int titleWidth = textRenderer.getWidth(entry.label);

            boolean hovered =
                mouseX >= entry.labelX &&
                    mouseX <= entry.labelX + titleWidth &&
                    mouseY >= y &&
                    mouseY <= y + textRenderer.fontHeight;

            int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

            context.drawTextWithShadow(textRenderer, Text.literal(entry.label), entry.labelX, y, color);
        }
    }

    /// Рендер
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (SphereToggleEntry entry : sphereEntries) {
            if (entry.checkbox != null) {
                entry.checkbox.visible = false;
            }
        }

        super.render(context, mouseX, mouseY, delta);

        updateSphereElements();
        context.enableScissor(0, getContentTop(), width, getContentBottom());

        renderSphereLabels(context, mouseX, mouseY);

        for (SphereToggleEntry entry : sphereEntries) {
            if (entry.checkbox != null && entry.checkbox.visible) {
                entry.checkbox.render(context, mouseX, mouseY, delta);
            }
        }

        context.disableScissor();
    }

    @Override
    protected int getScrollbarX() {
        int gridWidth = COLUMNS * COL_SPACING;
        int startX = (width - gridWidth) / 2;
        int gridRight = startX + gridWidth;

        return gridRight + 10;
    }

    /// Сохранение
    private void save() {
        config.save();

        BetterSpheres.refreshBlockedStatus();

        changed = false;
    }

    /// Закрытие
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