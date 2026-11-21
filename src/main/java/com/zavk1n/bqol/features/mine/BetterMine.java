package com.zavk1n.bqol.features.mine;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.features.rct.BetterRCT;
import com.zavk1n.bqol.features.mine.funtime.FunTimeProcess;
import com.zavk1n.bqol.features.mine.holyworld.HolyWorldProcess;
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
        holyWorldProcess = new HolyWorldProcess();
        funTimeProcess = new FunTimeProcess();
    }

    private MinecraftClient client;
    private static BetterMine instance;
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean holyWorld;
        boolean holyWorldMineHelper;
        boolean funTime;
        boolean funTimeMineHelper;
    }

    private final HolyWorldProcess holyWorldProcess;
    private final FunTimeProcess funTimeProcess;

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

    public static boolean isFunTimeEnabled() {
        return instance != null && instance.isFunTimeEnabledInternal();
    }

    public static void setFunTimeEnabled(boolean enabled) {
        if (instance != null) {
            instance.setFunTimeEnabledInternal(enabled);
        }
    }

    public static boolean isFunTimeMineHelperEnabled() {
        return instance != null && instance.isFunTimeMineHelperEnabledInternal();
    }

    public static void setFunTimeMineHelperEnabled(boolean enabled) {
        if (instance != null) {
            instance.setFunTimeMineHelperEnabledInternal(enabled);
        }
    }

    public static HolyWorldProcess getHolyWorldMineHelper() {
        return getInstance().holyWorldProcess;
    }
    public static FunTimeProcess getFunTimeMineHelper() {
        return getInstance().funTimeProcess;
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_mine");
        blocked.holyWorld = LiteApiManager.isFeatureBlocked("better_mine_holyworld");
        blocked.holyWorldMineHelper = LiteApiManager.isFeatureBlocked("better_mine_holyworld_mine_helper");
        blocked.funTime = LiteApiManager.isFeatureBlocked("better_mine_funtime");
        blocked.funTimeMineHelper = LiteApiManager.isFeatureBlocked("better_mine_funtime_mine_helper");
    }

    private boolean isEnabledInternal() {
        return config.isBetterMineEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterMineEnabled(enabled);

        refreshBlockedStatusInternal();

        if (!enabled) {
            holyWorldProcess.stopSilently();
        }
    }

    private boolean isHolyWorldEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterMineHolyWorldEnabled()
            && !blocked.holyWorld;
    }

    private void setHolyWorldEnabledInternal(boolean enabled) {
        config.setBetterMineHolyWorldEnabled(enabled);

        if (!enabled) {
            holyWorldProcess.stopSilently();
        }
    }

    private boolean isHolyWorldMineHelperEnabledInternal() {
        return isHolyWorldEnabledInternal()
            && config.isHolyWorldMineHelperEnabled()
            && !blocked.holyWorldMineHelper;
    }

    private void setHolyWorldMineHelperEnabledInternal(boolean enabled) {
        config.setHolyWorldMineHelperEnabled(enabled);

        if (!enabled) {
            holyWorldProcess.stopSilently();
        }
    }


    private boolean isFunTimeEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterMineFunTimeEnabled()
            && !blocked.funTime;
    }

    private void setFunTimeEnabledInternal(boolean enabled) {
        config.setBetterMineFunTimeEnabled(enabled);

        if (!enabled) {
            funTimeProcess.stopSilently();
        }
    }

    private boolean isFunTimeMineHelperEnabledInternal() {
        return isFunTimeEnabledInternal()
            && config.isFunTimeMineHelperEnabled()
            && !blocked.funTimeMineHelper;
    }

    private void setFunTimeMineHelperEnabledInternal(boolean enabled) {
        config.setFunTimeMineHelperEnabled(enabled);

        if (!enabled) {
            funTimeProcess.stopSilently();
        }
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabledInternal()) {
            holyWorldProcess.stopSilently();
            funTimeProcess.stopSilently();
            return;
        }

        if (isHolyWorld()) {
            if (isHolyWorldMineHelperEnabledInternal()) {
                funTimeProcess.stopSilently();
                holyWorldProcess.tick(mc);
            } else {
                holyWorldProcess.stopSilently();
            }

            funTimeProcess.stopSilently();

        } else if (isFunTime()) {
            if (isFunTimeMineHelperEnabledInternal()) {
                holyWorldProcess.stopSilently();
                funTimeProcess.tick(mc);
            } else {
                funTimeProcess.stopSilently();
            }

            holyWorldProcess.stopSilently();

        } else {
            holyWorldProcess.stopSilently();
            funTimeProcess.stopSilently();
        }
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
        String command = parts[0].toLowerCase(Locale.ROOT);

        if (!command.equals(".minehelper")) {
            return true;
        }

        if (isFunTime()) {
            if (!isFunTimeMineHelperEnabledInternal()) {
                return false;
            }

            return handleMineHelperCommand(parts);
        }

        if (isHolyWorld()) {
            if (!isHolyWorldMineHelperEnabledInternal()) {
                return false;
            }

            return handleMineHelperCommand(parts);
        }

        return true;
    }

    private boolean handleMineHelperCommand(String[] args) {
        if (isFunTime() && !isFunTimeMineHelperEnabledInternal()) {
            return false;
        }

        if (isHolyWorld() && !isHolyWorldMineHelperEnabledInternal()) {
            return false;
        }

        if (isFunTime()) {
            if (args.length != 2) {
                Chat.info(mc(), message("Использование: .minehelper <default>"));
                return false;
            }

            FunTimeProcess.Mode mode = FunTimeProcess.Mode.from(args[1]);

            if (mode == null) {
                Chat.info(mc(), message("Доступные режимы: default"));
                return false;
            }

            funTimeProcess.setAnalyzeMode(mode);
            funTimeProcess.start(mode);

            return false;
        }

        if (isHolyWorld()) {
            if (args.length != 2) {
                Chat.info(mc(), message("Использование: .minehelper <default/nether>"));
                return false;
            }

            HolyWorldProcess.Mode mode = HolyWorldProcess.Mode.from(args[1]);

            if (mode == null) {
                Chat.info(mc(), message("Доступные режимы: default, nether"));
                return false;
            }

            holyWorldProcess.setAnalyzeMode(mode);
            holyWorldProcess.start(mode);

            return false;
        }

        Chat.info(mc(), message("Mine Helper доступен только на серверах из модулей Better Mine."));
        return false;
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

    private boolean isFunTime() {
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
            .contains("funtime");
    }
}