package com.zavk1n.bqol.utils.liteapi.restapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.utils.rct.HolyWorldController;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HWServers {
    private HWServers() {}

    private static final String API_URL = "https://api.holyworld.me/v1/servers";

    private static final Pattern SERVER_ID = Pattern.compile("^LITE_ANARCHY_(\\d+)$");
    private static final Pattern SERVER_NAME = Pattern.compile("#\\s*(\\d+)");

    private static final long UPDATE_INTERVAL_MS = 600_000L;
    private static final long RETRY_INTERVAL_MS = 60_000L;

    private static final HWServers instance = new HWServers();
    private final AtomicBoolean updating = new AtomicBoolean(false);
    private volatile Map<Integer, Server> servers = Collections.emptyMap();

    private volatile boolean loaded;
    private volatile long nextUpdate;

    public static HWServers getInstance() {
        return instance;
    }

    public static final class Server {
        public final int number;
        public final String name;
        public final HolyWorldController.LiteMode mode;

        private Server(
            int number,
            String name,
            HolyWorldController.LiteMode mode
        ) {
            this.number = number;
            this.name = name;
            this.mode = mode;
        }
    }

    public void updateIfNeeded() {
        if (System.currentTimeMillis() < nextUpdate) {
            return;
        }

        refresh();
    }

    public void refresh() {
        if (!updating.compareAndSet(false, true)) {
            return;
        }

        nextUpdate = System.currentTimeMillis() + RETRY_INTERVAL_MS;

        CompletableFuture.runAsync(() -> {
            try {
                Map<Integer, Server> result = fetchServers();

                if (result.isEmpty()) {
                    throw new IllegalStateException("API returned no recognized Lite Anarchy servers");
                }

                servers = Collections.unmodifiableMap(result);
                loaded = true;

                nextUpdate = System.currentTimeMillis() + UPDATE_INTERVAL_MS;

                BQoL.LOGGER.info("Loaded {} Lite Anarchy servers, max #{}", result.size(), getMaxAnarchy());

            } catch (Exception e) {
                BQoL.LOGGER.warn("Failed to update server list", e);

            } finally {
                updating.set(false);
            }
        });
    }

    private Map<Integer, Server> fetchServers() throws Exception {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                new URL(API_URL).openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                throw new IllegalStateException("HTTP " + connection.getResponseCode());
            }

            JsonObject json;

            try (InputStream stream = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {

                JsonElement root = JsonParser.parseReader(reader);

                if (!root.isJsonObject()) {
                    throw new IllegalStateException("Invalid API response");
                }

                json = root.getAsJsonObject();
            }

            Map<Integer, Server> result = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                Matcher matcher = SERVER_ID.matcher(entry.getKey());

                if (!matcher.matches()) {
                    continue;
                }

                JsonElement element = entry.getValue();

                if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                    continue;
                }

                int number;

                try {
                    number = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }

                if (number < 1) {
                    continue;
                }

                String name = element.getAsString();

                Matcher nameMatcher = SERVER_NAME.matcher(name);

                if (nameMatcher.find()) {
                    try {
                        if (Integer.parseInt(nameMatcher.group(1)) != number) {
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }

                HolyWorldController.LiteMode mode = HolyWorldController.LiteMode.fromName(name);

                if (mode == null) {
                    continue;
                }

                result.put(number, new Server(number, name, mode));
            }

            return result;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean exists(int number) {
        if (number < 1) {
            return false;
        }

        if (loaded) {
            return servers.containsKey(number);
        }

        return HolyWorldController.LiteMode
            .fallbackForNumber(number) != null;
    }

    public HolyWorldController.LiteMode getMode(int number) {
        if (loaded) {
            Server server = servers.get(number);

            return server == null ? null : server.mode;
        }

        return HolyWorldController.LiteMode
            .fallbackForNumber(number);
    }

    public String getDisplayName(int number) {
        Server server = servers.get(number);

        if (server != null) {
            return server.name;
        }

        HolyWorldController.LiteMode mode = getMode(number);

        return mode == null ? null : mode.displayName + " #" + number;
    }

    public int getMaxAnarchy() {
        if (!loaded) {
            return 36;
        }

        return servers.keySet()
            .stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0);
    }

    public int getServerCount() {
        return loaded ? servers.size() : 36;
    }

    public OptionalInt getHighestNumber(HolyWorldController.LiteMode mode) {
        return servers.values()
            .stream()
            .filter(server -> server.mode == mode)
            .mapToInt(server -> server.number)
            .max();
    }

    public Map<Integer, Server> getServers() {
        return servers;
    }
}