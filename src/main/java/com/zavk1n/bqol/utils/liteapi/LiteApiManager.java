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

    private static final Set<String> manuallyBlockedFeatures = ConcurrentHashMap.newKeySet();

    private static final List<String> FEATURES = List.of(
            /// Better Sprint
            "better_sprint",

            "better_sprint_default",
            "better_sprint_pvp",
            "better_sprint_stair_up",
            "better_sprint_water_sprint",
            "better_sprint_scaffold",

            /// BetterRCT
            "better_rct",

            "better_rct_holyworld",
            "better_rct_reallyworld",

            /// Better Sounds
            "better_sounds",

            /// Better Interact
            "better_interact",

            "better_interact_click_through",
            "better_interact_auto_signs",
            "better_interact_anti_signs",
            "better_interact_safe_harvest",

            /// Better Tnt
            "better_tnt",

            "better_tnt_timer_tnt",
            "better_tnt_alert",
            "better_tnt_alert_show_xyz",
            "better_tnt_alert_show_blocks",
            "better_tnt_explosion_area",

            /// Better TP
            "better_tp",
            "better_tp_auto_accept",
            "better_tp_auto_deny",

            /// Better RRLS
            "better_rrls",

            /// Better Prefixes
            "better_prefixes",

            "better_prefixes_name_protect",
            "better_prefixes_donate",
            "better_prefixes_title",

            /// Better Holograms
            "better_holograms",

            "better_holograms_visible_armor_stand",
            "better_holograms_anti_holograms",

            /// Better Spheres
            "better_spheres",

            "better_spheres_holyworld",
            "better_spheres_holyworld_golden",

            "better_spheres_reallyworld",

            /// Better Sky
            "better_sky",

            /// Shulker Particles
            "better_particles",

            "better_particles_shulkers",
            "better_particles_totems",

            /// Better Fog
            "better_fog",

            "better_fog_color",
            "better_fog_distance",
            "better_fog_no_fog",
            "better_fog_night_vision",
            "better_fog_no_blindness",

            /// Better Health
            "better_health",

            "better_health_hovering",
            "better_health_scaling",
            "better_health_pvp",
            "better_health_decimal",
            "better_health_golden_hearts",
            "better_health_golden_plus",

            /// No Render
            "no_render",

            /// Better Mine
            "better_mine",
            "better_mine_holyworld",
            "better_mine_holyworld_mine_helper"
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

        BQoL.LOGGER.info("LiteApi: sent request id={}, attempt={}", request.id(), attempt + 1);

        return responseFuture
            .orTimeout(5, TimeUnit.SECONDS)
            .handle((response, throwable) -> {

                pending.remove(request.id());

                if (throwable == null) {
                    return response;
                }

                BQoL.LOGGER.warn("LiteApi: request {} timed out (attempt {})", request.id(), attempt + 1);

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
                        BQoL.LOGGER.warn("LiteApi: request rejected ({}){}", response.error(), response.message() != null ? ": " + response.message() : "");
                        return CompletableFuture.completedFuture(response);

                    case "NOT_FOUND":
                        BQoL.LOGGER.error("LiteApi: server does not support checkFeatures()");
                        return CompletableFuture.completedFuture(response);

                    case "INTERNAL_ERROR":
                        if (attempt + 1 >= maxAttempts) {
                            return CompletableFuture.completedFuture(response);
                        }

                        CompletableFuture<CheckFeaturesResponse> retry = new CompletableFuture<>();
                        CompletableFuture
                            .delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
                            .execute(() -> sendRequestInternal(maxAttempts, delayMs, attempt + 1)
                                .thenAccept(retry::complete)
                            );

                        return retry;

                    default:
                        return CompletableFuture.completedFuture(response);
                }
            });
    }

    private static void handleResponse(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            if (root.has("event")) {
                BQoL.LOGGER.debug("LiteApi: ignoring push event '{}'", root.get("event").getAsString());
                return;
            }

            CheckFeaturesResponse response = GSON.fromJson(root, CheckFeaturesResponse.class);
            CompletableFuture<CheckFeaturesResponse> future = pending.remove(response.id());

            if (future == null) {
                BQoL.LOGGER.warn("LiteApi: received response for unknown request id={}", response.id());
                return;
            }

            future.complete(response);

            if (response.isOk()) {
                updateBlocklist(new HashSet<>(response.blocklist()));

                BQoL.LOGGER.info(
                    "LiteApi: received response id={}, blocklist size={}",
                    response.id(),
                    response.blocklist().size()
                );
            } else { BQoL.LOGGER.info("LiteApi: not received responce :(");
            }

            future.complete(response);

        } catch (JsonParseException e) {
            BQoL.LOGGER.warn("LiteApi: failed to parse response: {}", e.getMessage());
        } catch (Exception e) {
            BQoL.LOGGER.error("LiteApi: failed to handle response", e);
        }
    }

    public static void updateBlocklist (Set<String> newBlocked) {
        blockedFeatures = new HashSet<>(newBlocked);

        BQoL.LOGGER.info("LiteApi: blocked features updated: {}", getBlockedFeatures());

        applyBlockingModules();
    }

    public static void manualBlock(String featureId) {
        if (featureId == null || featureId.isBlank()) {
            return;
        }

        manuallyBlockedFeatures.add(featureId);

        BQoL.LOGGER.info("LiteApi: manually blocked feature {}", featureId);

        applyBlockingModules();
    }

    /// Применение блокировок модулей
    private static void applyBlockingModules() {
        BQoLConfig config = BQoLConfig.getInstance();
        MinecraftClient client = MinecraftClient.getInstance();

        /// Better Sprint
        if (isFeatureBlocked("better_sprint")) config.setBetterSprintEnabled(false);

        if (isFeatureBlocked("better_sprint_default")) config.setBetterSprintDefaultMode(false);
        if (isFeatureBlocked("better_sprint_pvp")) config.setBetterSprintPvPMode(false);
        if (isFeatureBlocked("better_sprint_stair_up")) config.setBetterSprintStairUp(false);
        if (isFeatureBlocked("better_sprint_water_sprint")) config.setBetterSprintWaterSprint(false);
        if (isFeatureBlocked("better_sprint_scaffold")) config.setBetterSprintScaffold(false);

        BetterSprint.refreshBlockedStatus();

        /// Better RCT
        if (isFeatureBlocked("better_rct")) config.setBetterRCTEnabled(false);

        if (isFeatureBlocked("better_rct_holyworld")) config.setBetterRCTHolyWorld(false);
        if (isFeatureBlocked("better_rct_reallyworld")) config.setBetterRCTReallyWorld(false);

        BetterRCT.refreshBlockedStatus();

        /// Better Sounds
        if (isFeatureBlocked("better_sounds")) config.setBetterSoundsEnabled(false);

        BetterSounds.refreshBlockedStatus();

        /// Better Interact
        if (isFeatureBlocked("better_interact")) config.setBetterInteractEnabled(false);

        if (isFeatureBlocked("better_interact_click_through")) config.setBetterInteractClickThrough(false);
        if (isFeatureBlocked("better_interact_anti_signs")) config.setBetterInteractAntiSigns(false);
        if (isFeatureBlocked("better_interact_auto_signs")) config.setBetterInteractAutoSigns(false);
        if (isFeatureBlocked("better_interact_safe_harvest")) config.setBetterInteractSafeHarvest(false);

        BetterInteract.refreshBlockedStatus();

        /// Better Tnt
        if (isFeatureBlocked("better_tnt")) config.setBetterTntEnabled(false);

        if (isFeatureBlocked("better_tnt_timer_tnt")) config.setBetterTntTimer(false);
        if (isFeatureBlocked("better_tnt_alert")) config.setBetterTntAlert(false);
        if (isFeatureBlocked("better_tnt_alert_show_xyz")) config.setBetterTntAlertShowXYZ(false);
        if (isFeatureBlocked("better_tnt_alert_show_blocks")) config.setBetterTntAlertShowBlocks(false);
        if (isFeatureBlocked("better_tnt_explosion_area")) config.setBetterTntExplosionArea(false);

        /// Better TP
        if (isFeatureBlocked("better_tp")) config.setBetterTPEnabled(false);

        if (isFeatureBlocked("better_tp_auto_accept")) config.setBetterTPAutoAccept(false);
        if (isFeatureBlocked("better_tp_auto_deny")) config.setBetterTPAutoDeny(false);

        /// Better RRLS
        if (isFeatureBlocked("better_rrls")) config.setBetterRRLSEnabled(false);

        /// Better Prefixes
        if (isFeatureBlocked("better_prefixes")) config.setBetterPrefixesEnabled(false);

        if (isFeatureBlocked("better_prefixes_name_protect")) config.setBetterPrefixesNameProtect(false);
        if (isFeatureBlocked("better_prefixes_donate")) config.setBetterPrefixesDonate(false);
        if (isFeatureBlocked("better_prefixes_title")) config.setBetterPrefixesTitle(false);

        BetterPrefixes.refreshBlockedStatus();

        /// Better Holograms
        if (isFeatureBlocked("better_holograms")) config.setBetterHologramsEnabled(false);

        if (isFeatureBlocked("better_holograms_visible_armor_stand")) config.setBetterHologramsVisibleArmorStand(false);
        if (isFeatureBlocked("better_holograms_anti_holograms")) config.setBetterHologramsAntiHolograms(false);

        BetterHolograms.refreshBlockedStatus();

        /// Better Spheres
        if (isFeatureBlocked("better_spheres")) config.setBetterSpheresEnabled(false);

        if (isFeatureBlocked("better_spheres_holyworld")) {
            config.setHolyWorldSpheresEnabled(false);
            config.setHWGoldenSpheres(false);
        }
        if (isFeatureBlocked("better_spheres_holyworld_golden")) config.setHWGoldenSpheres(false);
        if (isFeatureBlocked("better_spheres_reallyworld")) {
            config.setReallyWorldSpheresEnabled(false);
        }

        BetterSpheres.refreshBlockedStatus();

        /// Better Sky
        if (isFeatureBlocked("better_sky")) config.setBetterSkyEnabled(false);

        BetterSky.refreshBlockedStatus();

        /// Better Particles
        if (isFeatureBlocked("better_particles")) config.setBetterParticlesEnabled(false);

        if (isFeatureBlocked("better_particles_shulkers")) config.setBetterParticlesShulkersEnabled(false);
        if (isFeatureBlocked("better_particles_totems")) config.setBetterParticlesShulkersEnabled(false);

        BetterParticles.refreshBlockedStatus();

        /// Better Fog
        if (isFeatureBlocked("better_fog")) {
            config.setBetterFogEnabled(false);
            if (client != null && client.worldRenderer != null) client.worldRenderer.reload();
        }

        if (isFeatureBlocked("better_fog_color")) config.setBetterFogColor(0x000000);
        if (isFeatureBlocked("better_fog_distance")) config.setBetterFogDistance(16);
        if (isFeatureBlocked("better_fog_no_fog")) config.setBetterFogNoFog(false);
        if (isFeatureBlocked("better_fog_night_vision")) config.setBetterFogNightVision(false);
        if (isFeatureBlocked("better_fog_no_blindness")) config.setBetterFogNoBlindness(false);

        BetterFog.refreshBlockedStatus();

        /// Better Health
        if (isFeatureBlocked("better_health")) {
            config.setBetterHealthEnabled(false);
            BetterHealth.resetDisplay();
        }

        if (isFeatureBlocked("better_health_hovering")) config.setBetterHealthHovering(false);
        if (isFeatureBlocked("better_health_scaling")) config.setBetterHealthScaling(false);
        if (isFeatureBlocked("better_health_pvp")) config.setBetterHealthPvPMode(false);
        if (isFeatureBlocked("better_health_decimal")) config.setBetterHealthDecimal(false);
        if (isFeatureBlocked("better_health_golden_hearts")) {
            config.setBetterHealthGoldenHearts(false);
            config.setBetterHealthGoldenHeartsPlus(false);
        }
        if (isFeatureBlocked("better_health_golden_plus")) config.setBetterHealthGoldenHeartsPlus(false);

        BetterHealth.refreshBlockedStatus();

        /// No Renders
        if (isFeatureBlocked("no_render")) config.setNoRenderEnabled(false);

        NoRender.refreshBlockedStatus();

        /// Better Mine
        if (isFeatureBlocked("better_mine")) config.setBetterMineEnabled(false);
        if (isFeatureBlocked("better_mine_holyworld")) config.setBetterMineHolyWorldEnabled(false);
        if (isFeatureBlocked("better_mine_holyworld_mine_helper")) config.setHolyWorldMineHelperEnabled(false);
    }

    public static boolean isFeatureBlocked(String feature) {
        return blockedFeatures.contains(feature) || manuallyBlockedFeatures.contains(feature);
    }

    public static Set<String> getBlockedFeatures() {
        Set<String> result = new HashSet<>(blockedFeatures);

        result.addAll(manuallyBlockedFeatures);

        return Collections.unmodifiableSet(result);
    }

    public static void reset() {
        pending.clear();

        blockedFeatures.clear();

        applyBlockingModules();

        lastRequestTime = 0;

        BQoL.LOGGER.info("LiteApi: reset");
    }
}