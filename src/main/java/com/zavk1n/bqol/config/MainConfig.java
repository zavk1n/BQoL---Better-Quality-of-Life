package com.zavk1n.bqol.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zavk1n.bqol.BQoL;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MainConfig extends Defaults {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File config_file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "bqol_config.json");

    private static MainConfig instance;

    protected MainConfig() {
        validateSettings();
    }

    public static MainConfig getInstance() {
        if (instance == null) {
            instance = load();
        }

        return instance;
    }

    public static MainConfig load() {
        MainConfig config;

        if (config_file.exists()) {
            try (FileReader reader = new FileReader(config_file)) {
                config = GSON.fromJson(reader, MainConfig.class);

                if (config == null) {
                    config = new MainConfig();
                } else {
                    config.validateSettings();
                }
            } catch (Exception e) {
                BQoL.LOGGER.error("Failed to load BQoL config", e);
                config = new MainConfig();
            }
        } else {
            config = new MainConfig();
        }

        instance = config;
        return config;
    }

    public void save() {
        validateSettings();

        try {
            if (!config_file.getParentFile().exists()) {
                config_file.getParentFile().mkdirs();
            }

            try (FileWriter writer = new FileWriter(config_file)) {
                GSON.toJson(this, writer);
                BQoL.LOGGER.info("Saved BQoL config to {}", config_file.getAbsolutePath());
            }
        } catch (IOException e) {
            BQoL.LOGGER.error("Failed to save BQoL config", e);
        }
    }

    public String toJson() { return GSON.toJson(this); }

    public static MainConfig fromJson(String json) {
        MainConfig config = GSON.fromJson(json, MainConfig.class);

        if (config == null) {
            return new MainConfig();
        }

        config.validateSettings();

        return config;
    }

    public MainConfig copy() { return fromJson(toJson()); }
}
