package com.zavk1n.bqol.features.rct;

import com.zavk1n.bqol.features.rct.faketime.FakeTimeProcess;
import com.zavk1n.bqol.features.rct.holyworld.HolyWorldProcess;
import com.zavk1n.bqol.features.rct.reallyworld.ReallyWorldProcess;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

public final class MessageHandler {
    private MessageHandler() {}

    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> handleMessage(message));
    }

    private static void handleMessage(Text message) {
        if (message == null) {
            return;
        }

        String content = message.getString();

        HolyWorldProcess.handleIncomingMessage(content);
        ReallyWorldProcess.handleIncomingMessage(content);
        FakeTimeProcess.handleIncomingMessage(content);
    }
}