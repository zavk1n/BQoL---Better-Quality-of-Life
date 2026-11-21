package com.zavk1n.bqol.config;

public abstract class Validate extends GettersSettersETC {

    protected Validate() {
    }

    protected void validateSettings() {
        betterTntAlertPosition = Math.max(0, Math.min(4, betterTntAlertPosition));

        betterHealthDuration = Math.max(3000, Math.min(30000, betterHealthDuration));
        betterHealthPosition = Math.max(0, Math.min(3, betterHealthPosition));

        betterFogDistance = Math.max(1, Math.min(32, betterFogDistance));

        betterFogColor &= 0xFFFFFF;

        betterLiquidWaterColor &= 0xFFFFFF;
        betterLiquidLavaColor &= 0xFFFFFF;
        betterLiquidWaterTransparency = Math.max(0, Math.min(100, betterLiquidWaterTransparency));
        betterLiquidLavaTransparency = Math.max(0, Math.min(100, betterLiquidLavaTransparency));

        betterLiquidWaterColorIntensity = Math.max(0, Math.min(100, betterLiquidWaterColorIntensity));
        betterLiquidLavaColorIntensity = Math.max(0, Math.min(100, betterLiquidLavaColorIntensity));

        betterSkyColor &= 0xFFFFFF;

        betterPrefixesNameColorRgb &= 0xFFFFFF;
        betterPrefixesDonateColorRgb &= 0xFFFFFF;
        betterPrefixesTitleColorRgb &= 0xFFFFFF;
        betterPrefixesStarColorRgb &= 0xFFFFFF;
        betterPrefixesLightningColorRgb &= 0xFFFFFF;

        betterHitColor &= 0xFFFFFF;
        betterHitboxesColor &= 0xFFFFFF;

        betterParticlesShulkersConstantColor &= 0xFFFFFF;
        betterParticlesShulkersBreakingColor &= 0xFFFFFF;

        betterSkyTime = Math.max(1000L, Math.min(24000L, betterSkyTime));

        if (betterInteractAutoSignsText == null) {
            betterInteractAutoSignsText = "";
        }

        if (betterChatKillMessageText == null) {
            betterChatKillMessageText = "";
        }

        if (betterPrefixesProtectedName == null) {
            betterPrefixesProtectedName = "";
        }

        if (betterPrefixesDonateText == null) {
            betterPrefixesDonateText = "";
        }

        if (betterPrefixesTitleText == null) {
            betterPrefixesTitleText = "";
        }

        if (noRenderTotemOverlay == null) {
            noRenderTotemOverlay = RenderMode.FULL;
        }

        if (noRenderFireOverlay == null) {
            noRenderFireOverlay = RenderMode.FULL;
        }

        if (noRenderTotemParticles == null) {
            noRenderTotemParticles = RenderMode.FULL;
        }

        if (noRenderPotionParticles == null) {
            noRenderPotionParticles = RenderMode.FULL;
        }

        if (noRenderExplosion == null) {
            noRenderExplosion = RenderMode.FULL;
        }

        if (noRenderSmoke == null) {
            noRenderSmoke = RenderMode.FULL;
        }

        if (noRenderBubbles == null) {
            noRenderBubbles = RenderMode.FULL;
        }

        if (noRenderWeather == null) {
            noRenderWeather = RenderMode.FULL;
        }

        if (noRenderArrows == null) {
            noRenderArrows = RenderMode.FULL;
        }

        if (noRenderFireworks == null) {
            noRenderFireworks = RenderMode.FULL;
        }

        if (noRenderNames == null) {
            noRenderNames = RenderMode.FULL;
        }

        if (noRenderPlayers == null) {
            noRenderPlayers = RenderMode.FULL;
        }

        if (noRenderHand == null) {
            noRenderHand = RenderMode.FULL;
        }

        if (betterHealthGoldenHeartsPlus && !betterHealthGoldenHearts) {
            betterHealthGoldenHeartsPlus = false;
        }
    }
}
