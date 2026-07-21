package com.zavk1n.bqol.utils.liteapi;

import com.google.gson.*;
import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.features.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class LiteApiManager {
    private static final Identifier CHANNEL = new Identifier("liteapi", "feature-control");
    private static final String CLIENT_ID = "bqol";
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(CheckFeaturesRequest.class, new FeatureCheckerSerializer())
        .registerTypeAdapter(CheckFeaturesResponse.class, new FeatureCheckerDeserializer())
        .create();

    private static final ConcurrentHashMap<String, CompletableFuture<CheckFeaturesResponse>> pending = new ConcurrentHashMap<>();
    private static Set<String> blockedFeatures = new HashSet<>();

    private static final List<String> FEATURES = List.of(
            /// BetterSprint
            "better_sprint",
            "better_sprint_default",
            "better_sprint_pvp",
            "better_sprint_tree",
            "better_sprint_stair_up",
            "better_sprint_water_sprint",

            /// BetterSounds
            "better_sounds",

            /// BetterSpheres
            "better_spheres",
            "better_spheres_holyworld",
            "better_spheres_parameters",
            "better_spheres_names",
            "better_spheres_golden",

            /// ShulkerParticles
            "shulker_particles",
            "shulker_particles_constant",
            "shulker_particles_breaking",
            "shulker_particles_vanilla_breaking",
            "shulker_particles_constant_dependence",
            "shulker_particles_breaking_dependence",

            /// Custom Fog
            "custom_fog",
            "custom_fog_no_fog",
            "custom_fog_night_vision",
            "custom_fog_biome_fog",

            /// Custom Health
            "custom_health",
            "custom_health_hovering",
            "custom_health_scaling",
            "custom_health_pvp",
            "custom_health_decimal",
            "custom_health_golden_hearts",
            "custom_health_golden_plus",

            /// No Render
            "no_render",
            "no_render_totem_overlay",
            "no_render_fire_overlay",
            "no_render_totem_particles",
            "no_render_potion_particles",
            "no_render_weather",
            "no_render_arrows",
            "no_render_fireworks",
            "no_render_names",
            "no_render_players",
            "no_render_hand",

            /// Better Sky
            "better_sky"
    );

    private static long lastRequestTime = 0;
    private static final long RATE_LIMIT_MS = 10_000;

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL,
                (MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) -> {
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    client.execute(() -> handleResponse(json));
                });
    }

    /// Отправление запроса
    public static CompletableFuture<CheckFeaturesResponse> sendRequest() {
        long now = System.currentTimeMillis();

        if (now - lastRequestTime < RATE_LIMIT_MS) {
            BQoL.LOGGER.warn("LiteApi: rate limited, skipping request");
            return CompletableFuture.completedFuture(null);
        }

        return sendRequestWithRetry(2, RATE_LIMIT_MS);
    }

    private static CompletableFuture<CheckFeaturesResponse> sendRequestWithRetry(int maxAttempts, long delayMs) {
        return sendRequestInternal(maxAttempts, delayMs, 0);
    }

    private static CompletableFuture<CheckFeaturesResponse> sendRequestInternal(int maxAttempts, long delayMs, int attempt) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.getNetworkHandler() == null) {
            BQoL.LOGGER.warn("LiteApi: not connected, skipping request");
            return CompletableFuture.completedFuture(null);
        }

        CheckFeaturesRequest request = new CheckFeaturesRequest(CLIENT_ID, FEATURES);
        CompletableFuture<CheckFeaturesResponse> responseFuture = new CompletableFuture<>();

        pending.put(request.id(), responseFuture);

        String json = GSON.toJson(request);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBytes(json.getBytes(StandardCharsets.UTF_8));

        ClientPlayNetworking.send(CHANNEL, buf);

        lastRequestTime = System.currentTimeMillis();

        BQoL.LOGGER.info(
            "LiteApi: sent request id={}, attempt={}",
            request.id(),
            attempt + 1
        );

        return responseFuture
            .orTimeout(5, TimeUnit.SECONDS)
            .handle((response, throwable) -> {

                pending.remove(request.id());

                if (throwable == null) {
                    return response;
                }

                BQoL.LOGGER.warn(
                    "LiteApi: request {} timed out (attempt {})",
                    request.id(),
                    attempt + 1
                );

                if (attempt + 1 >= maxAttempts) {
                    return null;
                }

                CompletableFuture<CheckFeaturesResponse> retry =
                    new CompletableFuture<>();

                CompletableFuture
                    .delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
                    .execute(() ->
                        sendRequestInternal(
                            maxAttempts,
                            delayMs,
                            attempt + 1
                        ).thenAccept(retry::complete)
                    );

                return retry.join();
            })
            .thenCompose(response -> {

                if (response == null) {
                    return CompletableFuture.completedFuture(null);
                }

                if (response.isOk()) {
                    return CompletableFuture.completedFuture(response);
                }

                switch (response.error()) {

                    case "BAD_REQUEST":
                    case "RATE_LIMITED":
                        BQoL.LOGGER.warn(
                            "LiteApi: request rejected ({}){}",
                            response.error(),
                            response.message() != null
                                ? ": " + response.message()
                                : ""
                        );
                        return CompletableFuture.completedFuture(response);

                    case "NOT_FOUND":
                        BQoL.LOGGER.error(
                            "LiteApi: server does not support checkFeatures()"
                        );
                        return CompletableFuture.completedFuture(response);

                    case "INTERNAL_ERROR":

                        if (attempt + 1 >= maxAttempts) {
                            return CompletableFuture.completedFuture(response);
                        }

                        CompletableFuture<CheckFeaturesResponse> retry = new CompletableFuture<>();

                        CompletableFuture
                            .delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
                            .execute(() ->
                                sendRequestInternal(
                                    maxAttempts,
                                    delayMs,
                                    attempt + 1
                                ).thenAccept(retry::complete)
                            );

                        return retry;

                    default:
                        return CompletableFuture.completedFuture(response);
                }
            });
    }

    /// Обработчик ответов
    private static void handleResponse(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            if (root.has("event")) {
                BQoL.LOGGER.debug(
                    "LiteApi: ignoring push event '{}'",
                    root.get("event").getAsString()
                );
                return;
            }

            CheckFeaturesResponse response = GSON.fromJson(root, CheckFeaturesResponse.class);
            CompletableFuture<CheckFeaturesResponse> future = pending.remove(response.id());

            if (future == null) {
                BQoL.LOGGER.warn(
                    "LiteApi: received response for unknown request id={}",
                    response.id()
                );
                return;
            }

            future.complete(response);

            if (response.isOk()) {
                BQoL.LOGGER.info(
                    "LiteApi: received response id={}, blocklist size={}",
                    response.id(),
                    response.blocklist().size()
                );
            } else {
                if (response.message() != null && !response.message().isBlank()) {
                    BQoL.LOGGER.warn(
                        "LiteApi: server returned {} for request {} ({})",
                        response.error(),
                        response.id(),
                        response.message()
                    );
                } else {
                    BQoL.LOGGER.warn(
                        "LiteApi: server returned {} for request {}",
                        response.error(),
                        response.id()
                    );
                }
            }

        } catch (JsonParseException e) {
            BQoL.LOGGER.warn(
                "LiteApi: failed to parse response: {}",
                e.getMessage()
            );
        } catch (Exception e) {
            BQoL.LOGGER.error(
                "LiteApi: failed to handle response",
                e
            );
        }
    }

    /// Обновление листа
    public static void updateBlocklist(Set<String> newBlocked) {
        blockedFeatures = new HashSet<>(newBlocked);

        BQoL.LOGGER.info(
            "LiteApi: blocked features updated: {}",
            blockedFeatures
        );

        applyBlockingModules();
    }

    /// Запасной ручной блок
    public static void manualBlock(String featureId) {
        if (featureId == null || featureId.isBlank()) {
            return;
        }

        blockedFeatures.add(featureId);

        BQoL.LOGGER.info(
            "LiteApi: manually blocked feature {}",
            featureId
        );

        applyBlockingModules();
    }

    /// Применение блокировок модулей
    private static void applyBlockingModules() {
        BQoLConfig config = BQoLConfig.getInstance();
        MinecraftClient client = MinecraftClient.getInstance();

        /// BetterSprint
        if (isFeatureBlocked("better_sprint")) config.setBetterSprintEnabled(false);
        if (isFeatureBlocked("better_sprint_default")) config.setBetterSprintDefaultMode(false);
        if (isFeatureBlocked("better_sprint_pvp")) config.setBetterSprintPvPMode(false);
        if (isFeatureBlocked("better_sprint_tree")) config.setBetterSprintTreeMode(false);
        if (isFeatureBlocked("better_sprint_stair_up")) config.setBetterSprintStairUp(false);
        if (isFeatureBlocked("better_sprint_water_sprint")) config.setBetterSprintWaterSprint(false);

        BetterSprint.refreshBlockedStatus();

        /// BetterSounds
        if (isFeatureBlocked("better_sounds")) config.setBetterSoundsEnabled(false);

        BetterSounds.refreshBlockedStatus();

        /// BetterSpheres
        if (isFeatureBlocked("better_spheres")) config.setBetterSpheresEnabled(false);
        if (isFeatureBlocked("better_spheres_holyworld")) {
            config.setHolyWorldSpheresEnabled(false);
            config.setColoredParameters(false);
            config.setColoredNames(false);
            config.setGoldenSpheres(false);
        }
        if (isFeatureBlocked("better_spheres_parameters")) config.setColoredParameters(false);
        if (isFeatureBlocked("better_spheres_names")) config.setColoredNames(false);
        if (isFeatureBlocked("better_spheres_golden")) config.setGoldenSpheres(false);

        BetterSpheres.refreshBlockedStatus();

        /// ShulkerParticles
        if (isFeatureBlocked("shulker_particles")) config.setShulkerParticlesEnabled(false);
        if (isFeatureBlocked("shulker_particles_constant")) config.setShulkerParticlesConstant(false);
        if (isFeatureBlocked("shulker_particles_breaking")) config.setShulkerParticlesBreaking(false);
        if (isFeatureBlocked("shulker_particles_vanilla_breaking")) config.setShulkerParticlesVanillaBreaking(false);
        if (isFeatureBlocked("shulker_particles_constant_dependence")) config.setShulkerParticlesConstantDependence(false);
        if (isFeatureBlocked("shulker_particles_breaking_dependence")) config.setShulkerParticlesBreakingDependence(false);

        ShulkerParticles.refreshBlockedStatus();

        /// Custom Fog
        if (isFeatureBlocked("custom_fog")) {
            config.setCustomFogEnabled(false);
            if (client != null && client.worldRenderer != null) client.worldRenderer.reload();
        }

        if (isFeatureBlocked("custom_fog_no_fog")) config.setNoFog(false);
        if (isFeatureBlocked("custom_fog_night_vision")) config.setNightVision(false);
        if (isFeatureBlocked("custom_fog_biome_fog")) config.setBiomeFog(false);

        CustomFog.refreshBlockedStatus();

        /// Custom Health
        if (isFeatureBlocked("custom_health")) {
            config.setCustomHealthEnabled(false);
            CustomHealth.resetDisplay();
        }

        if (isFeatureBlocked("custom_health_hovering")) config.setCustomHealthHovering(false);
        if (isFeatureBlocked("custom_health_scaling")) config.setCustomHealthScaling(false);
        if (isFeatureBlocked("custom_health_pvp")) config.setCustomHealthPvPMode(false);
        if (isFeatureBlocked("custom_health_decimal")) config.setCustomHealthDecimal(false);
        if (isFeatureBlocked("custom_health_golden_hearts")) {
            config.setCustomHealthGoldenHearts(false);
            config.setCustomHealthGoldenHeartsPlus(false);
        }
        if (isFeatureBlocked("custom_health_golden_plus")) config.setCustomHealthGoldenHeartsPlus(false);

        CustomHealth.refreshBlockedStatus();

        /// No Renders
        if (isFeatureBlocked("no_render")) config.setNoRenderEnabled(false);
        if (isFeatureBlocked("no_render_totem_overlay")) config.setNoRenderTotemOverlay(false);
        if (isFeatureBlocked("no_render_fire_overlay")) config.setNoRenderFireOverlayEnabled(false);
        if (isFeatureBlocked("no_render_totem_particles")) config.setNoRenderTotemParticlesEnabled(false);
        if (isFeatureBlocked("no_render_potion_particles")) config.setNoRenderPotionParticlesEnabled(false);
        if (isFeatureBlocked("no_render_weather")) config.setNoRenderWeatherEnabled(false);
        if (isFeatureBlocked("no_render_arrows")) config.setNoRenderArrowsEnabled(false);
        if (isFeatureBlocked("no_render_fireworks")) config.setNoRenderFireworksEnabled(false);
        if (isFeatureBlocked("no_render_names")) config.setNoRenderNamesEnabled(false);
        if (isFeatureBlocked("no_render_players")) config.setNoRenderPlayersEnabled(false);
        if (isFeatureBlocked("no_render_hand")) config.setNoRenderHandEnabled(false);

        NoRender.refreshBlockedStatus();

        /// BetterSky
        if (isFeatureBlocked("better_sky")) config.setBetterSkyEnabled(false);

        BetterSky.refreshBlockedStatus();
    }

    public static boolean isFeatureBlocked(String feature) {
        return blockedFeatures.contains(feature);
    }

    public static Set<String> getBlockedFeatures() {
        return Collections.unmodifiableSet(blockedFeatures);
    }

    /// Утилита сброса
    public static void reset() {
        pending.clear();

        blockedFeatures.clear();

        applyBlockingModules();

        lastRequestTime = 0;

        BQoL.LOGGER.info("LiteApi: reset");
    }
}
// v1.0