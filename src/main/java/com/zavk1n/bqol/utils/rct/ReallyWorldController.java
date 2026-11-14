package com.zavk1n.bqol.utils.rct;

import com.zavk1n.bqol.features.BetterRCT;
import com.zavk1n.bqol.utils.chat.Chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class ReallyWorldController {
    private static ReallyWorldController instance;

    private static final String HUB_COMMAND = "hub";

    private static final int HUB_TIMEOUT_TICKS = 120;
    private static final int MENU_TIMEOUT_TICKS = 60;
    private static final int SUCCESS_DELAY_TICKS = 60;
    private static final int MENU_CHANGE_DELAY_TICKS = 2;
    private static final int MAX_ANARCHY = 88;

    private static final int MAX_MENU_ATTEMPTS = 3;

    private static final Pattern GRIEF_NUMBER = Pattern.compile("#\\s*(\\d+)");

    public int operationTimeoutTicks = 500;
    public String lastNumber;

    private State state = State.IDLE;

    private int stateTicks;
    private int operationTicks;
    private int finishDelayTicks;
    private int menuAttempts;
    private int switchingMenuAttempts;
    private int targetNumber = -1;

    private String previousMenuSnapshot;

    private enum State {
        IDLE,
        TRAVELLING_TO_HUB,
        OPENING_SERVER_MENU,
        WAITING_SERVER_MENU,
        FINDING_WORKBENCH,
        WAITING_WORKBENCH_MENU,
        FINDING_FIRST_PAGE,
        WAITING_FIRST_PAGE,
        FINDING_SECOND_PAGE,
        WAITING_SECOND_PAGE,
        FINDING_SERVER,
        WAITING_FINISH
    }

    public ReallyWorldController() {
        instance = this;
    }

    public static ReallyWorldController getInstance() {
        return instance;
    }

    public boolean isBusy() {
        return state != State.IDLE;
    }

    public void start(String number) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null || mc.getNetworkHandler() == null || isBusy()) {
            return;
        }

        if (number == null || number.isBlank()) {
            Chat.send(mc, Chat.invalidNumber());
            return;
        }

        final int value;

        try {
            value = Integer.parseInt(number.trim());
        } catch (NumberFormatException ignored) {
            Chat.send(mc, Chat.invalidNumber());
            return;
        }

        if (value < 1 || value > MAX_ANARCHY) {
            Chat.send(mc, value > MAX_ANARCHY ? Chat.unknownRoute() : Chat.invalidNumber());
            return;
        }

        targetNumber = value;
        previousMenuSnapshot = null;

        menuAttempts = 0;
        switchingMenuAttempts = 0;
        operationTicks = 0;
        finishDelayTicks = 0;

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
            fail(mc, "Истек таймаут операции RCT.");
            return;
        }

        stateTicks++;

        switch (state) {
            case TRAVELLING_TO_HUB -> waitForReallyWorldHub(mc);
            case OPENING_SERVER_MENU -> openServerMenu(mc);
            case WAITING_SERVER_MENU -> waitForServerMenu(mc);
            case FINDING_WORKBENCH -> findAndClickWorkbench(mc);
            case WAITING_WORKBENCH_MENU -> waitForWorkbenchMenu(mc);
            case FINDING_FIRST_PAGE -> findAndClickNextPage(mc);
            case WAITING_FIRST_PAGE -> waitForFirstPage(mc);
            case FINDING_SECOND_PAGE -> findAndClickSecondPage(mc);
            case WAITING_SECOND_PAGE -> waitForSecondPage(mc);
            case FINDING_SERVER -> findAndClickServer(mc);
            case WAITING_FINISH -> waitForFinish(mc);
            case IDLE -> {}
        }
    }

    private void waitForReallyWorldHub(MinecraftClient mc) {
        if (hasReallyWorldHubCompass(mc)) {
            switchTo(State.OPENING_SERVER_MENU);
        } else if (stateTicks >= HUB_TIMEOUT_TICKS) {
            fail(mc, "После /hub не удалось обнаружить компас выбора сервера.");
        }
    }

    private static boolean hasReallyWorldHubCompass(MinecraftClient mc) {
        if (mc.player == null) {
            return false;
        }

        return isServerCompass(mc.player.getMainHandStack())
            || isServerCompass(mc.player.getOffHandStack())
            || findCompassInventorySlot(mc) != -1;
    }

    private static boolean isServerCompass(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.COMPASS && BetterRCT.matches(stack.getName().getString(), "ВЫБОР СЕРВЕРА (ПКМ)");
    }

    private void openServerMenu(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) {
            return;
        }

        if (isServerCompass(mc.player.getOffHandStack())) {
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            switchTo(State.WAITING_SERVER_MENU);
            return;
        }

        int inventorySlot = findCompassInventorySlot(mc);

        if (inventorySlot == -1) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Компас выбора сервера не найден в инвентаре.");
            }
            return;
        }

        if (inventorySlot >= 9) {
            int hotbarSlot = findEmptyHotbarSlot(mc);

            if (hotbarSlot == -1) {
                hotbarSlot = mc.player.getInventory().getSelectedSlot();
            }

            if (!moveToHotbar(mc, inventorySlot, hotbarSlot)) {
                return;
            }

            mc.player.getInventory().setSelectedSlot(hotbarSlot);
        } else {
            mc.player.getInventory().setSelectedSlot(inventorySlot);
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        switchTo(State.WAITING_SERVER_MENU);
    }

    private static int findCompassInventorySlot(MinecraftClient mc) {
        if (mc.player == null) {
            return -1;
        }

        var inventory = mc.player.getInventory();

        for (int slot = 0; slot < 36; slot++) {
            if (isServerCompass(inventory.getStack(slot))) {
                return slot;
            }
        }

        return -1;
    }

    private void waitForServerMenu(MinecraftClient mc) {
        if (BetterRCT.openMenu(mc) != null) {
            switchingMenuAttempts = 0;
            menuAttempts = 0;
            switchTo(State.FINDING_WORKBENCH);
            return;
        }

        if (stateTicks >= MENU_TIMEOUT_TICKS) {
            retryServerMenu(mc);
        }
    }

    private void retryServerMenu(MinecraftClient mc) {
        if (switchingMenuAttempts < MAX_MENU_ATTEMPTS) {
            switchingMenuAttempts++;
            switchTo(State.OPENING_SERVER_MENU);
            return;
        }

        fail(mc, "Не удалось открыть меню выбора серверов.");
    }

    private void findAndClickWorkbench(MinecraftClient mc) {
        HandledScreen<?> screen = BetterRCT.openMenu(mc);

        if (screen == null) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                retryWorkbench(mc, "Меню выбора серверов было закрыто.");
            }
            return;
        }

        ScreenHandler menu = screen.getScreenHandler();
        OptionalInt slot = workbenchSlot(menu);

        if (slot.isEmpty()) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                retryWorkbench(mc, "В меню не найден режим «Гриф выживание».");
            }
            return;
        }

        previousMenuSnapshot = BetterRCT.snapshot(screen);

        if (clickSlot(mc, menu, slot.getAsInt())) {
            menuAttempts++;
            switchTo(State.WAITING_WORKBENCH_MENU);
        }
    }

    private void waitForWorkbenchMenu(MinecraftClient mc) {
        if (stateTicks < MENU_CHANGE_DELAY_TICKS) {
            return;
        }

        if (!hasMenuChanged(mc)) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                retryWorkbench(mc, "Меню грифа не открылось или не обновилось.");
            }
            return;
        }

        menuAttempts = 0;
        switchTo(targetNumber <= 32 ? State.FINDING_SERVER : State.FINDING_FIRST_PAGE);
    }

    private void retryWorkbench(MinecraftClient mc, String errorMessage) {
        if (menuAttempts < MAX_MENU_ATTEMPTS) {
            menuAttempts++;
            previousMenuSnapshot = null;
            switchTo(State.FINDING_WORKBENCH);
            return;
        }

        fail(mc, errorMessage);
    }

    private void findAndClickNextPage(MinecraftClient mc) {
        HandledScreen<?> screen = BetterRCT.openMenu(mc);

        if (screen == null) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Меню серверов было закрыто.");
            }

            return;
        }

        ScreenHandler menu = screen.getScreenHandler();
        OptionalInt slot = nextPageSlot(menu);

        if (slot.isEmpty()) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Не найдена кнопка перехода на следующую страницу.");
            }

            return;
        }

        previousMenuSnapshot = BetterRCT.snapshot(screen);

        if (clickSlot(mc, menu, slot.getAsInt())) {
            switchTo(State.WAITING_FIRST_PAGE);
        }
    }

    private void waitForFirstPage(MinecraftClient mc) {
        if (stateTicks < MENU_CHANGE_DELAY_TICKS) {
            return;
        }

        if (!hasMenuChanged(mc)) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Первая страница серверов не обновилась.");
            }

            return;
        }

        switchTo(targetNumber >= 65 ? State.FINDING_SECOND_PAGE : State.FINDING_SERVER);
    }

    private void findAndClickSecondPage(MinecraftClient mc) {
        HandledScreen<?> screen = BetterRCT.openMenu(mc);

        if (screen == null) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Меню серверов было закрыто.");
            }

            return;
        }

        ScreenHandler menu = screen.getScreenHandler();
        OptionalInt slot = nextPageSlot(menu);

        if (slot.isEmpty()) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Не найдена кнопка второй страницы серверов.");
            }

            return;
        }

        previousMenuSnapshot = BetterRCT.snapshot(screen);

        if (clickSlot(mc, menu, slot.getAsInt())) {
            switchTo(State.WAITING_SECOND_PAGE);
        }
    }

    private void waitForSecondPage(MinecraftClient mc) {
        if (stateTicks < MENU_CHANGE_DELAY_TICKS) {
            return;
        }

        if (!hasMenuChanged(mc)) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Вторая страница серверов не обновилась.");
            }

            return;
        }

        switchTo(State.FINDING_SERVER);
    }

    private void findAndClickServer(MinecraftClient mc) {
        HandledScreen<?> screen = BetterRCT.openMenu(mc);

        if (screen == null) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Меню серверов было закрыто до выбора грифа.");
            }

            return;
        }

        ScreenHandler menu = screen.getScreenHandler();
        OptionalInt slot = findItemByNumber(menu, targetNumber);

        if (slot.isEmpty()) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Не удалось найти гриф #" + targetNumber + " в меню.");
            }

            return;
        }

        if (clickSlot(mc, menu, slot.getAsInt())) {
            switchTo(State.WAITING_FINISH);
        }
    }

    private static OptionalInt findItemByNumber(ScreenHandler menu, int expectedNumber) {
        int slotCount = BetterRCT.slotCount(menu);

        for (int i = 0; i < slotCount; i++) {
            ItemStack stack = menu.slots.get(i).getStack();

            if (stack.isEmpty() || stack.getItem() != Items.PLAYER_HEAD) {
                continue;
            }

            String name = BetterRCT.normalize(stack.getName().getString());

            if (!name.contains("гриф")) {
                continue;
            }

            Matcher matcher = GRIEF_NUMBER.matcher(name);

            while (matcher.find()) {
                try {
                    if (Integer.parseInt(matcher.group(1)) == expectedNumber) {
                        return OptionalInt.of(i);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return OptionalInt.empty();
    }

    public static OptionalInt workbenchSlot(ScreenHandler menu) {
        return BetterRCT.slotMatching(menu, stack -> stack.getItem() == Items.CRAFTING_TABLE && BetterRCT.matches(stack.getName().getString(), "ГРИФ ВЫЖИВАНИЕ (1.21.11)"));
    }

    public static OptionalInt nextPageSlot(ScreenHandler menu) {
        return BetterRCT.slotMatching(menu, stack -> stack.getItem() == Items.ARROW && BetterRCT.matches(stack.getName().getString(), "Следующая страница"));
    }

    private boolean hasMenuChanged(MinecraftClient mc) {
        HandledScreen<?> screen = BetterRCT.openMenu(mc);

        return screen != null
            && previousMenuSnapshot != null
            && !previousMenuSnapshot.equals(BetterRCT.snapshot(screen));
    }

    public boolean scoreboardContains(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world == null || text == null || text.isBlank()) {
            return false;
        }

        String wanted = BetterRCT.normalize(text);

        boolean griefSearch = wanted.startsWith("гриф");

        String expectedNumber = null;

        if (griefSearch) {
            Matcher matcher = GRIEF_NUMBER.matcher(wanted);

            if (!matcher.find()) {
                return false;
            }

            expectedNumber = matcher.group(1);
        }

        for (String line : BetterRCT.sidebarLines(mc)) {
            String normalized = BetterRCT.normalize(line);

            if (normalized.isEmpty()) {
                continue;
            }

            if (!griefSearch) {
                if (BetterRCT.matches(normalized, wanted)) {
                    return true;
                }
                continue;
            }

            if (!normalized.contains("гриф")) {
                continue;
            }

            Matcher matcher = GRIEF_NUMBER.matcher(normalized);

            while (matcher.find()) {
                if (expectedNumber.equals(matcher.group(1))) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isOnReallyWorldServer(String number) {
        if (number == null || number.isBlank()) {
            return false;
        }

        return scoreboardContains("ГРИФ #" + number);
    }

    private void waitForFinish(MinecraftClient mc) {
        String number = Integer.toString(targetNumber);

        if (!BetterRCT.getInstance().isOnServer(number)) {
            finishDelayTicks = 0;
            return;
        }

        if (++finishDelayTicks < SUCCESS_DELAY_TICKS) {
            return;
        }

        lastNumber = number;

        int messageColor = BetterRCT.getInstance().MESSAGE_COLOR;

        Text message = Text.literal("Я успешно зашел на сервер - ")
            .styled(style -> style.withColor(messageColor))
            .append(
                Text.literal("Гриф #" + number)
                    .styled(style -> style
                        .withColor(messageColor)
                        .withBold(true))
            );

        closeMenu(mc);
        Chat.info(mc, message);
        reset();
    }

    public void handleIncomingMessageInternal(String message) {
        if (message != null
            && isBusy()
            && message.contains("Не удалось подключиться к серверу")) {
            stop();
        }
    }

    public static void handleIncomingMessage(String message) {
        if (instance != null) {
            instance.handleIncomingMessageInternal(message);
        }
    }

    public void stop() {
        if (!isBusy()) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        closeMenu(mc);

        Chat.info(mc, Text.literal("Скрипт RCT был остановлен.")
            .styled(style -> style
                .withColor(BetterRCT.getInstance().MESSAGE_COLOR)
                .withBold(true))
        );

        reset();
    }

    private static boolean clickSlot(MinecraftClient mc, ScreenHandler menu, int slot) {
        if (mc.interactionManager == null
            || mc.player == null
            || slot < 0
            || slot >= menu.slots.size()) {
            return false;
        }

        mc.interactionManager.clickSlot(menu.syncId, slot, 0, SlotActionType.PICKUP, mc.player);

        return true;
    }

    private static boolean moveToHotbar(MinecraftClient mc, int inventorySlot, int hotbarSlot) {
        if (mc.player == null
            || mc.interactionManager == null
            || inventorySlot < 9
            || inventorySlot > 35
            || hotbarSlot < 0
            || hotbarSlot > 8) {
            return false;
        }

        ScreenHandler handler = mc.player.currentScreenHandler;

        mc.interactionManager.clickSlot(handler.syncId, inventorySlot, hotbarSlot, SlotActionType.SWAP, mc.player);

        return true;
    }

    private static int findEmptyHotbarSlot(MinecraftClient mc) {
        if (mc.player == null) {
            return -1;
        }

        var inventory = mc.player.getInventory();

        for (int slot = 0; slot < 9; slot++) {
            if (inventory.getStack(slot).isEmpty()) {
                return slot;
            }
        }

        return -1;
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

    private void fail(MinecraftClient mc, String message) {
        closeMenu(mc);

        Chat.info(mc, Text.literal(message)
            .styled(style -> style
                .withColor(BetterRCT.getInstance().MESSAGE_COLOR)
                .withBold(true))
        );

        reset();
    }

    private void reset() {
        targetNumber = -1;
        previousMenuSnapshot = null;

        menuAttempts = 0;
        switchingMenuAttempts = 0;

        stateTicks = 0;
        operationTicks = 0;
        finishDelayTicks = 0;

        state = State.IDLE;
    }
}