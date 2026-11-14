package com.zavk1n.bqol.utils.rct;

import com.zavk1n.bqol.features.BetterRCT;
import com.zavk1n.bqol.utils.chat.Chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.Locale;
import java.util.OptionalInt;

@Environment(EnvType.CLIENT)
public class FakeTimeController {
    private static FakeTimeController instance;

    public FakeTimeController() {
        instance = this;
    }

    public static FakeTimeController getInstance() {
        return instance;
    }

    private static final String HUB_COMMAND = "hub";

    private static final int HUB_TIMEOUT_TICKS = 100;
    private static final int SUCCESS_DELAY_TICKS = 30;
    private static final int INVENTORY_SIZE = 9;

    public int operationTimeoutTicks = 500;
    public String lastNumber;

    private int stateTicks;
    private int operationTicks;
    private int finishDelayTicks;

    private String targetNumber;
    private String targetServerName;

    private State state = State.IDLE;

    private enum State {
        IDLE,
        TRAVELLING_TO_HUB,
        WAITING_MENU,
        WAITING_FINISH
    }

    public boolean isBusy() {
        return state != State.IDLE;
    }

    public void start(String number) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null || mc.getNetworkHandler() == null || isBusy()) {
            return;
        }

        final int value;

        try {
            value = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            Chat.send(mc, Chat.invalidNumber());
            return;
        }

        String serverName = getServerName(value);

        if (serverName == null) {
            Chat.send(mc, Chat.invalidNumber());
            return;
        }

        targetNumber = Integer.toString(value);
        targetServerName = serverName;

        operationTicks = 0;
        finishDelayTicks = 0;

        OptionalInt honeycombSlot = findHubHoneycomb(mc);

        if (honeycombSlot.isPresent()) {
            switchTo(State.WAITING_MENU);

            if (!openHoneycomb(mc, honeycombSlot.getAsInt())) {
                reset();
            }

            return;
        }

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
            case WAITING_MENU -> waitForMenu(mc);
            case WAITING_FINISH -> waitForFinish(mc);
            case IDLE -> {
            }
        }
    }

    private void waitForHub(MinecraftClient mc) {
        OptionalInt slot = findHubHoneycomb(mc);

        if (slot.isPresent()) {
            if (openHoneycomb(mc, slot.getAsInt())) {
                switchTo(State.WAITING_MENU);
            }

            return;
        }

        if (stateTicks >= HUB_TIMEOUT_TICKS) {
            fail(mc, "Произошел сбой: после /hub соты «Сервера» не появились.");
        }
    }

    private static OptionalInt findHubHoneycomb(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;

        if (player == null) {
            return OptionalInt.empty();
        }

        var inventory = player.getInventory();
        int size = inventory.size();

        for (int i = 0; i < Math.min(INVENTORY_SIZE, size); i++) {
            if (isServerHoneycomb(inventory.getStack(i))) {
                return OptionalInt.of(i);
            }
        }

        for (int i = INVENTORY_SIZE; i < size; i++) {
            if (isServerHoneycomb(inventory.getStack(i))) {
                return OptionalInt.of(i);
            }
        }

        return OptionalInt.empty();
    }

    private static boolean isServerHoneycomb(ItemStack stack) {
        return !stack.isEmpty()
            && stack.isOf(Items.HONEYCOMB)
            && normalize(stack.getName().getString()).contains("сервера");
    }

    private static boolean openHoneycomb(MinecraftClient mc, int inventorySlot) {
        ClientPlayerEntity player = mc.player;

        if (player == null || mc.interactionManager == null) {
            return false;
        }

        int hotbarSlot;

        if (inventorySlot < INVENTORY_SIZE) {
            hotbarSlot = inventorySlot;
        } else {
            hotbarSlot = player.getInventory().getSelectedSlot();

            mc.interactionManager.clickSlot(player.playerScreenHandler.syncId, inventorySlot, hotbarSlot, SlotActionType.SWAP, player);
        }

        player.getInventory().setSelectedSlot(hotbarSlot);
        mc.interactionManager.interactItem(player, Hand.MAIN_HAND);
        player.swingHand(Hand.MAIN_HAND);

        return true;
    }

    private void waitForMenu(MinecraftClient mc) {
        HandledScreen<?> screen = getOpenMenu(mc);

        if (screen == null) {
            return;
        }

        ScreenHandler menu = screen.getScreenHandler();
        OptionalInt slot = findServerItem(mc, menu);

        if (slot.isEmpty()) {
            return;
        }

        if (clickSlot(mc, menu, slot.getAsInt())) {
            switchTo(State.WAITING_FINISH);
        }
    }

    private OptionalInt findServerItem(MinecraftClient mc, ScreenHandler menu) {
        if (menu == null || targetServerName == null) {
            return OptionalInt.empty();
        }

        String expected = normalize(targetServerName);

        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getStack();

            if (stack.isEmpty()) {
                continue;
            }

            String name = normalize(stack.getName().getString());

            if (containsServerName(name, expected)) {
                return OptionalInt.of(slot.id);
            }

            if (containsServerName(getTooltipText(mc, stack), expected)) {
                return OptionalInt.of(slot.id);
            }
        }

        return OptionalInt.empty();
    }

    private static String getTooltipText(MinecraftClient mc, ItemStack stack) {
        if (stack.isEmpty() || mc.player == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (Text line : stack.getTooltip(net.minecraft.item.Item.TooltipContext.DEFAULT, mc.player, net.minecraft.item.tooltip.TooltipType.BASIC)) {
            result.append(normalize(line.getString()));
        }

        return result.toString();
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

        Text message = Text.literal("Я успешно зашел на сервер - ")
            .styled(style -> style.withColor(betterRCT.MESSAGE_COLOR))
            .append(Text.literal(targetServerName)
                .styled(style -> style
                    .withColor(betterRCT.MESSAGE_COLOR)
                    .withBold(true)));

        closeMenu(mc);
        Chat.info(mc, message);
        reset();
    }

    private static boolean containsServerName(String text, String expected) {
        if (text == null || expected == null || expected.isEmpty()) {
            return false;
        }

        int index = text.indexOf(expected);

        while (index >= 0) {
            int end = index + expected.length();

            if (end >= text.length() || !Character.isDigit(text.charAt(end))) {
                return true;
            }

            index = text.indexOf(expected, index + 1);
        }

        return false;
    }

    private static String getServerName(int number) {
        return switch (number) {
            case 1 -> "СолоЛайт #1";
            case 2 -> "ДуоЛайт #2";
            case 3 -> "КланЛайт #3";
            default -> null;
        };
    }

    private static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return BetterRCT.normalize(text)
            .replaceAll("\\s+", "")
            .toLowerCase(Locale.ROOT);
    }

    private static HandledScreen<?> getOpenMenu(MinecraftClient mc) {
        return mc.currentScreen instanceof HandledScreen<?> screen ? screen : null;
    }

    private static boolean clickSlot(MinecraftClient mc, ScreenHandler menu, int slot) {
        if (mc.interactionManager == null || mc.player == null || menu == null) {
            return false;
        }

        mc.interactionManager.clickSlot(menu.syncId, slot, 0, SlotActionType.PICKUP, mc.player);

        return true;
    }

    private static void closeMenu(MinecraftClient mc) {
        if (mc.player != null && getOpenMenu(mc) != null) {
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
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!isBusy()) {
            return;
        }

        closeMenu(mc);

        BetterRCT betterRCT = BetterRCT.getInstance();
        int color = betterRCT != null ? betterRCT.MESSAGE_COLOR : 0xFFC7C2FF;

        Chat.info(mc, Text.literal("Скрипт RCT был остановлен.")
            .styled(style -> style
                .withColor(color)
                .withBold(true)));

        reset();
    }

    public void fail(MinecraftClient mc, String text) {
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
        targetServerName = null;

        stateTicks = 0;
        operationTicks = 0;
        finishDelayTicks = 0;

        state = State.IDLE;
    }
}