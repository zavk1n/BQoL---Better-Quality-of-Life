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
import net.minecraft.client.input.KeyInput;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class FeatureKeybindManager {

    private static final String MENU = "menu";
    private static final KeyBinding.Category CATEGORY = new KeyBinding.Category(Identifier.of("bqol", "keybinds"));
    private static final BQoLConfig config = BQoLConfig.getInstance();

    private static final long TOGGLE_COOLDOWN_NANOS = 200_000_000L;

    private static final int KEY_COUNT = GLFW.GLFW_KEY_LAST + 1;

    private record ToggleAction(BooleanSupplier enabledGetter, Consumer<Boolean> featureSetter, Consumer<Boolean> configSetter, Consumer<Boolean> notification) {}

    private static final Map<String, KeyBinding> KEYBINDINGS = new LinkedHashMap<>();

    private static final Map<String, String> FEATURE_KEY_IDS = Map.ofEntries(
        Map.entry("better_movement", "1_better_movement_toggle"),
        Map.entry("better_rct", "2_better_rct_toggle"),
        Map.entry("better_sounds", "3_better_sounds_toggle"),
        Map.entry("better_interact", "4_better_interact_toggle"),
        Map.entry("better_hurt_cam", "5_better_hurt_cam_toggle"),
        Map.entry("better_third_person", "6_better_third_person_toggle"),
        Map.entry("better_tnt", "7_better_tnt_toggle"),
        Map.entry("better_chat", "8_better_chat_toggle"),
        Map.entry("better_tp", "9_better_tp_toggle"),
        Map.entry("better_rrls", "10_better_rrls_toggle"),
        Map.entry("better_prefixes", "11_better_prefixes_toggle"),
        Map.entry("better_holograms", "12_better_holograms_toggle"),
        Map.entry("better_totems", "13_better_totems_toggle"),
        Map.entry("better_spheres", "14_better_spheres_toggle"),
        Map.entry("better_sky", "15_better_sky_toggle"),
        Map.entry("better_particles", "16_better_particles_toggle"),
        Map.entry("better_liquid", "17_better_liquid_toggle"),
        Map.entry("better_fog", "18_better_fog_toggle"),
        Map.entry("better_health", "19_better_health_toggle"),
        Map.entry("no_render", "20_no_render_toggle"),
        Map.entry("better_mine", "21_better_mine_toggle"),
        Map.entry("better_hit_counter", "22_better_hit_counter_toggle")
    );

    private static final String[] TOGGLE_KEYBINDINGS = {
        "1_better_movement_toggle",
        "2_better_rct_toggle",
        "3_better_sounds_toggle",
        "4_better_interact_toggle",
        "5_better_hurt_cam_toggle",
        "6_better_third_person_toggle",
        "7_better_tnt_toggle",
        "8_better_chat_toggle",
        "9_better_tp_toggle",
        "10_better_rrls_toggle",
        "11_better_prefixes_toggle",
        "12_better_holograms_toggle",
        "13_better_totems_toggle",
        "14_better_spheres_toggle",
        "15_better_sky_toggle",
        "16_better_particles_toggle",
        "17_better_liquid_toggle",
        "18_better_fog_toggle",
        "19_better_health_toggle",
        "20_no_render_toggle",
        "21_better_mine_toggle",
        "22_better_hit_counter_toggle"
    };

    private static final Map<String, ToggleAction> TOGGLE_ACTIONS = createToggleActions();

    private static final boolean[] IGNORED_PRESSED_KEYS = new boolean[KEY_COUNT];
    private static final boolean[] PREVIOUS_PRESSED_KEYS = new boolean[KEY_COUNT];
    private static final boolean[] CURRENT_PRESSED_KEYS = new boolean[KEY_COUNT];

    private static String listeningFeature;

    private static boolean hasToggled;

    private static long lastToggleNanos;

    private static boolean initialized;

    private FeatureKeybindManager() {}

    public static void initialize() {
        if (initialized) {
            return;
        }

        registerKeybinding(MENU, GLFW.GLFW_KEY_RIGHT_SHIFT);

        for (String key : TOGGLE_KEYBINDINGS) {
            registerKeybinding(key, InputUtil.UNKNOWN_KEY.getCode());
        }

        ClientTickEvents.END_CLIENT_TICK.register(FeatureKeybindManager::onClientTick);

        initialized = true;
    }

    private static void registerKeybinding(String name, int defaultKey) {
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, defaultKey, CATEGORY));

        KEYBINDINGS.put(name, keyBinding);
    }

    private static void onClientTick(MinecraftClient client) {

        if (listeningFeature != null) {
            handleKeybindListening(client);
            return;
        }

        if (!isWindowFocused(client) || isDangerousCombo(client)) {
            return;
        }

        for (Map.Entry<String, KeyBinding> entry :
            KEYBINDINGS.entrySet()) {

            if (entry.getValue().wasPressed()) {
                handleKeyPress(entry.getKey(), client);
            }
        }
    }

    private static boolean isWindowFocused(MinecraftClient client) {
        return client != null && client.isWindowFocused();
    }

    private static boolean isPressed(long window, int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    private static boolean isDangerousCombo(MinecraftClient client) {
        if (client == null || client.getWindow() == null) {
            return false;
        }

        long window = client.getWindow().getHandle();

        boolean ctrl = isPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL) || isPressed(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
        boolean alt = isPressed(window, GLFW.GLFW_KEY_LEFT_ALT) || isPressed(window, GLFW.GLFW_KEY_RIGHT_ALT);
        boolean shift = isPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT) || isPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        boolean win = isPressed(window, GLFW.GLFW_KEY_LEFT_SUPER) || isPressed(window, GLFW.GLFW_KEY_RIGHT_SUPER);

        if (ctrl && alt && isPressed(window, GLFW.GLFW_KEY_DELETE)
            || ctrl && shift && isPressed(window, GLFW.GLFW_KEY_ESCAPE)
            || alt && isPressed(window, GLFW.GLFW_KEY_TAB)
            || shift && isPressed(window, GLFW.GLFW_KEY_TAB)) {
            return true;
        }

        return win && shift && isPressed(window, GLFW.GLFW_KEY_S);
    }

    private static void handleKeyPress(String keyName, MinecraftClient client) {
        if (MENU.equals(keyName)) {
            openConfigScreen(client);
            return;
        }

        ToggleAction action = TOGGLE_ACTIONS.get(keyName);

        if (action == null) {
            return;
        }

        long currentTime = System.nanoTime();

        if (hasToggled && currentTime - lastToggleNanos < TOGGLE_COOLDOWN_NANOS) {
            return;
        }

        lastToggleNanos = currentTime;
        hasToggled = true;

        boolean enabled = !action.enabledGetter().getAsBoolean();

        toggleFeature(action, enabled);
    }

    private static void toggleFeature(ToggleAction action, boolean enabled) {
        action.featureSetter().accept(enabled);
        action.configSetter().accept(enabled);

        config.save();

        action.notification().accept(enabled);
    }

    private static Map<String, ToggleAction> createToggleActions() {
        Map<String, ToggleAction> actions = new LinkedHashMap<>();

        addAction(actions,
            "1_better_movement_toggle",
            config::isBetterMovementEnabled, BetterMovement::setEnabled,
            config::setBetterMovementEnabled, NotificationManager::showBetterMovementNotification
        );

        addAction(actions,
            "2_better_rct_toggle",
            config::isBetterRCTEnabled, BetterRCT::setEnabled,
            config::setBetterRCTEnabled, NotificationManager::showBetterRCTNotification
        );

        addAction(actions,
            "3_better_sounds_toggle",
            config::isBetterSoundsEnabled, BetterSounds::setEnabled,
            config::setBetterSoundsEnabled, NotificationManager::showBetterSoundsNotification
        );

        addAction(actions,
            "4_better_interact_toggle",
            config::isBetterInteractEnabled, BetterInteract::setEnabled,
            config::setBetterInteractEnabled, NotificationManager::showBetterInteractNotification
        );

        addAction(actions,
            "5_better_hurt_cam_toggle",
            config::isBetterHurtCamEnabled, BetterHurtCam::setEnabled,
            config::setBetterHurtCamEnabled, NotificationManager::showBetterHurtCamNotification
        );

        addAction(
            actions,
            "6_better_third_person_toggle",
            config::isBetterThirdPersonEnabled, BetterThirdPerson::setEnabled,
            config::setBetterThirdPersonEnabled, NotificationManager::showBetterThirdPersonNotification
        );

        addAction(
            actions,
            "7_better_tnt_toggle",
            config::isBetterTntEnabled, BetterTnt::setEnabled,
            config::setBetterTntEnabled, NotificationManager::showBetterTntNotification
        );

        addAction(
            actions,
            "8_better_chat_toggle",
            config::isBetterChatEnabled, BetterChat::setEnabled,
            config::setBetterChatEnabled, NotificationManager::showBetterChatNotification
        );

        addAction(actions,
            "9_better_tp_toggle",
            config::isBetterTPEnabled, BetterTP::setEnabled,
            config::setBetterTPEnabled, NotificationManager::showBetterTPNotification
        );

        addAction(actions,
            "10_better_rrls_toggle",
            config::isBetterRRLSEnabled, BetterRRLS::setEnabled,
            config::setBetterRRLSEnabled, NotificationManager::showBetterRRLSNotification
        );

        addAction(actions,
            "11_better_prefixes_toggle",
            config::isBetterPrefixesEnabled, BetterPrefixes::setEnabled,
            config::setBetterPrefixesEnabled, NotificationManager::showBetterPrefixesNotification
        );

        addAction(actions,
            "12_better_holograms_toggle",
            config::isBetterHologramsEnabled, BetterHolograms::setEnabled,
            config::setBetterHologramsEnabled, NotificationManager::showBetterHologramsNotification
        );

        addAction(actions,
            "13_better_totems_toggle",
            config::isBetterTotemsEnabled, BetterTotems::setEnabled,
            config::setBetterTotemsEnabled, NotificationManager::showBetterTotemsNotification
        );

        addAction(actions,
            "14_better_spheres_toggle",
            config::isBetterSpheresEnabled, BetterSpheres::setEnabled,
            config::setBetterSpheresEnabled, NotificationManager::showBetterSpheresNotification
        );

        addAction(actions,
            "15_better_sky_toggle",
            config::isBetterSkyEnabled, BetterSky::setEnabled,
            config::setBetterSkyEnabled, NotificationManager::showBetterSkyNotification
        );

        addAction(actions,
            "16_better_particles_toggle",
            config::isBetterParticlesEnabled, BetterParticles::setEnabled,
            config::setBetterParticlesEnabled, NotificationManager::showBetterParticlesNotification
        );

        addAction(actions,
            "17_better_liquid_toggle",
            config::isBetterLiquidEnabled, BetterLiquid::setEnabled,
            config::setBetterLiquidEnabled, NotificationManager::showBetterLiquidNotification
        );

        addAction(
            actions,
            "18_better_fog_toggle",
            config::isBetterFogEnabled, BetterFog::setEnabled,
            config::setBetterFogEnabled, NotificationManager::showBetterFogNotification
        );

        addAction(actions,
            "19_better_health_toggle",
            config::isBetterHealthEnabled,
            enabled -> {
                BetterHealth.setEnabled(enabled);

                if (!enabled) {
                    BetterHealth.resetDisplay();
                }
            },
            config::setBetterHealthEnabled,
            NotificationManager::showBetterHealthNotification
        );

        addAction(actions,
            "20_no_render_toggle",
            config::isNoRenderEnabled, NoRender::setEnabled,
            config::setNoRenderEnabled, NotificationManager::showNoRenderNotification
        );

        addAction(actions,
            "21_better_mine_toggle",
            config::isBetterMineEnabled, BetterMine::setEnabled,
            config::setBetterMineEnabled, NotificationManager::showBetterMineNotification
        );

        addAction(actions,
            "22_better_hit_counter_toggle",
            config::isBetterHitCounterEnabled, BetterHitCounter::setEnabled,
            config::setBetterHitCounterEnabled, NotificationManager::showBetterHitCounterNotification
        );

        return Map.copyOf(actions);
    }

    private static void addAction(Map<String, ToggleAction> actions, String key, BooleanSupplier enabledGetter, Consumer<Boolean> featureSetter, Consumer<Boolean> configSetter, Consumer<Boolean> notification) {
        actions.put(key, new ToggleAction(enabledGetter, featureSetter, configSetter, notification));
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
        if (featureId == null) {
            return null;
        }

        String keyId = FEATURE_KEY_IDS.get(featureId);

        return keyId == null ? null : KEYBINDINGS.get(keyId);
    }

    public static KeyBinding getKeyBinding(String name) {
        return KEYBINDINGS.get(name);
    }

    public static Map<String, KeyBinding> getAllKeybindings() {
        return Map.copyOf(KEYBINDINGS);
    }

    public static void startKeybindListening(String featureId) {
        if (getFeatureKeyBinding(featureId) == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.getWindow() == null) {
            return;
        }

        stopKeybindListening();

        long window = client.getWindow().getHandle();

        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            if (isPressed(window, key)) {
                IGNORED_PRESSED_KEYS[key] = true;
                PREVIOUS_PRESSED_KEYS[key] = true;
            }
        }

        listeningFeature = featureId;
    }

    public static boolean isKeybindListening(String featureId) {
        return featureId != null && featureId.equals(listeningFeature);
    }

    public static boolean isKeybindListening() {
        return listeningFeature != null;
    }

    public static void stopKeybindListening() {
        listeningFeature = null;

        Arrays.fill(IGNORED_PRESSED_KEYS, false);
        Arrays.fill(PREVIOUS_PRESSED_KEYS, false);
        Arrays.fill(CURRENT_PRESSED_KEYS, false);
    }

    private static void handleKeybindListening(MinecraftClient client) {
        if (client == null
            || client.getWindow() == null
            || !client.isWindowFocused()) {
            return;
        }

        long window = client.getWindow().getHandle();

        Arrays.fill(CURRENT_PRESSED_KEYS, false);

        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            if (isPressed(window, key)) {
                CURRENT_PRESSED_KEYS[key] = true;
            }
        }

        if (CURRENT_PRESSED_KEYS[GLFW.GLFW_KEY_ESCAPE] && !PREVIOUS_PRESSED_KEYS[GLFW.GLFW_KEY_ESCAPE]) {
            KeyBinding keyBinding = getFeatureKeyBinding(listeningFeature);

            if (keyBinding != null) {
                keyBinding.setBoundKey(InputUtil.UNKNOWN_KEY);
                KeyBinding.updateKeysByCode();

                client.options.write();
            }

            stopKeybindListening();
            return;
        }

        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            if (!CURRENT_PRESSED_KEYS[key] || IGNORED_PRESSED_KEYS[key]) {
                continue;
            }

            if (!PREVIOUS_PRESSED_KEYS[key]) {
                assignKeybind(listeningFeature, key);
                stopKeybindListening();

                return;
            }
        }

        System.arraycopy(CURRENT_PRESSED_KEYS, 0, PREVIOUS_PRESSED_KEYS, 0, KEY_COUNT);
    }

    private static void assignKeybind(String featureId, int glfwKey) {
        MinecraftClient client = MinecraftClient.getInstance();

        KeyBinding keyBinding = getFeatureKeyBinding(featureId);

        if (keyBinding == null) {
            return;
        }

        InputUtil.Key newKey = InputUtil.fromKeyCode(new KeyInput(glfwKey, 0, 0));

        String newTranslationKey = newKey.getTranslationKey();

        for (Map.Entry<String, KeyBinding> entry :
            KEYBINDINGS.entrySet()) {

            if (entry.getValue() == keyBinding) {
                continue;
            }

            KeyBinding otherKeyBinding = entry.getValue();

            if (otherKeyBinding
                .getBoundKeyTranslationKey()
                .equals(newTranslationKey)) {

                otherKeyBinding.setBoundKey(InputUtil.UNKNOWN_KEY);
            }
        }

        keyBinding.setBoundKey(newKey);

        KeyBinding.updateKeysByCode();

        if (client != null) {
            client.options.write();
        }
    }
}