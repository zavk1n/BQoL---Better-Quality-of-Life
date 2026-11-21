package com.zavk1n.bqol.features.rct;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.features.spheres.BetterSpheres;
import com.zavk1n.bqol.mixin.ACCESSORBossHealthOverlay;
import com.zavk1n.bqol.utils.chat.Chat;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import com.zavk1n.bqol.utils.liteapi.restapi.HWServers;
import com.zavk1n.bqol.features.rct.faketime.FakeTimeProcess;
import com.zavk1n.bqol.features.rct.holyworld.HolyWorldProcess;
import com.zavk1n.bqol.features.rct.reallyworld.ReallyWorldProcess;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import net.minecraft.screen.ScreenHandler;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class BetterRCT {
    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        return client;
    }

    private BetterRCT() {
        this.holyWorldController = new HolyWorldProcess();
        this.reallyWorldController = new ReallyWorldProcess();
        this.fakeTimeController = new FakeTimeProcess();
    }

    private static BetterRCT instance;
    private MinecraftClient client;
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean holyworld;
        boolean reallyworld;
        boolean faketime;
    }

    private enum ServerType {
        OTHER,
        HOLYWORLD,
        REALLYWORLD,
        FAKETIME
    }

    private ServerType cachedServerType = ServerType.OTHER;
    private String cachedServerAddress;

    private final HolyWorldProcess holyWorldController;
    private final ReallyWorldProcess reallyWorldController;
    private final FakeTimeProcess fakeTimeController;

    private String currentNumber;
    private String currentSidebarLine;
    private long pvpConfirmDeadline;

    public int MESSAGE_COLOR = 0xC7C2FF;

    private static final String[] COMMANDS = {".rct", ".an"};

    private static final int MAX_REALLYWORLD_ANARCHIES = 88;
    private static final int MAX_FAKETIME_ANARCHIES = 3;

    private static final int INVENTORY_SLOTS = 36;
    private static final long PVP_CONFIRM_MS = 3000L;

    private static final Pattern SERVER_NUM = Pattern.compile("#\\s*(\\d+)");
    private static final Pattern SECTION_CODE = Pattern.compile("§.");
    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final Pattern WORD_SPLIT = Pattern.compile("[^\\p{L}\\p{N}]+");

    public static void initialize() {
        if (instance == null) {
            instance = new BetterRCT();
            instance.refreshBlockedStatusInternal();
            HWServers.getInstance().refresh();
            BQoL.LOGGER.info("Better RCT initialized");
        }
    }

    public static BetterRCT getInstance() {
        if (instance == null) {
            initialize();
        }

        return instance;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) {
            instance.refreshBlockedStatusInternal();
        }
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) {
            instance.setEnabledInternal(enabled);
        }
    }

    public static boolean isHolyWorldRCTEnabled() {
        return instance != null && instance.isHolyWorldRCTEnabledInternal();
    }

    public static void setHolyWorldRCTEnabled(boolean enabled) {
        if (instance != null) {
            instance.setHolyWorldRCTEnabledInternal(enabled);
        }
    }

    public static boolean isReallyWorldRCTEnabled() {
        return instance != null && instance.isReallyWorldRCTEnabledInternal();
    }

    public static void setReallyWorldRCTEnabled(boolean enabled) {
        if (instance != null) {
            instance.setReallyWorldRCTEnabledInternal(enabled);
        }
    }

    public static boolean isFakeTimeRCTEnabled() {
        return instance != null && instance.isFakeTimeRCTEnabledInternal();
    }

    public static void setFakeTimeRCTEnabled(boolean enabled) {
        if (instance != null) {
            instance.setFakeTimeRCTEnabledInternal(enabled);
        }
    }

    public static int getMaxHolyWorldAnarchies() {
        return HWServers.getInstance().getMaxAnarchy();
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_rct");
        blocked.holyworld = LiteApiManager.isFeatureBlocked("better_rct_holyworld");
        blocked.reallyworld = LiteApiManager.isFeatureBlocked("better_rct_reallyworld");
        blocked.faketime = LiteApiManager.isFeatureBlocked("better_rct_faketime");
    }

    private boolean isEnabledInternal() {
        return config.isBetterRCTEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterRCTEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    private boolean isHolyWorldRCTEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterRCTHolyWorld()
            && !blocked.holyworld
            && isHolyWorld();
    }

    private void setHolyWorldRCTEnabledInternal(boolean enabled) {
        config.setBetterRCTHolyWorld(enabled);
    }

    private boolean isReallyWorldRCTEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterRCTReallyWorld()
            && !blocked.reallyworld
            && isReallyWorld();
    }

    private void setReallyWorldRCTEnabledInternal(boolean enabled) {
        config.setBetterRCTReallyWorld(enabled);
    }

    private boolean isFakeTimeRCTEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterRCTFakeTime()
            && !blocked.faketime
            && isFakeTime();
    }

    private void setFakeTimeRCTEnabledInternal(boolean enabled) {
        config.setBetterRCTFakeTime(enabled);
    }

    public void onTick(MinecraftClient mc) {
        if (mc == null) {
            return;
        }

        updateServerType();

        boolean holyWorld = isHolyWorldRCTEnabledInternal();
        boolean reallyWorld = isReallyWorldRCTEnabledInternal();
        boolean fakeTime = isFakeTimeRCTEnabledInternal();

        if (!holyWorld && !reallyWorld && !fakeTime) {
            return;
        }

        if (holyWorld) {
            HWServers.getInstance().updateIfNeeded();
        }

        readSidebar(mc);

        if (holyWorld) {
            holyWorldController.tick(mc);
        }

        if (reallyWorld) {
            reallyWorldController.tick(mc);
        }

        if (fakeTime) {
            fakeTimeController.tick(mc);
        }
    }

    public boolean onChat(String message) {
        if (!isHolyWorldRCTEnabledInternal()
            && !isReallyWorldRCTEnabledInternal()
            && !isFakeTimeRCTEnabledInternal()) {
            return true;
        }

        return handleRctMessage(message);
    }

    public boolean onCommand(String command) {
        if (!isHolyWorldRCTEnabledInternal()
            && !isReallyWorldRCTEnabledInternal()
            && !isFakeTimeRCTEnabledInternal()) {
            return true;
        }

        return handleRctMessage("/" + command);
    }

    private boolean handleRctMessage(String message) {
        if (message == null || message.isBlank()) {
            return true;
        }

        String trimmed = message.trim();
        String command = findCommand(trimmed);

        if (command == null) {
            return true;
        }

        String argument = trimmed.substring(command.length()).trim();

        if (argument.equalsIgnoreCase("stop")) {
            holyWorldController.stop();
            reallyWorldController.stop();
            fakeTimeController.stop();

            return false;
        }

        if (isHolyWorldRCTEnabledInternal()) {
            return handleHolyWorldCommand(argument);
        }

        if (isReallyWorldRCTEnabledInternal()) {
            return handleReallyWorldCommand(argument);
        }

        if (isFakeTimeRCTEnabledInternal()) {
            return handleFakeTimeCommand(argument);
        }

        return true;
    }

    private boolean handleHolyWorldCommand(String argument) {
        String number = requestedHolyWorldNumber(argument);

        if (number == null) {
            sendHolyWorldNumberError(argument);
            return false;
        }

        if (!confirmedDuringPvp()) {
            return false;
        }

        holyWorldController.start(number);

        return false;
    }

    private boolean handleReallyWorldCommand(String argument) {
        String number = requestedReallyWorldNumber(argument);

        if (number == null) {
            sendReallyWorldNumberError(argument);
            return false;
        }

        if (!confirmedDuringPvp()) {
            return false;
        }

        reallyWorldController.start(number);

        return false;
    }

    private boolean handleFakeTimeCommand(String argument) {
        String number = requestedFakeTimeNumber(argument);

        if (number == null) {
            sendFakeTimeNumberError(argument);
            return false;
        }

        if (!confirmedDuringPvp()) {
            return false;
        }

        fakeTimeController.start(number);

        return false;
    }

    private static String findCommand(String message) {
        String lower = message.toLowerCase(Locale.ROOT);

        for (String command : COMMANDS) {
            if (lower.equals(command) || (lower.startsWith(command)
                && lower.length() > command.length()
                && Character.isWhitespace(lower.charAt(command.length())))) {
                return command;
            }
        }

        return null;
    }

    private static boolean isValidHolyWorldNumber(String number) {
        if (number == null || !number.matches("\\d+")) {
            return false;
        }

        try {
            return HWServers.getInstance().exists(Integer.parseInt(number));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String requestedHolyWorldNumber(String argument) {
        HWServers servers = HWServers.getInstance();

        String requested = argument;

        if (requested.isEmpty()) {
            if (isValidHolyWorldNumber(currentNumber)) {
                return Integer.toString(Integer.parseInt(currentNumber));
            }

            requested = holyWorldController.lastNumber;

            if (requested == null || requested.isBlank()) {
                return null;
            }
        }

        if (!requested.matches("\\d+")) {
            return null;
        }

        try {
            int number = Integer.parseInt(requested);

            if (!servers.exists(number)) {
                return null;
            }

            return Integer.toString(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isValidReallyWorldNumber(String number) {
        if (number == null) {
            return false;
        }

        try {
            int value = Integer.parseInt(number);

            return value >= 1 && value <= MAX_REALLYWORLD_ANARCHIES;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String requestedReallyWorldNumber(String argument) {
        if (argument.isEmpty()) {
            if (isValidReallyWorldNumber(currentNumber)) {
                return currentNumber;
            }

            String lastNumber = reallyWorldController.lastNumber;

            return isValidReallyWorldNumber(lastNumber) ? lastNumber : null;
        }

        if (!argument.matches("-?\\d+")) {
            return null;
        }

        try {
            int number = Integer.parseInt(argument);

            if (number < 1 || number > MAX_REALLYWORLD_ANARCHIES) {
                return null;
            }

            return Integer.toString(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isValidFakeTimeNumber(String number) {
        if (number == null) {
            return false;
        }

        try {
            int value = Integer.parseInt(number);

            return value >= 1 && value <= MAX_FAKETIME_ANARCHIES;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String requestedFakeTimeNumber(String argument) {
        if (argument.isEmpty()) {
            if (isValidFakeTimeNumber(currentNumber)) {
                return currentNumber;
            }

            String lastNumber = fakeTimeController.lastNumber;

            return isValidFakeTimeNumber(lastNumber) ? lastNumber : null;
        }

        if (!argument.matches("-?\\d+")) {
            return null;
        }

        try {
            int number = Integer.parseInt(argument);

            if (number < 1 || number > MAX_FAKETIME_ANARCHIES) {
                return null;
            }

            return Integer.toString(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String serverNumber(String text) {
        if (text == null) {
            return null;
        }

        Matcher matcher = SERVER_NUM.matcher(text);

        return matcher.find() ? matcher.group(1) : null;
    }

    private void sendHolyWorldNumberError(String argument) {
        MinecraftClient mc = mc();

        if (argument == null || !argument.matches("\\d+")) {
            Chat.send(mc, Chat.invalidNumber());
            return;
        }

        int number;

        try {
            number = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            Chat.send(mc, Chat.invalidNumber());
            return;
        }

        if (number < 1) {
            Chat.send(mc, Chat.invalidNumber());
            return;
        }

        HWServers servers = HWServers.getInstance();

        if (!servers.isLoaded()) {
            Chat.send(mc, Text.literal("Список серверов HolyWorld ещё не загружен. " + "Используется резервный диапазон 1–36.")
                .styled(style -> style.withColor(MESSAGE_COLOR))
            );

            return;
        }

        Chat.send(mc, Chat.unknownRoute());
    }

    private void sendReallyWorldNumberError(String argument) {
        if (argument.isEmpty()) {
            Chat.send(mc(), Chat.invalidNumber());
            return;
        }

        try {
            int value = Integer.parseInt(argument);

            if (value >= 89) {
                Chat.send(mc(), Chat.unknownRoute());
            } else {
                Chat.send(mc(), Chat.invalidNumber());
            }
        } catch (NumberFormatException e) {
            Chat.send(mc(), Chat.invalidNumber());
        }
    }

    private void sendFakeTimeNumberError(String argument) {
        if (argument.isEmpty()) {
            Chat.send(mc(), Chat.invalidNumber());
            return;
        }

        try {
            int value = Integer.parseInt(argument);

            if (value >= 4) {
                Chat.send(mc(), Chat.unknownRoute());
            } else {
                Chat.send(mc(), Chat.invalidNumber());
            }
        } catch (NumberFormatException e) {
            Chat.send(mc(), Chat.invalidNumber());
        }
    }

    public boolean confirmedDuringPvp() {
        MinecraftClient mc = mc();

        if (!isPvpbarActive(mc)) {
            pvpConfirmDeadline = 0L;
            return true;
        }

        long now = System.currentTimeMillis();

        if (now > pvpConfirmDeadline) {
            pvpConfirmDeadline = now + PVP_CONFIRM_MS;

            warnAboutPvp();

            return false;
        }

        pvpConfirmDeadline = 0L;

        return true;
    }

    private void warnAboutPvp() {
        MinecraftClient mc = mc();

        if (mc.player == null) {
            return;
        }

        Text message = Text.literal("PvP ⚔")
            .styled(style -> style.withColor(MESSAGE_COLOR).withBold(true))
            .append(Text.literal(" — Уверен ли ты что хочешь выйти? Повтори "))
            .append(Text.literal(".rct").styled(style -> style.withColor(MESSAGE_COLOR).withBold(true)))
            .append(Text.literal(" в течение "))
            .append(Text.literal("3 сек.").styled(style -> style.withColor(MESSAGE_COLOR).withBold(true)))
            .append(Text.literal(" для перезахода"));

        Chat.send(mc, message);
    }

    private static boolean isPvpbarActive(MinecraftClient mc) {
        if (mc == null || mc.inGameHud == null) {
            return false;
        }

        BossBarHud overlay = mc.inGameHud.getBossBarHud();

        for (ClientBossBar event : ((ACCESSORBossHealthOverlay) overlay)
            .bqol$getEvents()
            .values()) {

            String name = event.getName()
                .getString()
                .toLowerCase(Locale.ROOT);

            if (name.contains("pvp") || name.contains("пвп")) {
                return true;
            }
        }

        return false;
    }

    public static List<String> loreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();

        if (stack == null || stack.isEmpty()) {
            return lines;
        }

        LoreComponent lore = stack.get(DataComponentTypes.LORE);

        if (lore == null) {
            return lines;
        }

        for (Text line : lore.lines()) {
            lines.add(line.getString());
        }

        return lines;
    }

    public static List<String> sidebarLines(MinecraftClient mc) {
        List<String> lines = new ArrayList<>();

        if (mc == null || mc.world == null) {
            return lines;
        }

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);

        if (sidebar == null) {
            return lines;
        }

        lines.add(sidebar.getDisplayName().getString());

        for (ScoreboardEntry entry : scoreboard.getScoreboardEntries(sidebar)) {
            if (entry.hidden()) {
                continue;
            }

            String line = ownerLine(scoreboard, entry);

            if (!line.isBlank()) {
                lines.add(line);
            }
        }

        return lines;
    }

    private static String ownerLine(Scoreboard scoreboard, ScoreboardEntry entry) {
        Team team = scoreboard.getScoreHolderTeam(entry.owner());

        return Team.decorateName(team, entry.name()).getString();
    }

    private void readSidebar(MinecraftClient mc) {
        currentSidebarLine = null;
        currentNumber = null;

        if (mc == null || mc.world == null) {
            return;
        }

        for (String line : sidebarLines(mc)) {
            String number = serverNumber(line);

            if (number != null) {
                currentSidebarLine = line;
                currentNumber = number;
                return;
            }
        }
    }

    public static HandledScreen<?> openMenu(MinecraftClient mc) {
        if (mc == null) {
            return null;
        }

        Screen screen = mc.currentScreen;

        if (screen instanceof HandledScreen<?> handled && !(screen instanceof InventoryScreen)) {
            return handled;
        }

        return null;
    }

    public static int slotCount(ScreenHandler menu) {
        if (menu == null) {
            return 0;
        }

        int total = menu.slots.size();

        return total > INVENTORY_SLOTS ? total - INVENTORY_SLOTS : total;
    }

    public static boolean matches(String candidate, String wanted) {
        String left = normalize(candidate);
        String right = normalize(wanted);

        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }

        if (left.equals(right) || left.contains(right)) {
            return true;
        }

        List<String> wantedTokens = tokens(right);

        if (wantedTokens.isEmpty()) {
            return false;
        }

        for (String token : wantedTokens) {
            if (!left.contains(token)) {
                return false;
            }
        }

        return true;
    }

    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String clean = SECTION_CODE.matcher(text)
            .replaceAll("");

        return SPACES.matcher(clean)
            .replaceAll(" ")
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    public static List<String> tokens(String text) {
        List<String> result = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return result;
        }

        for (String part : WORD_SPLIT.split(normalize(text))) {
            if (part.length() >= 2) {
                result.add(part);
            }
        }

        return result;
    }

    public static String combinedInfo(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }

        StringBuilder text = new StringBuilder(stack.getName().getString());

        for (String line : loreLines(stack)) {
            text.append('\n').append(line);
        }

        return text.toString();
    }

    public static OptionalInt slotMatching(ScreenHandler menu, Predicate<ItemStack> predicate) {
        if (menu == null || predicate == null) {
            return OptionalInt.empty();
        }

        int slots = slotCount(menu);

        for (int slot = 0; slot < slots; ++slot) {
            ItemStack stack = menu.slots.get(slot).getStack();

            if (!stack.isEmpty() && predicate.test(stack)) {
                return OptionalInt.of(slot);
            }
        }

        return OptionalInt.empty();
    }

    public static OptionalInt playerStackSlot(ScreenHandler menu, int count) {
        return slotMatching(menu, stack -> stack.getItem() == Items.PLAYER_HEAD && stack.getCount() == count);
    }

    public static String snapshot(HandledScreen<?> screen) {
        if (screen == null) {
            return "";
        }

        ScreenHandler menu = screen.getScreenHandler();

        StringBuilder snapshot = new StringBuilder()
            .append(menu.syncId)
            .append('|')
            .append(normalize(screen.getTitle().getString()))
            .append('|');

        int slots = slotCount(menu);

        for (int slot = 0; slot < slots; ++slot) {
            ItemStack stack = menu.slots.get(slot).getStack();

            if (stack.isEmpty()) {
                snapshot.append("-");
            } else {
                snapshot
                    .append(normalize(combinedInfo(stack)))
                    .append("|item=")
                    .append(net.minecraft.registry.Registries.ITEM.getId(stack.getItem()))
                    .append("|count=")
                    .append(stack.getCount());
            }

            snapshot.append(';');
        }

        return snapshot.toString();
    }

    public boolean isOnServer(String number) {
        return number != null && number.equals(currentNumber);
    }

    private void updateServerType() {
        MinecraftClient client = mc();
        ServerInfo server = client.getCurrentServerEntry();

        String address = server != null ? server.address : null;

        if (Objects.equals(cachedServerAddress, address)) {
            return;
        }

        cachedServerAddress = address;

        if (address == null || address.isEmpty()) {
            cachedServerType = ServerType.OTHER;
            return;
        }

        String normalized = address.toLowerCase(Locale.ROOT);

        if (normalized.contains("holyworld")) {
            cachedServerType = ServerType.HOLYWORLD;
        } else if (normalized.contains("reallyworld")) {
            cachedServerType = ServerType.REALLYWORLD;
        } else if (normalized.contains("faketime")) {
            cachedServerType = ServerType.FAKETIME;
        }
        else {
            cachedServerType = ServerType.OTHER;
        }
    }

    private boolean isHolyWorld() {
        return cachedServerType == ServerType.HOLYWORLD;
    }

    private boolean isReallyWorld() {
        return cachedServerType == ServerType.REALLYWORLD;
    }

    private boolean isFakeTime() {
        return cachedServerType == ServerType.FAKETIME;
    }
}