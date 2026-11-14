package com.zavk1n.bqol.utils.rct;

import com.zavk1n.bqol.features.BetterRCT;
import com.zavk1n.bqol.utils.chat.Chat;
import com.zavk1n.bqol.utils.liteapi.restapi.HWServers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.OptionalInt;

@Environment(EnvType.CLIENT)
public final class HolyWorldController {

    private static HolyWorldController instance;

    private static final String HUB_COMMAND = "hub";
    private static final String MENU_COMMAND = "menu";

    private static final int HUB_TIMEOUT_TICKS = 50;
    private static final int SUCCESS_DELAY_TICKS = 30;

    private static final String HUB_COMPASS_NAME = "Выбор режима (ПКМ)";
    private static final String LITE_ANARCHY = "лайт";
    private static final String ANARCHY = "анархия";
    private static final String VERSION = "1.16";

    public int operationTimeoutTicks = 500;
    public String lastNumber;

    private State state = State.IDLE;
    private Step step = Step.FIND_LITE_ANARCHY;

    private int stateTicks;
    private int operationTicks;
    private int finishDelayTicks;

    private int targetServerNumber;
    private String targetNumber;
    private LiteMode targetMode;
    private String snapshotBeforeClick;

    private enum State {
        IDLE,
        TRAVELLING_TO_HUB,
        WALKING_MENU,
        AWAITING_SCREEN_CHANGE,
        WAITING_FINISH
    }

    private enum Step {
        FIND_LITE_ANARCHY,
        FIND_CATEGORY,
        FIND_SERVER
    }

    public enum LiteMode {
        SOLO("СолоЛайт", 1, 9, 1),
        DUO("ДуоЛайт", 10, 19, 2),
        TRIO("ТриоЛайт", 20, 29, 3),
        CLAN("КланЛайт", 30, 36, 16);

        public final String displayName;
        public final int min;
        public final int max;
        public final int armorStandCount;

        private final String normalizedName;

        LiteMode(String displayName, int min, int max, int armorStandCount) {
            this.displayName = displayName;
            this.min = min;
            this.max = max;
            this.armorStandCount = armorStandCount;
            this.normalizedName = normalizeModeName(displayName);
        }

        public static LiteMode fromName(String name) {
            if (name == null || name.isEmpty()) {
                return null;
            }

            String normalized = normalizeModeName(name);

            for (LiteMode mode : values()) {
                if (normalized.startsWith(mode.normalizedName + "#")) {
                    return mode;
                }
            }

            return null;
        }

        public static LiteMode fallbackForNumber(int number) {
            for (LiteMode mode : values()) {
                if (number >= mode.min && number <= mode.max) {
                    return mode;
                }
            }

            return null;
        }

        public static LiteMode forNumber(int number) {
            return HWServers.getInstance().getMode(number);
        }

        private static String normalizeModeName(String name) {
            return BetterRCT.normalize(name).replaceAll("\\s+", "");
        }
    }

    public HolyWorldController() {
        instance = this;
    }

    public static HolyWorldController getInstance() {
        return instance;
    }

    public boolean isBusy() {
        return state != State.IDLE;
    }

