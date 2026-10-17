package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class BetterTP {
    private BetterTP() {}

    private static BetterTP instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean autoAccept;
        boolean autoDeny;
    }

    private static final String TP_REQUEST_MESSAGE = "Заявка будет автоматически отменена через 60 секунд.";

    public static void initialize() {
        if (instance == null) {
            instance = new BetterTP();
            instance.refreshBlockedStatusInternal();
            instance.registerEvents();
            BQoL.LOGGER.info("Better TP initialized");
        }
    }

    public static BetterTP getInstance() {
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

    public static boolean isAutoAcceptEnabled() {
        return instance != null && instance.isAutoAcceptEnabledInternal();
    }

    public static void setAutoAcceptEnabled(boolean enabled) {
        if (instance != null) instance.setAutoAcceptEnabledInternal(enabled);
    }

    public static boolean isAutoDenyEnabled() {
        return instance != null && instance.isAutoDenyEnabledInternal();
    }

    public static void setAutoDenyEnabled(boolean enabled) {
        if (instance != null) instance.setAutoDenyEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_tp");
        blocked.autoAccept = LiteApiManager.isFeatureBlocked("better_tp_auto_accept");
        blocked.autoDeny = LiteApiManager.isFeatureBlocked("better_tp_auto_deny");
    }

    private boolean isEnabledInternal() {
        return config.isBetterTPEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterTPEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    private boolean isAutoAcceptEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterTPAutoAccept()
            && !blocked.autoAccept;
    }

    private void setAutoAcceptEnabledInternal(boolean enabled) {
        config.setBetterTPAutoAccept(enabled);
    }

    private boolean isAutoDenyEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterTPAutoDeny()
            && !blocked.autoDeny;
    }

    private void setAutoDenyEnabledInternal(boolean enabled) {
        config.setBetterTPAutoDeny(enabled);
    }

    private void registerEvents() {
        ClientReceiveMessageEvents.GAME.register(
            (message, overlay) -> {
                if (overlay) {
                    return;
                }

                handleChatMessage(message);
            }
        );
    }

    private void handleChatMessage(Text message) {
        if (!isEnabledInternal() || message == null) {
            return;
        }

        String content = message.getString();

        if (!content.contains(TP_REQUEST_MESSAGE)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }

        if (isAutoDenyEnabledInternal()) {
            sendCommand("tpdeny");
            return;
        }

        if (isAutoAcceptEnabledInternal()) {
            sendCommand("tpaccept");
        }
    }

    private void sendCommand(String command) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }

        client.getNetworkHandler().sendChatCommand(command);
    }
}