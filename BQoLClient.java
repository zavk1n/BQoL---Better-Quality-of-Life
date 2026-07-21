package com.zavk1n.bqol.client;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.client.init.ConnectionManager;
import com.zavk1n.bqol.client.init.FeatureRegistry;
import com.zavk1n.bqol.client.init.TickManager;
import com.zavk1n.bqol.utils.NotificationManager;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.fabricmc.api.ClientModInitializer;

public class BQoLClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BQoL.LOGGER.info(
            "Initializing {} Client v{}",
            BQoL.MOD_NAME,
            BQoL.MOD_VERSION
        );

        try {
            /// 1. Глобальные утилиты
            LiteApiManager.register();
            NotificationManager.initialize();

            /// 2. Декларативная инициализация всех функий
            FeatureRegistry.initializeAll();

            /// 3. Клавиши и HUD
            KeybindManager.initialize();

            /// 4. Обработка подключения / отключения
            ConnectionManager.register();

            /// 5. Обработка тиков
            TickManager.register();

            BQoL.LOGGER.info("BQoL client loaded successfully");
        } catch (Exception e) {
            BQoL.LOGGER.error("Error initializing BQoL client", e);
            throw e;
        }
    }
}
// v1.0