    public void start(String number) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null
            || mc.getNetworkHandler() == null
            || isBusy()) {
            return;
        }

        final int value;

        try {
            value = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            Chat.send(mc, Chat.invalidNumber());
            return;
        }

        HWServers servers = HWServers.getInstance();

        if (!servers.exists(value)) {
            Chat.send(mc, Chat.invalidNumber());
            return;
        }

        LiteMode mode = servers.getMode(value);

        if (mode == null) {
            Chat.send(mc, Chat.unknownRoute());
            return;
        }

        targetServerNumber = value;
        targetNumber = Integer.toString(value);
        targetMode = mode;

        stateTicks = 0;
        operationTicks = 0;
        finishDelayTicks = 0;

        snapshotBeforeClick = null;
        step = Step.FIND_LITE_ANARCHY;

        switchTo(State.TRAVELLING_TO_HUB);
        sendCommand(mc, HUB_COMMAND);
    }

    public void tick(MinecraftClient mc) {
        if (state == State.IDLE) {
            return;
        }

        if (mc.player == null || mc.getNetworkHandler() == null) {
            reset();
            return;
        }

        if (++operationTicks > operationTimeoutTicks) {
            fail(mc, "Истек таймаут операции.");
            return;
        }

        stateTicks++;

        switch (state) {
            case TRAVELLING_TO_HUB -> waitForHub(mc);
            case WALKING_MENU -> walkMenu(mc);
            case AWAITING_SCREEN_CHANGE -> awaitScreenChange(mc);
            case WAITING_FINISH -> waitForFinish(mc);
            case IDLE -> {
            }
        }
    }

    private void waitForHub(MinecraftClient mc) {
        if (hasHubCompass(mc)) {
            sendCommand(mc, MENU_COMMAND);
            switchTo(State.WALKING_MENU);
            return;
        }

        if (stateTicks >= HUB_TIMEOUT_TICKS) {
            fail(mc, "Произошел сбой и после /hub компас не появился.");
        }
    }

    private static boolean hasHubCompass(MinecraftClient mc) {
        if (mc.player == null) {
            return false;
        }

        var inventory = mc.player.getInventory();

        for (int i = 0, size = inventory.size(); i < size; i++) {
            if (isHubCompass(inventory.getStack(i))) {
                return true;
            }
        }

        return isHubCompass(mc.player.getOffHandStack());
    }

    private static boolean isHubCompass(ItemStack stack) {
        return !stack.isEmpty()
            && stack.isOf(Items.COMPASS)
            && BetterRCT.matches(stack.getName().getString(), HUB_COMPASS_NAME);
    }

    private void walkMenu(MinecraftClient mc) {
        HandledScreen<?> screen = BetterRCT.openMenu(mc);

        if (screen == null) {
            return;
        }

        ScreenHandler menu = screen.getScreenHandler();

        switch (step) {
            case FIND_LITE_ANARCHY -> findAndClickSlot(mc, screen, menu);
            case FIND_CATEGORY -> findAndClickCategory(mc, screen, menu);
            case FIND_SERVER -> findAndClickServer(mc, menu);
        }
    }

    private void findAndClickSlot(MinecraftClient mc, HandledScreen<?> screen, ScreenHandler menu) {
        OptionalInt slot = liteAnarchySlot(menu);

        if (slot.isEmpty()) {
            return;
        }

        step = Step.FIND_CATEGORY;
        clickAndAwaitChange(mc, screen, menu, slot.getAsInt());
    }

    private void findAndClickCategory(MinecraftClient mc, HandledScreen<?> screen, ScreenHandler menu) {
        if (targetMode == null) {
            return;
        }

        OptionalInt slot = armorStandSlot(menu, targetMode.armorStandCount);

        if (slot.isEmpty()) {
            return;
        }

        step = Step.FIND_SERVER;
        clickAndAwaitChange(mc, screen, menu, slot.getAsInt());
    }

    private void findAndClickServer(MinecraftClient mc, ScreenHandler menu) {
        if (targetNumber == null) {
            return;
        }

        OptionalInt slot = BetterRCT.playerStackSlot(menu, targetServerNumber);

        if (slot.isEmpty() || !clickSlot(mc, menu, slot.getAsInt())) {
            return;
        }

        switchTo(State.WAITING_FINISH);
    }

    private void clickAndAwaitChange(MinecraftClient mc, HandledScreen<?> screen, ScreenHandler menu, int slot) {
        snapshotBeforeClick = BetterRCT.snapshot(screen);

        if (!clickSlot(mc, menu, slot)) {
            snapshotBeforeClick = null;
            return;
        }

        switchTo(State.AWAITING_SCREEN_CHANGE);
    }

    private void awaitScreenChange(MinecraftClient mc) {
        HandledScreen<?> screen = BetterRCT.openMenu(mc);

        if (screen == null) {
            snapshotBeforeClick = null;
            switchTo(State.WALKING_MENU);
            return;
        }

        String currentSnapshot = BetterRCT.snapshot(screen);

        if (snapshotBeforeClick == null || !currentSnapshot.equals(snapshotBeforeClick)) {
            snapshotBeforeClick = null;
            switchTo(State.WALKING_MENU);
        }
    }

    private void waitForFinish(MinecraftClient mc) {
        BetterRCT betterRCT = BetterRCT.getInstance();

        if (betterRCT == null || !betterRCT.isOnServer(targetNumber)) {
            finishDelayTicks = 0;
            return;
        }

        if (++finishDelayTicks < SUCCESS_DELAY_TICKS) {
            return;
        }

        lastNumber = targetNumber;

        HWServers servers = HWServers.getInstance();
        String displayName = servers.getDisplayName(targetServerNumber);

        Text message = Text.literal("Я успешно зашел на сервер - ")
            .styled(style -> style.withColor(betterRCT.MESSAGE_COLOR))
            .append(Text.literal(displayName)
                .styled(style -> style
                    .withColor(betterRCT.MESSAGE_COLOR)
                    .withBold(true)));

        closeMenu(mc);
        Chat.info(mc, message);

        reset();
    }

    public void handleIncomingMessageInternal(String message) {
        if (message == null || !isBusy()) {
            return;
        }

        if (message.contains("Не удалось подключить вас к серверу")
            || message.contains("Не удалось подключиться к серверу")
            || message.contains("Вы уже подключаетесь к серверу")) {
            stop();
        }
    }

    public static void handleIncomingMessage(String message) {
        if (instance != null) {
            instance.handleIncomingMessageInternal(message);
        }
    }

    public void stop() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!isBusy()) {
            return;
        }

        closeMenu(mc);

        BetterRCT betterRCT = BetterRCT.getInstance();
        int color = betterRCT != null
            ? betterRCT.MESSAGE_COLOR
            : 0xFFC7C2FF;

        Chat.info(mc, Text.literal("Скрипт RCT был остановлен.")
            .styled(style -> style
                .withColor(color)
                .withBold(true)
            )
        );

        reset();
    }

    public static OptionalInt liteAnarchySlot(ScreenHandler menu) {
        return BetterRCT.slotMatching(menu, stack -> {
            String text = BetterRCT.normalize(BetterRCT.combinedInfo(stack));

            return text.contains(LITE_ANARCHY)
                && text.contains(ANARCHY)
                && text.contains(VERSION);
        });
    }

    public static OptionalInt armorStandSlot(ScreenHandler menu, int count) {
        return BetterRCT.slotMatching(menu, stack -> stack.isOf(Items.ARMOR_STAND) && stack.getCount() == count);
    }

    private static boolean clickSlot(MinecraftClient mc, ScreenHandler menu, int slot) {
        if (mc.interactionManager == null
            || mc.player == null
            || menu == null) {
            return false;
        }

        mc.interactionManager.clickSlot(menu.syncId, slot, 0, SlotActionType.PICKUP, mc.player);

        return true;
    }

    private static void closeMenu(MinecraftClient mc) {
        if (mc.player != null && BetterRCT.openMenu(mc) != null) {
            mc.player.closeHandledScreen();
            mc.setScreen(null);
        }
    }

    private static void sendCommand(MinecraftClient mc, String command) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatCommand(command);
        }
    }

    private void switchTo(State next) {
        state = next;
        stateTicks = 0;
    }

    private void fail(MinecraftClient mc, String text) {
        closeMenu(mc);

        BetterRCT betterRCT = BetterRCT.getInstance();

        int color = betterRCT != null ? betterRCT.MESSAGE_COLOR : 0xFFC7C2FF;

        Chat.info(mc, Text.literal(text)
            .styled(style -> style
                .withColor(color)
                .withBold(true)));

        reset();
    }

    private void reset() {
        targetNumber = null;
        targetServerNumber = 0;
        targetMode = null;
        snapshotBeforeClick = null;

        step = Step.FIND_LITE_ANARCHY;

        stateTicks = 0;
        operationTicks = 0;
        finishDelayTicks = 0;

        state = State.IDLE;
    }
}