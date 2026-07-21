package com.zavk1n.bqol;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BQoL implements ModInitializer {

    public static final String MOD_ID = "bqol";
    public static final String MOD_NAME = "BQoL";
    public static final String MOD_VERSION = "1.0.0";

    public static final Logger LOGGER = LoggerFactory.getLogger(BQoL.class);

    @Override
    public void onInitialize() {
        LOGGER.info("{} v{} initialized", MOD_NAME, MOD_VERSION);
    }
}
// v1.0