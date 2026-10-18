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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class KeybindManager {

    private static final String menu = "menu";
    private static final String CATEGORY = "category.bqol.keybinds";

    private static final Map<String, KeyBinding> keybindings = new LinkedHashMap<>();

    private static final BQoLConfig config = BQoLConfig.getInstance();

    private static final String BETTER_MOVEMENT_TOGGLE = "1_better_movement_toggle";
    private static final String BETTER_RCT_TOGGLE = "2_better_rct_toggle";
    private static final String BETTER_SOUNDS_TOGGLE = "3_better_sounds_toggle";
    private static final String BETTER_INTERACT_TOGGLE = "4_better_interact_toggle";
    private static final String BETTER_HURT_CAM_TOGGLE = "5_better_interact_toggle";
    private static final String BETTER_THIRD_PERSON_TOGGLE = "6_better_interact_toggle";
    private static final String BETTER_TNT_TOGGLE = "7_better_tnt_toggle";
    private static final String BETTER_TP_TOGGLE = "8_better_tp_toggle";
    private static final String BETTER_RRLS_TOGGLE = "9_better_rrls_toggle";
    private static final String BETTER_PREFIXES_TOGGLE = "10_better_prefixes_toggle";
    private static final String BETTER_HOLOGRAMS_TOGGLE = "11_better_holograms_toggle";
    private static final String BETTER_SPHERES_TOGGLE = "12_better_spheres_toggle";
    private static final String BETTER_SKY_TOGGLE = "13_better_sky_toggle";
    private static final String BETTER_PARTICLES_TOGGLE = "14_better_particles_toggle";
    private static final String BETTER_FOG_TOGGLE = "15_better_fog_toggle";
    private static final String BETTER_HEALTH_TOGGLE = "16_better_health_toggle";
    private static final String NO_RENDER_TOGGLE = "17_no_render_toggle";
    private static final String BETTER_MINE_TOGGLE = "18_better_mine_toggle";

    private static final long TOGGLE_COOLDOWN = 200L;

    private static long toggleTime;
    private static String listeningFeature = null;

    private static final Set<Integer> ignoredPressedKeys = new HashSet<>();
    private static final Set<Integer> previousPressedKeys = new HashSet<>();

    private KeybindManager() {}

    public static void initialize() {
        registerKeybinding(menu, GLFW.GLFW_KEY_RIGHT_SHIFT);

        String[] toggles = {
            BETTER_MOVEMENT_TOGGLE,
            BETTER_RCT_TOGGLE,
            BETTER_SOUNDS_TOGGLE,
            BETTER_INTERACT_TOGGLE,
            BETTER_HURT_CAM_TOGGLE,
            BETTER_THIRD_PERSON_TOGGLE,
            BETTER_TNT_TOGGLE,
            BETTER_TP_TOGGLE,
            BETTER_RRLS_TOGGLE,
            BETTER_PREFIXES_TOGGLE,
            BETTER_HOLOGRAMS_TOGGLE,
            BETTER_SPHERES_TOGGLE,
            BETTER_SKY_TOGGLE,
            BETTER_PARTICLES_TOGGLE,
            BETTER_FOG_TOGGLE,
            BETTER_HEALTH_TOGGLE,
            NO_RENDER_TOGGLE,
            BETTER_MINE_TOGGLE
        };

        for (String toggle : toggles) {
            registerKeybinding(toggle, InputUtil.UNKNOWN_KEY.getCode());
        }

        ClientTickEvents.END_CLIENT_TICK.register(
            client -> {
                if (listeningFeature != null) {
                    handleKeybindListening(client);
                    return;
                }

                if (!isWindowFocused(client) || isDangerousCombo(client)) {
                    return;
                }

                for (Map.Entry<String, KeyBinding> entry : keybindings.entrySet()) {
                    if (entry.getValue().wasPressed()) {
                        handleKeyPress(entry.getKey(), client);
                    }
                }
            }
        );
    }

    private static void registerKeybinding(String name, int defaultKey) {
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, defaultKey, CATEGORY));

        keybindings.put(name, keyBinding);
    }

    private static boolean isWindowFocused(MinecraftClient client) {
        return client != null && client.isWindowFocused();
    }

    private static boolean isDangerousCombo(MinecraftClient client) {
        if (client == null || client.getWindow() == null) {
            return false;
        }

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

        return (ctrl && alt && delete)
                || (ctrl && shift && esc)
                || (alt && tab)
                || (shift && tab)
                || (win && shift && s);
    }

    private static void handleKeyPress(String keyName, MinecraftClient client) {
        long currentTime = System.currentTimeMillis();

        switch (keyName) {
            case menu -> openConfigScreen(client);

            case BETTER_MOVEMENT_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterMovementEnabled(),
                    BetterMovement::setEnabled,
                    config::setBetterMovementEnabled,
                    NotificationManager::showBetterMovementNotification
                );
            }

            case BETTER_RCT_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterRCTEnabled(),
                    BetterRCT::setEnabled,
                    config::setBetterRCTEnabled,
                    NotificationManager::showBetterRCTNotification
                );
            }

            case BETTER_SOUNDS_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterSoundsEnabled(),
                    BetterSounds::setEnabled,
                    config::setBetterSoundsEnabled,
                    NotificationManager::showBetterSoundsNotification
                );
            }

            case BETTER_INTERACT_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterInteractEnabled(),
                    BetterInteract::setEnabled,
                    config::setBetterInteractEnabled,
                    NotificationManager::showBetterInteractNotification
                );
            }

            case BETTER_HURT_CAM_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterHurtCamEnabled(),
                    BetterHurtCam::setEnabled,
                    config::setBetterHurtCamEnabled,
                    NotificationManager::showBetterHurtCamNotification
                );
            }

            case BETTER_THIRD_PERSON_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterThirdPersonEnabled(),
                    BetterThirdPerson::setEnabled,
                    config::setBetterThirdPersonEnabled,
                    NotificationManager::showBetterThirdPersonNotification
                );
            }

            case BETTER_TNT_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterTntEnabled(),
                    BetterTnt::setEnabled,
                    config::setBetterTntEnabled,
                    NotificationManager::showBetterTntNotification
                );
            }

            case BETTER_TP_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterTPEnabled(),
                    BetterTP::setEnabled,
                    config::setBetterTPEnabled,
                    NotificationManager::showBetterTPNotification
                );
            }

            case BETTER_RRLS_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterRRLSEnabled(),
                    BetterRRLS::setEnabled,
                    config::setBetterRRLSEnabled,
                    NotificationManager::showBetterRRLSNotification
                );
            }

            case BETTER_PREFIXES_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterPrefixesEnabled(),
                    BetterPrefixes::setEnabled,
                    config::setBetterPrefixesEnabled,
                    NotificationManager::showBetterPrefixesNotification
                );
            }

            case BETTER_HOLOGRAMS_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterHologramsEnabled(),
                    BetterHolograms::setEnabled,
                    config::setBetterHologramsEnabled,
                    NotificationManager::showBetterHologramsNotification
                );
            }

            case BETTER_SPHERES_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterSpheresEnabled(),
                    BetterSpheres::setEnabled,
                    config::setBetterSpheresEnabled,
                    NotificationManager::showBetterSpheresNotification
                );
            }

            case BETTER_SKY_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterSkyEnabled(),
                    BetterSky::setEnabled,
                    config::setBetterSkyEnabled,
                    NotificationManager::showBetterSkyNotification
                );
            }

            case BETTER_PARTICLES_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterParticlesEnabled(),
                    BetterParticles::setEnabled,
                    config::setBetterParticlesEnabled,
                    NotificationManager::showBetterParticlesNotification
                );
            }

            case BETTER_FOG_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterFogEnabled(),
                    BetterFog::setEnabled,
                    config::setBetterFogEnabled,
                    NotificationManager::showBetterFogNotification
                );
            }

            case BETTER_HEALTH_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterHealthEnabled(),
                    enabled -> {
                        BetterHealth.setEnabled(enabled);
                        if (!enabled) {
                            BetterHealth.resetDisplay();
                        }
                    },

                    config::setBetterHealthEnabled,
                    NotificationManager::showBetterHealthNotification
                );
            }

            case NO_RENDER_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isNoRenderEnabled(),
                    NoRender::setEnabled,
                    config::setNoRenderEnabled,
                    NotificationManager::showNoRenderNotification
                );
            }

            case BETTER_MINE_TOGGLE -> {
                if (currentTime - toggleTime < TOGGLE_COOLDOWN) {
                    return;
                }

                toggleTime = currentTime;

                toggleFeature(
                    !config.isBetterMineEnabled(),
                    BetterMine::setEnabled,
                    config::setBetterMineEnabled,
                    NotificationManager::showBetterMineNotification
                );
            }
        }
    }

    private static void toggleFeature(boolean enabled,
        java.util.function.Consumer<Boolean> featureSetter,
        java.util.function.Consumer<Boolean> configSetter,
        java.util.function.Consumer<Boolean> notification) {
        featureSetter.accept(enabled);

        configSetter.accept(enabled);

        config.save();

        notification.accept(enabled);
    }

    private static void openConfigScreen(MinecraftClient client) {
        if (client == null
            || client.player == null
            || client.world == null) {
            return;
        }

        client.setScreen(new BQoLConfigScreen(client.currentScreen));
    }

    public static KeyBinding getFeatureKeyBinding(String featureId) {
        return switch (featureId) {

            case "better_movement" -> keybindings.get(BETTER_MOVEMENT_TOGGLE);
            case "better_rct" -> keybindings.get(BETTER_RCT_TOGGLE);
            case "better_sounds" -> keybindings.get(BETTER_SOUNDS_TOGGLE);
            case "better_interact" -> keybindings.get(BETTER_INTERACT_TOGGLE);
            case "better_hurt_cam" -> keybindings.get(BETTER_HURT_CAM_TOGGLE);
            case "better_third_person" -> keybindings.get(BETTER_THIRD_PERSON_TOGGLE);
            case "better_tnt" -> keybindings.get(BETTER_TNT_TOGGLE);
            case "better_tp" -> keybindings.get(BETTER_TP_TOGGLE);
            case "better_rrls" -> keybindings.get(BETTER_RRLS_TOGGLE);
            case "better_prefixes" -> keybindings.get(BETTER_PREFIXES_TOGGLE);
            case "better_holograms" -> keybindings.get(BETTER_HOLOGRAMS_TOGGLE);
            case "better_spheres" -> keybindings.get(BETTER_SPHERES_TOGGLE);
            case "better_sky" -> keybindings.get(BETTER_SKY_TOGGLE);
            case "better_particles" -> keybindings.get(BETTER_PARTICLES_TOGGLE);
            case "better_fog" -> keybindings.get(BETTER_FOG_TOGGLE);
            case "better_health" -> keybindings.get(BETTER_HEALTH_TOGGLE);
            case "no_render" -> keybindings.get(NO_RENDER_TOGGLE);
            case "better_mine" -> keybindings.get(BETTER_MINE_TOGGLE);

            default -> null;
        };
    }

    public static void startKeybindListening(String featureId) {
        if (getFeatureKeyBinding(featureId) == null) {
            return;
        }

        listeningFeature = featureId;

        ignoredPressedKeys.clear();
        previousPressedKeys.clear();

        MinecraftClient client =
            MinecraftClient.getInstance();

        if (client == null || client.getWindow() == null) {
            return;
        }

        long window = client.getWindow().getHandle();

        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            if (GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS) {
                ignoredPressedKeys.add(key);
                previousPressedKeys.add(key);
            }
        }
    }

    public static boolean isKeybindListening(String featureId) {
        return featureId != null && featureId.equals(listeningFeature);
    }

    public static boolean isKeybindListening() {
        return listeningFeature != null;
    }

    public static void stopKeybindListening() {
        listeningFeature = null;

        ignoredPressedKeys.clear();
        previousPressedKeys.clear();
    }

    private static void handleKeybindListening(MinecraftClient client) {
        if (listeningFeature == null) {
            return;
        }

        if (client == null
            || client.getWindow() == null
            || !client.isWindowFocused()) {
            return;
        }

        long window = client.getWindow().getHandle();

        Set<Integer> currentlyPressed = new HashSet<>();

        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            if (GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS) {
                currentlyPressed.add(key);
            }
        }

        if (currentlyPressed.contains(GLFW.GLFW_KEY_ESCAPE)
            && !previousPressedKeys.contains(GLFW.GLFW_KEY_ESCAPE)) {

            KeyBinding keyBinding = getFeatureKeyBinding(listeningFeature);

            if (keyBinding != null) {
                keyBinding.setBoundKey(InputUtil.UNKNOWN_KEY);
                KeyBinding.updateKeysByCode();

                MinecraftClient.getInstance().options.write();
            }

            stopKeybindListening();
            return;
        }

        for (int key : currentlyPressed) {
            if (ignoredPressedKeys.contains(key)) {
                continue;
            }

            if (!previousPressedKeys.contains(key)) {
                assignKeybind(listeningFeature, key);

                stopKeybindListening();
                return;
            }
        }

        previousPressedKeys.clear();
        previousPressedKeys.addAll(currentlyPressed);
    }

    private static void assignKeybind(String featureId, int glfwKey) {
        MinecraftClient client = MinecraftClient.getInstance();
        KeyBinding keyBinding = getFeatureKeyBinding(featureId);

        if (keyBinding == null) {
            return;
        }

        InputUtil.Key newKey = InputUtil.fromKeyCode(glfwKey, 0);

        String newTranslationKey = newKey.getTranslationKey();

        for (Map.Entry<String, KeyBinding> entry : keybindings.entrySet()) {
            String otherFeature = entry.getKey();
            KeyBinding otherKeyBinding = entry.getValue();

            if (otherFeature.equals(featureId)) {
                continue;
            }

            String otherTranslationKey = otherKeyBinding.getBoundKeyTranslationKey();

            if (otherTranslationKey.equals(newTranslationKey)) {
                otherKeyBinding.setBoundKey(InputUtil.UNKNOWN_KEY);
            }
        }

        keyBinding.setBoundKey(newKey);
        KeyBinding.updateKeysByCode();

        if (client != null) {
            client.options.write();
        }
    }

    public static KeyBinding getKeyBinding(String name) {
        return keybindings.get(name);
    }

    public static Map<String, KeyBinding> getAllKeybindings() {
        return Map.copyOf(keybindings);
    }
}