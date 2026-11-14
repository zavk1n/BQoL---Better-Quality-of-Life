package com.zavk1n.bqol;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BQoL implements ModInitializer {
    public static final String MOD_NAME = "BQoL";
    public static final String MOD_VERSION = "1.1.0";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("{} v{} initialized", MOD_NAME, MOD_VERSION);
    }
}