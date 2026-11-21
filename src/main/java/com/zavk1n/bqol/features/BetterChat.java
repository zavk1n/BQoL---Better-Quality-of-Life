package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BetterChat {
    private BetterChat() {}

    private static BetterChat instance;
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean stackMessages;
        boolean animation;
        boolean moreHistory;
        boolean heads;
        boolean ping;
        boolean transliteration;
        boolean killMessage;
    }

    private record StyleRun(int start, int end, Style style) {}

    private static final Pattern VALID_PREFIX_PATTERN = Pattern.compile("(?:\\s*(?:\\[[^\\]]*\\]|\\{[^}]*\\}):?)*\\s*");

    private static final int MAX_PATTERN_CACHE_SIZE = 128;

    private static final Map<String, Pattern> SENDER_PATTERN_CACHE =
        new LinkedHashMap<>(MAX_PATTERN_CACHE_SIZE, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
                return size() > MAX_PATTERN_CACHE_SIZE;
            }
        };

    private static final int MAX_SENDER_CACHE_SIZE = 256;

    private static final Map<String, PlayerListEntry> SENDER_CACHE =
        new LinkedHashMap<>(MAX_SENDER_CACHE_SIZE, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, PlayerListEntry> eldest) {
                return size() > MAX_SENDER_CACHE_SIZE;
            }
        };

    private static final int HEAD_SIZE = 8;

    private static final int CHAT_PING_COLOR = 0xA099FF;
    private static final int STACK_COLOR = 0x8C8C8C;

    private static final long KILL_WINDOW_NANOS = 8_000_000_000L;
    private static final long KILL_REMOVE_WINDOW_NANOS = 2_000_000_000L;
    private static final long STACK_INTERVAL_NANOS = 4_000_000_000L;

    private static final float KILL_REMOVE_MAX_HEALTH = 10.0F;
    private static final float KILL_REMOVE_MAX_DISTANCE = 12.0F;

    private static float killTargetLastHealth = 20.0F;
    private static float killTargetLastDistance = 0.0F;

    private static PlayerEntity killTarget = null;
    private static long killTargetTime = 0L;

    private static int stackCount = 0;
    private static long lastStackTime = 0L;
    private static Text lastStackMessage = null;

    private static boolean stackUpdating = false;

    public static void initialize() {
        if (instance == null) {
            instance = new BetterChat();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Chat initialized");
        }
    }

    public static BetterChat getInstance() {
        if (instance == null)
            initialize();
        return instance;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) instance.refreshBlockedStatusInternal();
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) instance.setEnabledInternal(enabled);
    }

    public static boolean isBetterChatStackMessagesEnabled() {
        return instance != null && instance.isBetterChatStackMessagesEnabledInternal();
    }

    public static void setBetterChatStackMessagesEnabled(boolean enabled) {
        if (instance != null) instance.setBetterChatStackMessagesEnabledInternal(enabled);
    }

    public static boolean isBetterChatAnimationEnabled() {
        return instance != null && instance.isBetterChatAnimationEnabledInternal();
    }

    public static void setBetterChatAnimationEnabled(boolean enabled) {
        if (instance != null) instance.setBetterChatAnimationEnabledInternal(enabled);
    }

    public static boolean isBetterChatMoreHistoryEnabled() {
        return instance != null && instance.isBetterChatMoreHistoryEnabledInternal();
    }

    public static void setBetterChatMoreHistoryEnabled(boolean enabled) {
        if (instance != null) instance.setBetterChatMoreHistoryEnabledInternal(enabled);
    }

    public static boolean isBetterChatHeadsEnabled() {
        return instance != null && instance.isBetterChatHeadsEnabledInternal();
    }

    public static void setBetterChatHeadsEnabled(boolean enabled) {
        if (instance != null) instance.setBetterChatHeadsEnabledInternal(enabled);
    }

    public static boolean isBetterChatPingEnabled() {
        return instance != null && instance.isBetterChatPingEnabledInternal();
    }

    public static void setBetterChatPingEnabled(boolean enabled) {
        if (instance != null) instance.setBetterChatPingEnabledInternal(enabled);
    }

    public static boolean isBetterChatTransliterationEnabled() {
        return instance != null && instance.isBetterChatTransliterationEnabledInternal();
    }

    public static void setBetterChatTransliterationEnabled(boolean enabled) {
        if (instance != null) instance.setBetterChatTransliterationEnabledInternal(enabled);
    }

    public static boolean isBetterChatKillMessageEnabled() {
        return instance != null && instance.isBetterChatKillMessageEnabledInternal();
    }

    public static void setBetterChatKillMessageEnabled(boolean enabled) {
        if (instance != null) instance.setBetterChatKillMessageEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_chat");
        blocked.stackMessages = LiteApiManager.isFeatureBlocked("better_chat_stack_messages");
        blocked.animation = LiteApiManager.isFeatureBlocked("better_chat_animation");
        blocked.moreHistory = LiteApiManager.isFeatureBlocked("better_chat_more_history");
        blocked.heads = LiteApiManager.isFeatureBlocked("better_chat_heads");
        blocked.ping = LiteApiManager.isFeatureBlocked("better_chat_ping");
        blocked.transliteration = LiteApiManager.isFeatureBlocked("better_chat_transliteration");
        blocked.killMessage = LiteApiManager.isFeatureBlocked("better_chat_kill_message");
    }

    private boolean isEnabledInternal() {
        return config.isBetterChatEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterChatEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    private boolean isBetterChatStackMessagesEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterChatStackMessages()
            && !blocked.stackMessages;
    }

    private void setBetterChatStackMessagesEnabledInternal(boolean enabled) {
        config.setBetterChatStackMessages(enabled);
    }

    private boolean isBetterChatAnimationEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterChatAnimation()
            && !blocked.animation;
    }

    private void setBetterChatAnimationEnabledInternal(boolean enabled) {
        config.setBetterChatAnimation(enabled);
    }

    private boolean isBetterChatMoreHistoryEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterChatMoreHistory()
            && !blocked.moreHistory;
    }

    private void setBetterChatMoreHistoryEnabledInternal(boolean enabled) {
        config.setBetterChatMoreHistory(enabled);
    }

    private boolean isBetterChatHeadsEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterChatHeads()
            && !blocked.heads;
    }

    private void setBetterChatHeadsEnabledInternal(boolean enabled) {
        config.setBetterChatHeads(enabled);
    }

    private boolean isBetterChatPingEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterChatPing()
            && !blocked.ping;
    }

    private void setBetterChatPingEnabledInternal(boolean enabled) {
        config.setBetterChatPing(enabled);
    }

    private boolean isBetterChatTransliterationEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterChatTransliteration()
            && !blocked.transliteration;
    }

    private void setBetterChatTransliterationEnabledInternal(boolean enabled) {
        config.setBetterChatTransliteration(enabled);
    }

    private boolean isBetterChatKillMessageEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterChatKillMessage()
            && !blocked.killMessage;
    }

    private void setBetterChatKillMessageEnabledInternal(boolean enabled) {
        config.setBetterChatKillMessage(enabled);
    }

    public static boolean isStackUpdating() {
        return stackUpdating;
    }

    private static boolean removeLastStackMessage(ChatHud hud) {
        return ((ChatStackAccess) hud).bqol$removeLastStackMessage();
    }

    public interface ChatStackAccess {
        boolean bqol$removeLastStackMessage();
    }

    public static void onAttack(PlayerEntity victim) {
        if (!isBetterChatKillMessageEnabled()
            || victim == null
            || victim.isDead()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        killTarget = victim;
        killTargetTime = System.nanoTime();
        killTargetLastHealth = victim.getHealth() + victim.getAbsorptionAmount();
        killTargetLastDistance = client.player != null ? client.player.distanceTo(victim) : 0.0F;
    }

    public static void onDeath(PlayerEntity victim) {
        if (killTarget == null
            || victim == null
            || !isBetterChatKillMessageEnabled()
            || !killTarget.getUuid().equals(victim.getUuid())) {
            return;
        }

        if (System.nanoTime() - killTargetTime > KILL_WINDOW_NANOS) {
            resetKillTarget();
            return;
        }

        resetKillTarget();
        sendKillMessage(MinecraftClient.getInstance(), victim);
    }

    public void tick(MinecraftClient client) {
        if (killTarget == null) {
            return;
        }

        long now = System.nanoTime();

        if (client.player == null
            || client.world == null
            || !isBetterChatKillMessageEnabled()
            || now - killTargetTime > KILL_WINDOW_NANOS) {
            resetKillTarget();
            return;
        }

        if (killTarget.isDead() || killTarget.getHealth() <= 0.0F) {
            PlayerEntity victim = killTarget;
            resetKillTarget();
            sendKillMessage(client, victim);
            return;
        }

        boolean gone = killTarget.isRemoved() || client.world.getEntityById(killTarget.getId()) != killTarget;

        if (gone) {
            PlayerEntity victim = killTarget;

            boolean recentHit = now - killTargetTime <= KILL_REMOVE_WINDOW_NANOS;
            boolean wasLow = killTargetLastHealth <= KILL_REMOVE_MAX_HEALTH;
            boolean wasClose = killTargetLastDistance <= KILL_REMOVE_MAX_DISTANCE;

            resetKillTarget();

            if (recentHit && wasLow && wasClose) {
                sendKillMessage(client, victim);
            }

            return;
        }

        killTargetLastHealth = killTarget.getHealth() + killTarget.getAbsorptionAmount();
        killTargetLastDistance = client.player.distanceTo(killTarget);
    }

    private static void sendKillMessage(MinecraftClient client, PlayerEntity victim) {
        if (client == null || client.player == null || victim == null) {
            return;
        }

        ClientPlayNetworkHandler handler = client.getNetworkHandler();

        if (handler == null) {
            return;
        }

        String text = getInstance().config.getBetterChatKillMessageText();

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        text = text.trim();

        if (text.startsWith("/")) {
            handler.sendChatCommand(text.substring(1));
        } else {
            handler.sendChatMessage(text);
        }
    }

    public static Text processChatPing(Text message) {
        if (message == null || !isBetterChatPingEnabled()) {
            return message;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.getNetworkHandler() == null) {
            return message;
        }

        String nickname = client.player.getNameForScoreboard();

        if (nickname == null || nickname.isEmpty()) {
            return message;
        }

        PlayerListEntry sender = findSender(message.getString());

        if (sender == null || sender.getProfile().id().equals(client.player.getUuid())) {
            return message;
        }

        String plainText = message.getString();

        if (plainText.isEmpty() || !getSenderPattern(nickname).matcher(plainText).find()) {
            return message;
        }

        StringBuilder plain = new StringBuilder(plainText.length());
        List<StyleRun> styles = new ArrayList<>();

        message.visit((style, part) -> {
            int start = plain.length();

            plain.append(part);

            int end = plain.length();

            if (start < end) {
                if (!styles.isEmpty()) {
                    int lastIndex = styles.size() - 1;

                    StyleRun last = styles.get(lastIndex);

                    if (last.end() == start && last.style().equals(style)) {
                        styles.set(lastIndex, new StyleRun(last.start(), end, style));
                    } else {
                        styles.add(new StyleRun(start, end, style));
                    }
                } else {
                    styles.add(new StyleRun(start, end, style));
                }
            }

            return Optional.<Void>empty();
        }, Style.EMPTY);

        if (plain.isEmpty()) {
            return message;
        }

        Matcher matcher = getSenderPattern(nickname).matcher(plain);

        if (!matcher.find()) {
            return message;
        }

        MutableText result = Text.empty();

        int position = 0;

        do {
            appendStyledRange(result, plain, styles, position, matcher.start(), false);
            appendStyledRange(result, plain, styles, matcher.start(), matcher.end(), true);

            position = matcher.end();

        } while (matcher.find());

        appendStyledRange(result, plain, styles, position, plain.length(), false);

        return result;
    }

    private static void appendStyledRange(MutableText result, CharSequence source, List<StyleRun> styles, int start, int end, boolean highlighted) {
        if (start >= end) {
            return;
        }

        for (StyleRun run : styles) {
            if (run.end() <= start) {
                continue;
            }

            if (run.start() >= end) {
                break;
            }

            int from = Math.max(start, run.start());
            int to = Math.min(end, run.end());

            if (from >= to) {
                continue;
            }

            Style style = run.style();

            if (highlighted) {
                style = style
                    .withColor(CHAT_PING_COLOR)
                    .withBold(true);
            }

            result.append(Text.literal(source.subSequence(from, to).toString())
                .setStyle(style)
            );
        }
    }

    public static PlayerListEntry findSender(String message) {
        if ((!isBetterChatHeadsEnabled() && !isBetterChatPingEnabled())
            || message == null
            || message.isEmpty()) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        ClientPlayNetworkHandler connection = client.getNetworkHandler();

        if (connection == null) {
            return null;
        }

        String clean = cleanChatMessage(message);

        Collection<PlayerListEntry> players = connection.getPlayerList();

        PlayerListEntry bestMatch = null;

        int bestPosition = Integer.MAX_VALUE;

        for (PlayerListEntry player : players) {
            if (player == null || player.getProfile() == null) {
                continue;
            }

            String nickname = player.getProfile().name();

            if (nickname == null || nickname.isEmpty()) {
                continue;
            }

            Matcher matcher = getSenderPattern(nickname).matcher(clean);

            while (matcher.find()) {
                if (!isValidChatSender(clean, matcher.start(), matcher.end())) {
                    continue;
                }

                if (matcher.start() < bestPosition) {
                    bestPosition = matcher.start();
                    bestMatch = player;
                }

                break;
            }

            if (bestPosition == 0) {
                break;
            }
        }

        return bestMatch;
    }

    private static String cleanChatMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder(message.length());

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            if (c == '§' && i + 1 < message.length()) {
                i++;
                continue;
            }

            result.append(c);
        }

        return result.toString();
    }

    private static boolean isValidChatSender(String message, int start, int end) {
        if (start < 0 || end > message.length()) {
            return false;
        }

        int colon = message.indexOf(':', end);

        if (colon >= 0 && colon - end <= 40) {
            return true;
        }

        if (start > 0
            && message.charAt(start - 1) == '<'
            && end < message.length()
            && message.charAt(end) == '>') {
            return true;
        }

        if (!VALID_PREFIX_PATTERN.matcher(message)
            .region(0, start)
            .matches()) {
            return false;
        }

        return end < message.length() && Character.isWhitespace(message.charAt(end));
    }

    public static boolean renderHeadForMessage(DrawContext context, int x, int y, String message, float opacity) {
        if (!isBetterChatHeadsEnabled() || context == null || message == null) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.getNetworkHandler() == null) {
            return false;
        }

        PlayerListEntry sender = getCachedSender(message);

        if (sender == null) {
            sender = findSender(message);

            if (sender != null) {
                cacheSender(message, sender);
            }
        }

        if (sender == null) {
            return false;
        }

        SkinTextures skin;

        if (sender.getProfile().id().equals(client.player.getUuid())) {
            skin = client.player.getSkin();
        } else {
            skin = sender.getSkinTextures();
        }

        int alpha = (int) (Math.max(0.0F, Math.min(1.0F, opacity)) * 255.0F);

        if (alpha == 0) {
            return false;
        }

        PlayerSkinDrawer.draw(context, skin, x, y, HEAD_SIZE, (alpha << 24) | 0xFFFFFF);

        return true;
    }

    public static boolean processStackMessage(ChatHud hud, Text message, MessageSignatureData signature, MessageIndicator indicator) {
        if (stackUpdating || message == null) {
            return false;
        }

        if (!isBetterChatStackMessagesEnabled()) {
            resetStackMessages();
            return false;
        }

        long now = System.nanoTime();

        boolean sameMessage = lastStackMessage != null && Objects.equals(lastStackMessage, message);
        boolean withinInterval = now - lastStackTime < STACK_INTERVAL_NANOS;

        if (!sameMessage || !withinInterval) {
            lastStackMessage = message.copy();
            lastStackTime = now;
            stackCount = 1;
            return false;
        }

        lastStackTime = now;

        stackCount++;

        MutableText stacked = lastStackMessage.copy();

        stacked.append(Text.literal(" (x" + stackCount + ")")
            .styled(style -> style.withColor(STACK_COLOR))
        );

        stackUpdating = true;

        try {
            if (!removeLastStackMessage(hud)) {
                resetStackMessages();
                return false;
            }

            hud.addMessage(stacked, signature, indicator);

            return true;
        } finally {
            stackUpdating = false;
        }
    }

    public static String transliterateOutgoingMessage(String message) {
        if (!isBetterChatTransliterationEnabled() || message == null || message.isEmpty()) {
            return message;
        }

        StringBuilder result = new StringBuilder(message.length());

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            switch (c) {
                case 'Ы':
                case 'ы':
                    result.append("bI");
                    break;
                case 'Я':
                case 'я':
                    result.append("9I");
                    break;
                case 'б':
                    result.append('6');
                    break;
                case 'Ь':
                case 'ь':
                    result.append('b');
                    break;
                case 'O':
                case 'о':
                case 'o':
                    result.append('0');
                    break;
                case 'е':
                case 'Е':
                    result.append('3');
                    break;
                case 'Ч':
                case 'ч':
                    result.append('4');
                    break;
                case 'Ю':
                case 'ю':
                    result.append("IO");
                    break;
                case 't':
                    result.append('T');
                    break;
                case 'x':
                    result.append('X');
                    break;
                default:
                    result.append(c);
                    break;
            }
        }

        return result.toString();
    }

    private static PlayerListEntry getCachedSender(String message) {
        PlayerListEntry sender = SENDER_CACHE.get(message);

        if (sender == null) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler connection = client.getNetworkHandler();

        if (connection == null || !connection.getPlayerList().contains(sender)) {
            SENDER_CACHE.remove(message);
            return null;
        }

        return sender;
    }

    private static void cacheSender(String message, PlayerListEntry sender) {
        if (message != null && sender != null) {
            SENDER_CACHE.put(message, sender);
        }
    }

    private static Pattern getSenderPattern(String nickname) {
        return SENDER_PATTERN_CACHE.computeIfAbsent(nickname, name -> Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(name) + "(?![A-Za-z0-9_])", Pattern.CASE_INSENSITIVE));
    }

    public static void clearSenderCache() {
        SENDER_CACHE.clear();
        SENDER_PATTERN_CACHE.clear();
    }

    public static void resetKillTarget() {
        killTarget = null;
        killTargetTime = 0L;
        killTargetLastHealth = 20.0F;
        killTargetLastDistance = 0.0F;
    }

    public static void resetStackMessages() {
        lastStackMessage = null;
        lastStackTime = 0L;
        stackCount = 0;
    }
}