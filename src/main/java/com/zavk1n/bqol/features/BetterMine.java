package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.bettermine.MineHelperController;
import com.zavk1n.bqol.utils.chat.Chat;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.util.Locale;

@Environment(EnvType.CLIENT)
public class BetterMine {
    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        return client;
    }

    private BetterMine() {
        mineHelperController = new MineHelperController();
    }

    private MinecraftClient client;
    private static BetterMine instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean holyworld;
        boolean hwminehelper;
    }

    private final MineHelperController mineHelperController;

    public static void initialize() {
        if (instance != null) {
            return;
        }
        instance = new BetterMine();
        instance.refreshBlockedStatusInternal();
        BQoL.LOGGER.info("Better Mine initialized");
    }

    public static BetterMine getInstance() {
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

    public static boolean isHolyWorldEnabled() {
        return instance != null && instance.isHolyWorldEnabledInternal();
    }

    public static void setHolyWorldEnabled(boolean enabled) {
        if (instance != null) {
            instance.setHolyWorldEnabledInternal(enabled);
        }
    }

    public static boolean isHolyWorldMineHelperEnabled() {
        return instance != null && instance.isHolyWorldMineHelperEnabledInternal();
    }

    public static void setHolyWorldMineHelperEnabled(boolean enabled) {
        if (instance != null) {
            instance.setHolyWorldMineHelperEnabledInternal(enabled);
        }
    }

    public static MineHelperController getMineHelperController() {
        return getInstance().mineHelperController;
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_mine");
        blocked.holyworld = LiteApiManager.isFeatureBlocked("better_mine_holyworld");
        blocked.hwminehelper = LiteApiManager.isFeatureBlocked("better_mine_holyworld_mine_helper");
    }

    private boolean isEnabledInternal() {
        return config.isBetterRCTEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterRCTEnabled(enabled);

        refreshBlockedStatusInternal();

        if (!enabled) {
            mineHelperController.stopSilently();
        }
    }

    private boolean isHolyWorldEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterMineHolyWorldEnabled()
            && !blocked.holyworld
            && isHolyWorld();
    }

    private void setHolyWorldEnabledInternal(boolean enabled) {
        config.setBetterMineHolyWorldEnabled(enabled);

        if (!enabled) {
            mineHelperController.stopSilently();
        }
    }

    private boolean isHolyWorldMineHelperEnabledInternal() {
        return isEnabledInternal()
            && config.isHolyWorldMineHelperEnabled()
            && !blocked.hwminehelper
            && isHolyWorld();
    }

    private void setHolyWorldMineHelperEnabledInternal(boolean enabled) {
        config.setHolyWorldMineHelperEnabled(enabled);

        if (!enabled) {
            mineHelperController.stopSilently();
        }
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabledInternal()) {
            return;
        }

        mineHelperController.tick(mc);
    }

    public boolean onChat(String message) {
        if (!isEnabledInternal()) {
            return true;
        }

        return handleCommand(message);
    }

    public boolean onCommand(String command) {
        if (!isEnabledInternal()) {
            return true;
        }

        return handleCommand("/" + command);
    }

    private boolean handleCommand(String message) {
        String trimmed = message.trim();

        if (trimmed.isEmpty()) {
            return true;
        }

        String[] parts = trimmed.split("\\s+");

        if (parts.length == 0) {
            return true;
        }

        String command = parts[0].toLowerCase(Locale.ROOT);

        if (command.equals(".minehelper")) {
            return handleMineHelperCommand(parts);
        }

        return true;
    }

    private boolean handleMineHelperCommand(String[] args) {
        if (args.length != 2) {
            sendMineHelperUsage();
            return false;
        }

        MineHelperController.Mode mode = MineHelperController.Mode.from(args[1]);

        if (mode == null) {
            Chat.info(mc(), message("Доступные режимы: default, nether"));
            return false;
        }

        if (!isHolyWorldEnabledInternal()) {
            Chat.info(mc(), message("Mine Helper доступен только на HolyWorld."));
            return false;
        }

        mineHelperController.setAnalyzeMode(mode);
        mineHelperController.start(mode);

        return false;
    }

    private void sendMineHelperUsage() {
        Chat.info(mc(), message("Использование: .minehelper <default/nether>"));
    }

    private static Text message(String text) {
        return Text.literal(text)
            .styled(style -> style
                .withColor(BetterRCT.getInstance().MESSAGE_COLOR)
                .withBold(true));
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
}