package com.zavk1n.bqol.client;

import com.zavk1n.bqol.client.screen.BQoLConfigScreen;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.features.*;
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
    private static final String menu = "menu";
    private static final String CATEGORY = "category.bqol.keybinds";

    /// Константы
    private static final Map<String, KeyBinding> keybindings = new LinkedHashMap<>();
    private static final BQoLConfig config = BQoLConfig.getInstance();

    private static final String BETTER_SPRINT_TOGGLE = "better_sprint_toggle";
    private static final String BETTER_SOUNDS_TOGGLE = "better_sounds_toggle";
    private static final String BETTER_SPHERES_TOGGLE = "better_spheres_toggle";
    private static final String SHULKER_PARTICLES_TOGGLE = "shulker_particles_toggle";
    private static final String CUSTOM_FOG_TOGGLE = "custom_fog_toggle";
    private static final String CUSTOM_HEALTH_TOGGLE = "custom_health_toggle";
    private static final String NO_RENDER_TOGGLE = "no_render_toggle";

    private static final long SPRINT_TOGGLE_COOLDOWN = 200L;
    private static long lastSprintToggleTime;

    private KeybindManager() { }

    /// Публичные статические методы
    public static void initialize() {
        registerKeybinding(menu, GLFW.GLFW_KEY_RIGHT_SHIFT);

        String[] toggles = {
            BETTER_SPRINT_TOGGLE,
            BETTER_SOUNDS_TOGGLE,
            BETTER_SPHERES_TOGGLE,
            SHULKER_PARTICLES_TOGGLE,
            CUSTOM_FOG_TOGGLE,
            CUSTOM_HEALTH_TOGGLE,
            NO_RENDER_TOGGLE
        };

        for (String toggle : toggles) {
            registerKeybinding(toggle, InputUtil.UNKNOWN_KEY.getCode());
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isWindowFocused(client) || isDangerousCombo(client)) {
                return;
            }

            for (Map.Entry<String, KeyBinding> entry : keybindings.entrySet()) {
                if (entry.getValue().wasPressed()) {
                    handleKeyPress(entry.getKey(), client);
                }
            }
        });
    }

    /// Регистрация биндов
    private static void registerKeybinding(String name, int defaultKey) {
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.bqol." + name,
                InputUtil.Type.KEYSYM,
                defaultKey,
                CATEGORY
            )
        );

        keybindings.put(name, keyBinding);
    }

    private static boolean isWindowFocused(MinecraftClient client) {
        return client.isWindowFocused();
    }

    /// Игнор системных сочетаний клавиш
    private static boolean isDangerousCombo(MinecraftClient client) {
        long window = client.getWindow().getHandle();

        boolean ctrl = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL)
            || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_CONTROL);

        boolean alt = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_ALT)
            || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_ALT);

        boolean shift = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)
            || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);

        boolean win = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SUPER)
            || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SUPER);

        boolean tab = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_TAB);
        boolean esc = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_ESCAPE);
        boolean delete = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_DELETE);
        boolean s = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_S);

        return
            // Ctrl + Alt + Delete
            (ctrl && alt && delete)

                // Ctrl + Shift + Esc
                || (ctrl && shift && esc)

                // Alt + Tab
                || (alt && tab)

                // Shift + Tab
                || (shift && tab)

                // Win + Shift + S
                || (win && shift && s);
    }

    /// Отслеживание нажатий
    private static void handleKeyPress(String keyName, MinecraftClient client) {
        long currentTime = System.currentTimeMillis();

        switch (keyName) {
            case menu -> openConfigScreen(client);

            case BETTER_SPRINT_TOGGLE -> {
                if (currentTime - lastSprintToggleTime < SPRINT_TOGGLE_COOLDOWN) {
                    return;
                }

                lastSprintToggleTime = currentTime;

                toggleFeature(
                    !config.isBetterSprintEnabled(),
                    BetterSprint::setEnabled,
                    config::setBetterSprintEnabled,
                    NotificationManager::showBetterSprintNotification
                );
            }

            case BETTER_SOUNDS_TOGGLE ->
                toggleFeature(
                    !config.isBetterSoundsEnabled(),
                    BetterSounds::setEnabled,
                    config::setBetterSoundsEnabled,
                    NotificationManager::showBetterSoundsNotification
                );

            case BETTER_SPHERES_TOGGLE ->
                toggleFeature(
                    !config.isBetterSpheresEnabled(),
                    BetterSpheres::setEnabled,
                    config::setBetterSpheresEnabled,
                    NotificationManager::showBetterSpheresNotification
                );

            case SHULKER_PARTICLES_TOGGLE ->
                toggleFeature(
                    !config.isShulkerParticlesEnabled(),
                    ShulkerParticles::setEnabled,
                    config::setShulkerParticlesEnabled,
                    NotificationManager::showShulkerParticlesNotification
                );

            case CUSTOM_FOG_TOGGLE ->
                toggleFeature(
                    !config.isCustomFogEnabled(),
                    CustomFog::setEnabled,
                    config::setCustomFogEnabled,
                    NotificationManager::showCustomFogNotification
                );

            case CUSTOM_HEALTH_TOGGLE ->
                toggleFeature(
                    !config.isCustomHealthEnabled(),
                    enabled -> {
                        CustomHealth.setEnabled(enabled);

                        if (!enabled) {
                            CustomHealth.resetDisplay();
                        }
                    },
                    config::setCustomHealthEnabled,
                    NotificationManager::showCustomHealthNotification
                );

            case NO_RENDER_TOGGLE ->
                toggleFeature(
                    !config.isNoRenderEnabled(),
                    NoRender::setEnabled,
                    config::setNoRenderEnabled,
                    NotificationManager::showNoRenderNotification
                );
        }
    }

    private static void toggleFeature(boolean enabled,
        java.util.function.Consumer<Boolean> featureSetter,
        java.util.function.Consumer<Boolean> configSetter,
        java.util.function.Consumer<Boolean> notification
    ) {
        featureSetter.accept(enabled);

        configSetter.accept(enabled);

        config.save();

        notification.accept(enabled);
    }

    private static void openConfigScreen(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        client.setScreen(new BQoLConfigScreen(client.currentScreen));
    }

    public static KeyBinding getKeyBinding(String name) {
        return keybindings.get(name);
    }

    public static Map<String, KeyBinding> getAllKeybindings() {
        return Map.copyOf(keybindings);
    }
}