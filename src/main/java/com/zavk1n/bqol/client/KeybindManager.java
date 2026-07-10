package com.zavk1n.bqol.client;

import com.zavk1n.bqol.client.screen.BQoLConfigScreen;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.features.BetterSprint;
import com.zavk1n.bqol.utils.NotificationManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public final class KeybindManager {
    private static final String MENU = "menu";
    private static final String BETTER_SPRINT_TOGGLE = "better_sprint_toggle";

    private static final Map<String, KeyBinding> keybindings = new LinkedHashMap<>();
    private static final BQoLConfig config = BQoLConfig.getInstance();

    private static final long SPRINT_TOGGLE_COOLDOWN = 200L;
    private static long lastSprintToggleTime;

    private KeybindManager() { }

    /// Публичные статические методы
    public static void initialize() {
        registerKeybinding(MENU, GLFW.GLFW_KEY_V, "category.bqol.main");
        registerKeybinding(BETTER_SPRINT_TOGGLE, GLFW.GLFW_KEY_G, "category.bqol.features");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Map.Entry<String, KeyBinding> entry : keybindings.entrySet()) {
                if (entry.getValue().wasPressed()) {
                    handleKeyPress(entry.getKey(), client);
                }
            }
        });
    }

    /// Регистрация биндов
    private static void registerKeybinding(String name, int defaultKey, String category) {
        KeyBinding keyBinding = new KeyBinding(
            "key.bqol." + name,
            InputUtil.Type.KEYSYM,
            defaultKey,
            category
        );

        KeyBindingHelper.registerKeyBinding(keyBinding);
        keybindings.put(name, keyBinding);
    }

    private static boolean isWindowFocused(MinecraftClient client) {
        return client.isWindowFocused();
    }

    /// Анти-комбо
    private static boolean isDangerousCombo(MinecraftClient client) {
        long window = client.getWindow().getHandle();

        boolean ctrlLeft = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL);
        boolean altLeft = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_ALT);
        boolean altRight = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_ALT);
        boolean shiftLeft = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT);
        boolean delete = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_DELETE);
        boolean esc = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_ESCAPE);
        boolean tab = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_TAB);

        return (ctrlLeft && altLeft && delete)
            || (ctrlLeft && altRight && delete)
            || (ctrlLeft && shiftLeft && esc)
            || (altLeft && tab)
            || (altRight && tab);
    }

    /// Отслеживание нажатий
    private static void handleKeyPress(String keyName, MinecraftClient client) {
        if (!isWindowFocused(client)) {
            return;
        }

        if (isDangerousCombo(client)) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        switch (keyName) {
            case MENU -> openConfigScreen(client);

            case BETTER_SPRINT_TOGGLE -> {
                if (currentTime - lastSprintToggleTime < SPRINT_TOGGLE_COOLDOWN) {
                    return;
                }

                lastSprintToggleTime = currentTime;

                boolean enabled = !config.isBetterSprintEnabled();

                BetterSprint.setEnabled(enabled);
                config.save();

                NotificationManager.showBetterSprintNotification(enabled);
            }
        }
    }

    private static void openConfigScreen(MinecraftClient client) {
        if (client.world != null && client.player != null) {
            client.setScreen(new BQoLConfigScreen(client.currentScreen));
        }
    }

    public static KeyBinding getKeyBinding(String name) {
        return keybindings.get(name);
    }

    public static Map<String, KeyBinding> getAllKeybindings() {
        return Map.copyOf(keybindings);
    }
}