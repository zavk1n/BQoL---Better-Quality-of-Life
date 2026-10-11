package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.mixin.ACCESSORBossHealthOverlay;
import com.zavk1n.bqol.utils.chat.Chat;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import com.zavk1n.bqol.utils.rct.HolyWorldController;
import com.zavk1n.bqol.utils.rct.ReallyWorldController;

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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.screen.ScreenHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
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
        this.holyWorldController = new HolyWorldController();
        this.reallyWorldController = new ReallyWorldController();
    }

    private static BetterRCT instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean holyworld;
        boolean reallyworld;
    }

    private final HolyWorldController holyWorldController;
    private final ReallyWorldController reallyWorldController;

    private String currentNumber;
    private String currentSidebarLine;
    private long pvpConfirmDeadline;
    public int MESSAGE_COLOR = 0xC7C2FF;
    private static final String[] COMMANDS = {".rct", ".an"};

    private static final int SIDEBAR_SLOT = 1;
    private static final int MAX_HOLYWORLD_ANARCHIES = 36;
    private static final int MAX_REALLYWORLD_ANARCHIES = 32;
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

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_rct");
        blocked.holyworld = LiteApiManager.isFeatureBlocked("better_rct_holyworld");
        blocked.reallyworld = LiteApiManager.isFeatureBlocked("better_rct_reallyworld");
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

    public void onTick(MinecraftClient mc) {
        boolean holyWorld = isHolyWorldRCTEnabledInternal();
        boolean reallyWorld = isReallyWorldRCTEnabledInternal();

        if (!holyWorld && !reallyWorld) {
            return;
        }

        readSidebar(mc);

        if (holyWorld) {
            holyWorldController.tick(mc);
        }

        if (reallyWorld) {
            reallyWorldController.tick(mc);
        }
    }

    public boolean onChat(String message) {
        if (!isHolyWorldRCTEnabledInternal() && !isReallyWorldRCTEnabledInternal()) {
            return true;
        }

        return handleRctMessage(message);
    }

    public boolean onCommand(String command) {
        if (!isHolyWorldRCTEnabledInternal() && !isReallyWorldRCTEnabledInternal()) {
            return true;
        }

        return handleRctMessage("/" + command);
    }

    private boolean handleRctMessage(String message) {
        String trimmed = message.trim();
        String command = findCommand(trimmed);

        if (command == null) {
            return true;
        }

        String argument = trimmed.substring(command.length())
            .trim();

        if (argument.equalsIgnoreCase("stop")) {
            holyWorldController.stop();
            reallyWorldController.stop();

            return false;
        }

        if (isHolyWorldRCTEnabledInternal()) {
            return handleHolyWorldCommand(argument);
        }

        if (isReallyWorldRCTEnabledInternal()) {
            return handleReallyWorldCommand(argument);
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

    private static String findCommand(String message) {
        String lower = message.toLowerCase();

        for (String command : COMMANDS) {
            if (lower.equals(command) || lower.startsWith(command + " ")) {
                return command;
            }
        }

        return null;
    }

    private String requestedHolyWorldNumber(String argument) {
        if (argument.isEmpty()) {
            if (currentNumber != null) {
                return currentNumber;
            }

            return holyWorldController.lastNumber;
        }

        if (!argument.matches("\\d+")) {
            return null;
        }

        int number;

        try {
            number = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            return null;
        }

        if (number < 1 || number > MAX_HOLYWORLD_ANARCHIES) {
            return null;
        }

        return Integer.toString(number);
    }

    private String requestedReallyWorldNumber(String argument) {
        if (argument.isEmpty()) {
            if (currentNumber != null && isValidReallyWorldNumber(currentNumber)) {
                return currentNumber;
            }

            return reallyWorldController.lastNumber;
        }

        if (!argument.matches("-?\\d+")) {
            return null;
        }

        int number;

        try {
            number = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            return null;
        }

        if (number < 1 || number > MAX_REALLYWORLD_ANARCHIES) {
            return null;
        }

        return Integer.toString(number);
    }

    private static boolean isValidReallyWorldNumber(String number) {
        try {
            int value = Integer.parseInt(number);

            return value >= 1 && value <= MAX_REALLYWORLD_ANARCHIES;
        } catch (NumberFormatException e) {
            return false;
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
        if (argument.isEmpty()) {
            Chat.send(mc(), Chat.invalidNumber());

            return;
        }

        try {
            int value = Integer.parseInt(argument);

            if (value >= 52) {
                Chat.send(mc(), Chat.unknownRoute());
            } else {
                Chat.send(mc(), Chat.invalidNumber());
            }
        } catch (NumberFormatException e) {
            Chat.send(mc(), Chat.invalidNumber());
        }
    }

    private void sendReallyWorldNumberError(String argument) {
        if (argument.isEmpty()) {
            Chat.send(mc(), Chat.invalidNumber());

            return;
        }

        try {
            int value = Integer.parseInt(argument);

            if (value >= 69) {
                Chat.send(mc(), Chat.unknownRoute());
            } else {
                Chat.send(mc(), Chat.invalidNumber());
            }
        } catch (NumberFormatException e) {
            Chat.send(mc(), Chat.invalidNumber());
        }
    }

    public boolean confirmedDuringPvp() {
        if (!isPvpbarActive(mc())) {
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
        MinecraftClient mc = MinecraftClient.getInstance();

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
        BossBarHud overlay = mc.inGameHud.getBossBarHud();

        for (ClientBossBar event : ((ACCESSORBossHealthOverlay) overlay)
            .bqol$getEvents()
            .values()) {

            String name = event.getName()
                .getString()
                .toLowerCase();

            if (name.contains("pvp") || name.contains("пвп")) {
                return true;
            }
        }

        return false;
    }

    public static List<String> loreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();

        NbtCompound display = stack.getSubNbt("display");

        if (display == null) {
            return lines;
        }

        NbtList lore = display.getList("Lore", NbtElement.STRING_TYPE);

        for (int i = 0; i < lore.size(); i++) {
            Text line = Text.Serializer.fromJson(lore.getString(i));

            if (line != null) {
                lines.add(line.getString());
            }
        }

        return lines;
    }

    public static List<String> sidebarLines(MinecraftClient mc) {
        List<String> lines = new ArrayList<>();

        if (mc == null || mc.world == null) {
            return lines;
        }

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(SIDEBAR_SLOT);

        if (sidebar == null) {
            return lines;
        }

        lines.add(sidebar.getDisplayName().getString());

        for (ScoreboardPlayerScore score : scoreboard.getAllPlayerScores(sidebar)) {
            String line = ownerLine(scoreboard, score.getPlayerName());

            if (!line.isBlank()) {
                lines.add(line);
            }
        }

        return lines;
    }

    private static String ownerLine(Scoreboard scoreboard, String owner) {
        Team team = scoreboard.getPlayerTeam(owner);

        return Team.decorateName(team, Text.literal(owner)).getString();
    }

    private void readSidebar(MinecraftClient mc) {
        currentSidebarLine = null;
        currentNumber = null;

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
        Screen screen = mc.currentScreen;

        boolean serverMenu = screen instanceof HandledScreen && !(screen instanceof InventoryScreen);

        return serverMenu ? (HandledScreen<?>) screen : null;
    }

    public static int slotCount(ScreenHandler menu) {
        int total = menu.slots.size();

        return total > INVENTORY_SLOTS ? total - INVENTORY_SLOTS : total;
    }

    public static boolean matches(String candidate, String wanted) {
        String left = normalize(candidate);
        String right = normalize(wanted);

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
        if (text == null) {
            return "";
        }

        String clean = SECTION_CODE
            .matcher(text)
            .replaceAll("");

        return SPACES
            .matcher(clean)
            .replaceAll(" ")
            .trim()
            .toLowerCase();
    }

    public static List<String> tokens(String text) {
        List<String> result = new ArrayList<>();

        for (String part : WORD_SPLIT.split(normalize(text))) {
            if (part.length() >= 2) {
                result.add(part);
            }
        }

        return result;
    }

    public static String combinedInfo(ItemStack stack) {
        StringBuilder text = new StringBuilder(stack.getName().getString());

        for (String line : loreLines(stack)) {
            text.append('\n').append(line);
        }

        return text.toString();
    }

    public static OptionalInt slotMatching(ScreenHandler menu, Predicate<ItemStack> predicate) {
        int slots = slotCount(menu);

        for (int slot = 0; slot < slots; ++slot) {
            ItemStack stack = menu.slots
                .get(slot)
                .getStack();

            if (!stack.isEmpty() && predicate.test(stack)) {
                return OptionalInt.of(slot);
            }
        }

        return OptionalInt.empty();
    }

    public static OptionalInt liteAnarchySlot(ScreenHandler menu) {
        return slotMatching(menu, stack -> {
            String text = normalize(combinedInfo(stack));

            return text.contains("лайт") && text.contains("анархия") && text.contains("1.16");
        });
    }

    public static OptionalInt armorStandSlot(ScreenHandler menu, int count) {
        return slotMatching(menu, stack -> stack.getItem() == Items.ARMOR_STAND && stack.getCount() == count);
    }

    public static OptionalInt playerStackSlot(ScreenHandler menu, int count) {
        return slotMatching(menu, stack -> stack.getItem() == Items.PLAYER_HEAD && stack.getCount() == count);
    }

    public static OptionalInt workbenchSlot(ScreenHandler menu) {
        return slotMatching(menu, stack -> {
            if (stack.getItem() != Items.CRAFTING_TABLE) {
                return false;
            }

            return matches(stack.getName().getString(), "ГРИФ ВЫЖИВАНИЕ (1.16.5)");
        });
    }

    public static OptionalInt nextPageSlot(ScreenHandler menu) {
        return slotMatching(menu, stack -> {
            if (stack.getItem() != Items.ARROW) {
                return false;
            }

            return matches(stack.getName().getString(), "Следующая страница");
        });
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
            ItemStack stack = menu.slots
                .get(slot)
                .getStack();

            if (stack.isEmpty()) {
                snapshot.append("-");
            } else {
                snapshot
                    .append(normalize(combinedInfo(stack)))
                    .append("|item=")
                    .append(net.minecraft.registry.Registries.ITEM
                            .getId(stack.getItem()))
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

    public boolean isOnReallyWorldServer(String number) {
        if (number == null || number.isBlank() || mc().world == null) {
            return false;
        }

        return reallyWorldController.scoreboardContains("ГРИФ #" + number);
    }

    private boolean isHolyWorld() {
        MinecraftClient client = mc();

        if (client == null) {
            return false;
        }

        ServerInfo server = client.getCurrentServerEntry();

        if (server == null || server.address == null) {
            return false;
        }

        return server.address
            .toLowerCase(Locale.ROOT)
            .contains("holyworld");
    }

    private boolean isReallyWorld() {
        MinecraftClient client = mc();

        if (client == null) {
            return false;
        }

        ServerInfo server = client.getCurrentServerEntry();

        if (server == null || server.address == null) {
            return false;
        }

        return server.address
            .toLowerCase(Locale.ROOT)
            .contains("reallyworld");
    }
}