package com.zavk1n.bqol.client.screen.featurescreen.speheres;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorCheckbox;
import com.zavk1n.bqol.features.spheres.BetterSpheres;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ReallyWorldConfigScreen extends MainConfigScreen {
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

    public ReallyWorldConfigScreen(Screen parent) {
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

    private void rebuildUI() {
        clearChildren();

        sphereEntries.clear();

        createSphereGrid();
        createFooter();

        updateSphereElements();
    }

    private void createSphereGrid() {
        int gridWidth = COLUMNS * COL_SPACING;
        int startX = (width - gridWidth) / 2;

        List<SphereEntry> spheres = List.of(
            new SphereEntry("Шар Воздуха", config::isSphereAirRW, config::setSphereAirRW),
            new SphereEntry("Шар Света", config::isSphereShineRW, config::setSphereShineRW),
            new SphereEntry("Шар Огня", config::isSphereFireRW, config::setSphereFireRW),
            new SphereEntry("Шар Воды", config::isSphereWaterRW, config::setSphereWaterRW),
            new SphereEntry("Шар Хаоса", config::isSphereChaosRW, config::setSphereChaosRW),
            new SphereEntry("Шар Земли", config::isSphereGroundRW, config::setSphereGroundRW),
            new SphereEntry("Шар CocaCola", config::isSphereCocaColaRW, config::setSphereCocaColaRW),
            new SphereEntry("Шар БОГА", config::isSphereGODRW, config::setSphereGODRW),
            new SphereEntry("Шар Pepsi", config::isSpherePepsiRW, config::setSpherePepsiRW),
            new SphereEntry("Шар RedBull", config::isSphereRedBullRW, config::setSphereRedBullRW),
            new SphereEntry("Шар Sprite", config::isSphereSpriteRW, config::setSphereSpriteRW),
            new SphereEntry("Шар Fanta", config::isSphereFantaRW, config::setSphereFantaRW),
            new SphereEntry("Шар Посейдона", config::isSpherePoseidonRW, config::setSpherePoseidonRW),
            new SphereEntry("Шар Аида", config::isSphereHadesRW, config::setSphereHadesRW),
            new SphereEntry("Шар броненосца", config::isSphereArmadilloRW, config::setSphereArmadilloRW),
            new SphereEntry("Шар D.HELPER", config::isSphereDHELPERRW, config::setSphereDHELPERRW),
            new SphereEntry("Шар Порядка", config::isSphereDisciplineRW, config::setSphereDisciplineRW),
            new SphereEntry("Шар Тьмы", config::isSphereDarkRW, config::setSphereDarkRW),
            new SphereEntry("Шар Крови", config::isSphereBloodRW, config::setSphereBloodRW),
            new SphereEntry("Шар Молнии", config::isSphereLightningRW, config::setSphereLightningRW),
            new SphereEntry("Шар Льда", config::isSphereIceRW, config::setSphereIceRW),
            new SphereEntry("Шар BUNNY", config::isSphereBUNNYRW, config::setSphereBUNNYRW),
            new SphereEntry("Шар Диабло", config::isSphereDiabloRW, config::setSphereDiabloRW),
            new SphereEntry("Шар Вардена", config::isSphereWardenRW, config::setSphereWardenRW),
            new SphereEntry("Шар Модера", config::isSphereModerRW, config::setSphereModerRW),
            new SphereEntry("Шар Читера", config::isSphereCheaterRW, config::setSphereCheaterRW),
            new SphereEntry("Шар Админа", config::isSphereAdminRW, config::setSphereAdminRW),
            new SphereEntry("Шар Учителя", config::isSphereTeacherRW, config::setSphereTeacherRW),

            new SphereEntry("Голова Бетмена", config::isHeadBatmanRW, config::setHeadBatmanRW),
            new SphereEntry("Голова Вампира", config::isHeadVampireRW, config::setHeadVampireRW),
            new SphereEntry("Голова Джека", config::isHeadJackRW, config::setHeadJackRW),
            new SphereEntry("Голова Гринча", config::isHeadGrinchRW, config::setHeadGrinchRW),
            new SphereEntry("Голова Гидры", config::isHeadHydraRW, config::setHeadHydraRW),
            new SphereEntry("Голова Железного человека", config::isHeadIronManRW, config::setHeadIronManRW),
            new SphereEntry("Голова Кобры", config::isHeadCobraRW, config::setHeadCobraRW),
            new SphereEntry("Голова Кролика", config::isHeadBunnyRW, config::setHeadBunnyRW),
            new SphereEntry("Голова Пегаса", config::isHeadPegasusRW, config::setHeadPegasusRW),
            new SphereEntry("Голова Пенгвин", config::isHeadPenguinRW, config::setHeadPenguinRW),
            new SphereEntry("Голова Пряника", config::isHeadGingerbreadRW, config::setHeadGingerbreadRW),
            new SphereEntry("Голова Рудольфа", config::isHeadRudolphRW, config::setHeadRudolphRW),
            new SphereEntry("Голова Санты", config::isHeadSantaRW, config::setHeadSantaRW),
            new SphereEntry("Голова ХАЛКА", config::isHeadHulkRW, config::setHeadHulkRW),
            new SphereEntry("Голова Тора", config::isHeadThorRW, config::setHeadThorRW),
            new SphereEntry("Голова Щелкунчика", config::isHeadNutcrackerRW, config::setHeadNutcrackerRW),
            new SphereEntry("Голова Эльфа", config::isHeadElfRW, config::setHeadElfRW),
            new SphereEntry("Тигринная голова", config::isHeadTigerRW, config::setHeadTigerRW),
            new SphereEntry("Пасхальное яйцо", config::isEasterEggRW, config::setEasterEggRW)
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