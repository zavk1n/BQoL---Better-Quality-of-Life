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

    private static final String HUB_COMMAND = "hub";
    private static final int HUB_TIMEOUT_TICKS = 50;
    private static final int MENU_TIMEOUT_TICKS = 50;
    private static final int SUCCESS_DELAY_TICKS = 60;
    private static final int MENU_CHANGE_DELAY_TICKS = 2;
    private static final int FINISH_TIMEOUT_TICKS = 200;
    private static final int MAX_ANARCHY = 32;

    private static final Pattern GRIEF_NUMBER = Pattern.compile("#\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    public int operationTimeoutTicks = 500;

    private State state = State.IDLE;

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

    private int stateTicks;
    private int operationTicks;
    private int finishDelayTicks;

    private String targetNumber;
    private String previousMenuSnapshot;

    public String lastNumber;

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
            value = Integer.parseInt(number.trim());
        } catch (NumberFormatException e) {
            Chat.send(mc, Chat.invalidNumber());
            return;
        }

        if (value < 1 || value > MAX_ANARCHY) {
            Chat.send(mc, value > MAX_ANARCHY ? Chat.unknownRoute() : Chat.invalidNumber());
            return;
        }

        targetNumber = Integer.toString(value);

        stateTicks = 0;
        operationTicks = 0;
        finishDelayTicks = 0;
        previousMenuSnapshot = null;

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

        ++stateTicks;

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
            return;
        }

        if (stateTicks >= HUB_TIMEOUT_TICKS) {
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

        if (inventorySlot < 9) {
            mc.player.getInventory().selectedSlot = inventorySlot;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

            switchTo(State.WAITING_SERVER_MENU);
            return;
        }

        int hotbarSlot = findEmptyHotbarSlot(mc);

        if (hotbarSlot == -1) {
            hotbarSlot = mc.player.getInventory().selectedSlot;
        }

        if (!moveToHotbar(mc, inventorySlot, hotbarSlot)) {
            return;
        }

        mc.player.getInventory().selectedSlot = hotbarSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        switchTo(State.WAITING_SERVER_MENU);
    }

    private static int findCompassInventorySlot(MinecraftClient mc) {
        if (mc.player == null) {
            return -1;
        }

        for (int slot = 0; slot < 36; slot++) {
            if (isServerCompass(mc.player.getInventory().getStack(slot))) {
                return slot;
            }
        }

        return -1;
    }

    private void waitForServerMenu(MinecraftClient mc) {
        if (BetterRCT.openMenu(mc) != null) {
            switchTo(State.FINDING_WORKBENCH);
            return;
        }

        if (stateTicks >= MENU_TIMEOUT_TICKS) {
            fail(mc, "Не удалось открыть меню выбора серверов.");
        }
    }

    private void findAndClickWorkbench(MinecraftClient mc) {
        HandledScreen<?> screen = BetterRCT.openMenu(mc);

        if (screen == null) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Меню выбора серверов было закрыто.");
            }

            return;
        }

        ScreenHandler menu = screen.getScreenHandler();
        OptionalInt slot = BetterRCT.workbenchSlot(menu);

        if (slot.isEmpty()) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "В меню не найден режим «Гриф выживание».");
            }

            return;
        }

        previousMenuSnapshot = BetterRCT.snapshot(screen);

        if (clickSlot(mc, menu, slot.getAsInt())) {
            switchTo(State.WAITING_WORKBENCH_MENU);
        }
    }

    private void waitForWorkbenchMenu(MinecraftClient mc) {
        if (stateTicks < MENU_CHANGE_DELAY_TICKS) {
            return;
        }

        if (!hasMenuChanged(mc)) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Меню грифа не открылось или не обновилось.");
            }

            return;
        }

        switchTo(getPageState());
    }

    private State getPageState() {
        return getTargetNumber() <= 32 ? State.FINDING_SERVER : State.FINDING_FIRST_PAGE;
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
        OptionalInt slot = BetterRCT.nextPageSlot(menu);

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

        if (getTargetNumber() >= 65) {
            switchTo(State.FINDING_SECOND_PAGE);
        } else {
            switchTo(State.FINDING_SERVER);
        }
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
        OptionalInt slot = BetterRCT.nextPageSlot(menu);

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
        int number = getTargetNumber();

        OptionalInt slot;

        if (number >= 65 && number <= 68) {
            slot = findItemByName(menu, "ГРИФ #" + number + " (1.16.5)");
        } else {
            slot = BetterRCT.playerStackSlot(menu, countFor(number));
        }

        if (slot.isEmpty()) {
            if (stateTicks >= MENU_TIMEOUT_TICKS) {
                fail(mc, "Не удалось найти гриф #" + number + " в меню.");
            }

            return;
        }

        if (clickSlot(mc, menu, slot.getAsInt())) {
            switchTo(State.WAITING_FINISH);
        }
    }

    private static OptionalInt findItemByName(ScreenHandler menu, String expectedName) {
        int slots = BetterRCT.slotCount(menu);
        Matcher expectedMatcher = GRIEF_NUMBER.matcher(expectedName);

        if (!expectedMatcher.find()) {
            return OptionalInt.empty();
        }

        final int expectedNumber;

        try {
            expectedNumber = Integer.parseInt(expectedMatcher.group(1));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }

        for (int i = 0; i < slots; i++) {
            ItemStack stack = menu.slots.get(i).getStack();

            if (stack.isEmpty() || stack.getItem() != Items.PLAYER_HEAD) {
                continue;
            }

            String name = stack.getName().getString();

            if (!BetterRCT.matches(name, "ГРИФ")) {
                continue;
            }

            Matcher matcher = GRIEF_NUMBER.matcher(name);

            if (!matcher.find()) {
                continue;
            }

            try {
                if (Integer.parseInt(matcher.group(1)) == expectedNumber) {
                    return OptionalInt.of(i);
                }
            } catch (NumberFormatException ignored) {}
        }

        return OptionalInt.empty();
    }

    private static int countFor(int number) {
        if (number <= 32) {
            return number;
        }

        if (number <= 64) {
            return number - 32;
        }

        return number - 64;
    }

    private int getTargetNumber() {
        if (targetNumber == null) {
            return -1;
        }

        try {
            return Integer.parseInt(targetNumber);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean hasMenuChanged(MinecraftClient mc) {
        HandledScreen<?> screen = BetterRCT.openMenu(mc);

        if (screen == null || previousMenuSnapshot == null) {
            return false;
        }

        return !previousMenuSnapshot.equals(BetterRCT.snapshot(screen));
    }

    private boolean isTargetGriefOnScoreboard(MinecraftClient mc) {
        return mc != null
            && mc.world != null
            && targetNumber != null
            && BetterRCT.getInstance().isOnReallyWorldServer(targetNumber);
    }

    public boolean scoreboardContains(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc == null || mc.world == null || text == null || text.isBlank()) {
            return false;
        }

        String wanted = BetterRCT.normalize(text);

        for (String line : BetterRCT.sidebarLines(mc)) {
            String normalized = BetterRCT.normalize(line);

            if (normalized.isEmpty()) {
                continue;
            }

            if (wanted.startsWith("гриф")) {
                Matcher wantedMatcher = GRIEF_NUMBER.matcher(wanted);
                Matcher lineMatcher = GRIEF_NUMBER.matcher(normalized);

                if (!wantedMatcher.find()) {
                    continue;
                }

                String expectedNumber = wantedMatcher.group(1);

                while (lineMatcher.find()) {
                    if (expectedNumber.equals(lineMatcher.group(1)) && normalized.contains("гриф")) {
                        return true;
                    }
                }
            } else if (BetterRCT.matches(normalized, wanted)) {
                return true;
            }
        }

        return false;
    }

    private void waitForFinish(MinecraftClient mc) {
        if (!isTargetGriefOnScoreboard(mc)) {
            finishDelayTicks = 0;

            if (stateTicks >= FINISH_TIMEOUT_TICKS) {
                fail(mc, "Вход на гриф #" + targetNumber + " не подтверждён. Проверьте скорборд.");
            }

            return;
        }

        if (++finishDelayTicks < SUCCESS_DELAY_TICKS) {
            return;
        }

        lastNumber = targetNumber;

        Text message = Text.literal("Я успешно зашел на сервер - ")
            .styled(style -> style.withColor(BetterRCT.getInstance().MESSAGE_COLOR))
            .append(Text.literal("Гриф #" + targetNumber)
                .styled(style -> style
                    .withColor(BetterRCT.getInstance().MESSAGE_COLOR)
                    .withBold(true)));

        closeMenu(mc);
        Chat.info(mc, message);
        reset();
    }

    public void stop() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!isBusy()) {
            return;
        }

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

        for (int slot = 0; slot < 9; slot++) {
            if (mc.player.getInventory().getStack(slot).isEmpty()) {
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
        targetNumber = null;
        previousMenuSnapshot = null;

        stateTicks = 0;
        operationTicks = 0;
        finishDelayTicks = 0;

        state = State.IDLE;
    }
}