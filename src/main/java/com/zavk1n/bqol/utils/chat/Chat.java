package com.zavk1n.bqol.utils.chat;

import com.zavk1n.bqol.features.rct.BetterRCT;
import com.mojang.brigadier.context.StringRange;

import com.zavk1n.bqol.features.mine.funtime.FunTimeProcess;
import com.zavk1n.bqol.features.mine.holyworld.HolyWorldProcess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public final class Chat {

    private Chat() {}

    public static MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        return client;
    }

    public static MinecraftClient client;

    private FunTimeProcess funTimeProcess;
    private HolyWorldProcess holyWorldProcess;

    private static final String PREFIX = "[BQoL] ";

    private static final List<String> COMMANDS = List.of(
        ".an",
        ".rct",
        ".minehelper",
        ".totemcounter"
    );

    private static final int PREFIX_COLOR = 0xA099FF;

    private static int messageColor() {
        return BetterRCT
            .getInstance()
            .MESSAGE_COLOR;
    }

    public static void info(MinecraftClient mc, String text) {
        send(mc, Text.literal(text).styled(style -> style.withColor(messageColor())));
    }

    public static void info(MinecraftClient mc, Text body) {
        send(mc, body);
    }

    public static void send(MinecraftClient mc, Text body) {
        if (mc == null || mc.player == null) {
            return;
        }

        MutableText message = Text.literal(PREFIX)
            .styled(style -> style
                .withColor(PREFIX_COLOR)
                .withBold(true))
            .append(body);

        mc.player.sendMessage(message, false);
    }

    public static List<String> matching(String input) {
        String typed = input.toLowerCase();

        if (!typed.startsWith(".")) {
            return List.of();
        }

        if (!typed.contains(" ")) {
            return COMMANDS.stream()
                .filter(command -> command.startsWith(typed))
                .map(command -> command.substring(1))
                .toList();
        }

        if (typed.startsWith(".minehelper ")) {
            String[] parts = typed.split(" ", -1);

            if (parts.length == 2) {
                String argument = parts[1];

                return minehelperModes().stream()
                    .filter(suggestion -> suggestion.startsWith(argument) && !suggestion.equals(argument))
                    .toList();
            }
        }

        if (typed.startsWith(".an ")) {
            String[] parts = typed.split(" ", -1);

            if (parts.length == 2) {
                String argument = parts[1];

                return List.of("stop").stream()
                    .filter(suggestion -> suggestion.startsWith(argument) && !suggestion.equals(argument))
                    .toList();
            }
        }

        if (typed.startsWith(".rct ")) {
            String[] parts = typed.split(" ", -1);

            if (parts.length == 2) {
                String argument = parts[1];

                return List.of("stop").stream()
                    .filter(suggestion -> suggestion.startsWith(argument) && !suggestion.equals(argument))
                    .toList();
            }
        }

        if (typed.startsWith(".totemcounter ")) {
            String[] parts = typed.split(" ", -1);

            if (parts.length == 2) {
                String argument = parts[1];

                return List.of("reset").stream()
                    .filter(suggestion -> suggestion.startsWith(argument) && !suggestion.equals(argument))
                    .toList();
            }
        }

        return List.of();
    }

    public static StringRange suggestionRange(String input) {
        String typed = input.toLowerCase();

        if (typed.startsWith(".minehelper ")) {
            int start = input.lastIndexOf(' ') + 1;
            return StringRange.between(start, input.length());
        }

        if (typed.startsWith(".an ")) {
            int start = input.lastIndexOf(' ') + 1;
            return StringRange.between(start, input.length());
        }

        if (typed.startsWith(".rct ")) {
            int start = input.lastIndexOf(' ') + 1;
            return StringRange.between(start, input.length());
        }

        if (typed.startsWith(".totemcounter ")) {
            int start = input.lastIndexOf(' ') + 1;
            return StringRange.between(start, input.length());
        }

        if (typed.startsWith(".")) {
            return StringRange.between(1, input.length());
        }

        return StringRange.between(input.length(), input.length());
    }

    public static Text unknownRoute() {
        return Text.literal("Номер сервера недоступен или не существует.")
            .styled(style -> style
                .withColor(messageColor())
                .withBold(true));
    }

    public static Text invalidNumber() {
        return Text.literal("Вы ввели некорректный номер сервера.")
            .styled(style -> style
                .withColor(messageColor())
                .withBold(true));
    }

    private boolean handleMineHelperCommand(String[] args) {
        if (isFunTime()) {
            if (args.length != 2) {
                Chat.info(mc(), Text.literal("Использование: .minehelper <default>").styled(style -> style.withColor(messageColor())));
                return false;
            }

            FunTimeProcess.Mode mode = FunTimeProcess.Mode.from(args[1]);

            if (mode == null) {
                Chat.info(mc(), Text.literal("Доступные режимы: default").styled(style -> style.withColor(messageColor())));
                return false;
            }

            funTimeProcess.setAnalyzeMode(mode);
            funTimeProcess.start(mode);

            return false;
        }

        if (isHolyWorld()) {
            if (args.length != 2) {
                Chat.info(mc(), Text.literal("Использование: .minehelper <default/nether>").styled(style -> style.withColor(messageColor())));
                return false;
            }

            HolyWorldProcess.Mode mode = HolyWorldProcess.Mode.from(args[1].strip());

            if (mode == null) {
                Chat.info(mc(), Text.literal("Доступные режимы: default, nether").styled(style -> style.withColor(messageColor())));
                return false;
            }

            holyWorldProcess.setAnalyzeMode(mode);
            holyWorldProcess.start(mode);

            return false;
        }

        Chat.info(mc(), Text.literal("Mine Helper доступен только на серверах из модулей Better Mine.").styled(style -> style.withColor(messageColor())));
        return false;
    }

    public static List<String> minehelperModes() {
        if (isHolyWorld()) {
            return List.of("default", "nether");
        }

        if (isFunTime()) {
            return List.of("default");
        }

        return List.of("default");
    }

    public static boolean isHolyWorld() {
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

    public static boolean isFunTime() {
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