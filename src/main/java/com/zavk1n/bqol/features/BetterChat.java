package com.zavk1n.bqol.features;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BetterChat {
    private BetterChat() {}

    private static BetterChat instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

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

    private static final int HEAD_SIZE = 8;
    private static final int CHAT_PING_COLOR = 0xA099FF;

    private static final long KILL_WINDOW_MS = 8000L;
    private static final long KILL_REMOVE_WINDOW_MS = 2000L;
    private static final float KILL_REMOVE_MAX_HEALTH = 10.0F;
    private static final float KILL_REMOVE_MAX_DISTANCE = 12.0F;

    private static float killTargetLastHealth = 20.0F;
    private static float killTargetLastDistance = 0.0F;

    private static PlayerEntity killTarget = null;
    private static long killTargetTime = 0L;

    private static final int STACK_COLOR = 0x8C8C8C;
    private static final long STACK_INTERVAL_MS = 4000L;

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

    public static void onAttack(PlayerEntity victim) {
        if (!isBetterChatKillMessageEnabled() || victim == null || victim.isDead()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        killTarget = victim;
        killTargetTime = System.currentTimeMillis();
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

        if (System.currentTimeMillis() - killTargetTime > KILL_WINDOW_MS) {
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

        long now = System.currentTimeMillis();

        if (client.player == null
            || client.world == null
            || !isBetterChatKillMessageEnabled()
            || now - killTargetTime > KILL_WINDOW_MS) {
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

            boolean recentHit = now - killTargetTime <= KILL_REMOVE_WINDOW_MS;
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
        if (!isBetterChatPingEnabled() || message == null) {
            return message;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.getNetworkHandler() == null) {
            return message;
        }

        String nickname = client.player.getGameProfile().getName();

        if (nickname == null || nickname.isEmpty()) {
            return message;
        }

        PlayerListEntry sender = findSender(message.getString());

        if (sender == null || sender.getProfile().getId().equals(client.player.getUuid())) {
            return message;
        }

        StringBuilder plain = new StringBuilder();
        List<Style> styles = new ArrayList<>();

        message.visit((style, part) -> {
            plain.append(part);

            for (int i = 0; i < part.length(); i++) {
                styles.add(style);
            }

            return Optional.<Void>empty();

        }, Style.EMPTY);

        if (plain.length() == 0) {
            return message;
        }

        Pattern pattern = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(nickname) + "(?![A-Za-z0-9_])", Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(plain);

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

    private static void appendStyledRange(MutableText result, CharSequence source, List<Style> styles, int start, int end, boolean highlighted) {
        int index = start;

        while (index < end) {
            Style original = styles.get(index);

            int next = index + 1;

            while (next < end && original.equals(styles.get(next))) {
                next++;
            }

            Style finalStyle = original;

            if (highlighted) {
                finalStyle = original
                    .withColor(CHAT_PING_COLOR)
                    .withBold(true);
            }

            result.append(Text.literal(source.subSequence(index, next).toString()).setStyle(finalStyle));

            index = next;
        }
    }

    public static PlayerListEntry findSender(String message) {
        if (!isBetterChatHeadsEnabled() || message == null || message.isEmpty()) {
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

            String nickname = player.getProfile().getName();

            if (nickname == null || nickname.isEmpty()) {
                continue;
            }

            Pattern pattern = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(nickname) + "(?![A-Za-z0-9_])", Pattern.CASE_INSENSITIVE);

            Matcher matcher = pattern.matcher(clean);

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
        }

        return bestMatch;
    }

    private static String cleanChatMessage(String message) {
        return message
            .replaceAll("(?i)§x(§[0-9a-f]){6}", "")
            .replaceAll("(?i)§[0-9a-fk-or]", "")
            .trim();
    }

    private static boolean isValidChatSender(String message, int start, int end) {
        if (start < 0 || end > message.length()) {
            return false;
        }

        String prefix = message.substring(0, start);
        String remaining = message.substring(end);

        int colon = remaining.indexOf(':');

        if (colon >= 0 && colon <= 40) {
            return true;
        }

        if (start > 0
            && message.charAt(start - 1) == '<'
            && end < message.length()
            && message.charAt(end) == '>') {
            return true;
        }

        boolean validPrefix = prefix.matches("(?:\\s*(?:\\[[^\\]]*\\]|\\{[^}]*\\}):?)*\\s*");
        boolean validAfter = !remaining.isEmpty() && Character.isWhitespace(remaining.charAt(0));

        return validPrefix && validAfter;
    }

    public static boolean renderHeadForMessage(DrawContext context, int x, int y, String message, float opacity) {
        if (!isBetterChatHeadsEnabled() || context == null || message == null) {
            return false;
        }

        PlayerListEntry sender = findSender(message);

        if (sender != null) {
            if (drawHeadTexture(context, x, y, sender.getSkinTexture(), opacity)) {
                return true;
            }
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null) {
            String localNickname = client.player.getGameProfile().getName();

            Pattern pattern = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(localNickname) + "(?![A-Za-z0-9_])", Pattern.CASE_INSENSITIVE);

            Matcher matcher = pattern.matcher(cleanChatMessage(message));

            while (matcher.find()) {
                if (isValidChatSender(cleanChatMessage(message), matcher.start(), matcher.end())) {
                    return drawHeadTexture(context, x, y, client.player.getSkinTexture(), opacity);
                }
            }
        }

        return false;
    }

    private static boolean drawHeadTexture(DrawContext context, int x, int y, Identifier skin, float opacity) {
        if (context == null || skin == null) {
            return false;
        }

        opacity = Math.max(0.0F, Math.min(1.0F, opacity));

        if (opacity <= 0.0F) {
            return false;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        try {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);

            context.drawTexture(skin, x, y, 8.0F, 8.0F, HEAD_SIZE, HEAD_SIZE, 64, 64);
            context.drawTexture(skin, x, y, 40.0F, 8.0F, HEAD_SIZE, HEAD_SIZE, 64, 64);

            return true;
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }

    public static boolean processStackMessage(ChatHud hud, Text message, MessageSignatureData signature, MessageIndicator indicator) {
        if (stackUpdating || message == null) {
            return false;
        }

        if (!isBetterChatStackMessagesEnabled()) {
            resetStackMessages();
            return false;
        }

        long now = System.currentTimeMillis();

        boolean sameMessage = lastStackMessage != null && Objects.equals(lastStackMessage, message);
        boolean withinInterval = now - lastStackTime < STACK_INTERVAL_MS;

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

    public static boolean isStackUpdating() {
        return stackUpdating;
    }

    private static boolean removeLastStackMessage(ChatHud hud) {
        return ((ChatStackAccess) hud).bqol$removeLastStackMessage();
    }

    public interface ChatStackAccess {
        boolean bqol$removeLastStackMessage();
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

    public static void resetKillTarget() {
        killTarget = null;
        killTargetTime = 0L;
    }

    public static void resetStackMessages() {
        lastStackMessage = null;
        lastStackTime = 0L;
        stackCount = 0;
    }
}