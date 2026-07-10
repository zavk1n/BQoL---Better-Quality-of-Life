package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import java.util.EnumSet;
import java.util.Locale;
import java.util.function.Predicate;

public class BetterSounds {

    private BetterSounds() {
        initializeRules();
    }

    private static BetterSounds instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private Predicate<String> combinedRule = path -> false;
    private final EnumSet<SoundCategory> enabledModes = EnumSet.noneOf(SoundCategory.class);

    private enum SoundCategory {
        EXPLOSION(config -> config.isExplosionMode()),
        ENDER_DRAGON(config -> config.isEnderDragonMode()),
        VILLAGER(config -> config.isVillagerMode()),
        THUNDER(config -> config.isThunderMode()),
        MOOD(config -> config.isMoodMode()),
        ICE(config -> config.isIceMode()),
        PISTON(config -> config.isPistonMode()),
        FIRE(config -> config.isFireMode()),
        EAT(config -> config.isEatMode()),
        DRINK(config -> config.isDrinkMode()),
        HITS(config -> config.isHitsMode()),
        STORAGE(config -> config.isStorageMode()),
        GRASS(config -> config.isGrassMode()),
        TOTEM(config -> config.isTotemMode()),
        ANVIL(config -> config.isAnvilMode()),
        XP(config -> config.isXpMode()),
        MINING(config -> config.isMiningMode()),
        WOOD(config -> config.isWoodMode()),
        LAVA_WATER(config -> config.isLavaWaterMode()),
        FARM(config -> config.isFarmMode()),
        MOB(config -> config.isMobMode()),
        ENDER_PORTAL(config -> config.isEnderPortalMode()),
        ACHIEVEMENTS(config -> config.isAchievementsMode());

        private final Predicate<BQoLConfig> enabledGetter;
        private Predicate<String> predicate;

        SoundCategory(Predicate<BQoLConfig> enabledGetter) {
            this.enabledGetter = enabledGetter;
        }

        public boolean isCategoryEnabled(BQoLConfig config) {
            return enabledGetter.test(config);
        }

        public Predicate<String> getPredicate() {
            return predicate;
        }

        public void setPredicate(Predicate<String> predicate) {
            this.predicate = predicate;
        }
    }

    /// Блокировка
    private boolean blockedMain;

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new BetterSounds();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            BQoL.LOGGER.info("BetterSounds initialized");
        }
    }

    public static BetterSounds getInstance() {
        if (instance == null)
            initialize();
        return instance;
    }

    public static void refreshBlockedStatus() { if (instance != null) instance.refreshBlockedStatusInternal(); }

    public static void reloadFromConfig() { if (instance != null) instance.reloadFromConfigInternal(); }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) { if (instance != null) instance.setEnabledInternal(enabled); }

    public static void setMode(SoundCategory category, boolean enabled) {
        if (instance != null) {
            instance.setModeInternal(category, enabled);
        }
    }

    /// Внутренние динамические методы
    private void refreshBlockedStatusInternal() {
        blockedMain = LiteApiManager.isFeatureBlocked("better_sounds");
    }

    private void reloadFromConfigInternal() {
        refreshBlockedStatusInternal();

        enabledModes.clear();

        for (SoundCategory category : SoundCategory.values()) {
            setMode(category, category.isCategoryEnabled(config));
        }
    }

    private boolean isEnabledInternal() {
        return config.isBetterSoundsEnabled() && !blockedMain;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterSoundsEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    private void setModeInternal(SoundCategory category, boolean enabled) {
        if (enabled) {
            enabledModes.add(category);
        } else {
            enabledModes.remove(category);
        }

        rebuildCombinedRule();
    }

    /// Работа с правилами
    private void initializeRules() {
        SoundCategory.EXPLOSION.setPredicate(this::isExplosionSound);
        SoundCategory.ENDER_DRAGON.setPredicate(this::isEnderDragonSound);
        SoundCategory.VILLAGER.setPredicate(this::isVillagerSound);
        SoundCategory.THUNDER.setPredicate(this::isThunderSound);
        SoundCategory.MOOD.setPredicate(this::isMoodSound);
        SoundCategory.ICE.setPredicate(this::isIceSound);
        SoundCategory.PISTON.setPredicate(this::isPistonSound);
        SoundCategory.FIRE.setPredicate(this::isFireSound);
        SoundCategory.EAT.setPredicate(this::isEatSound);
        SoundCategory.DRINK.setPredicate(this::isDrinkSound);
        SoundCategory.HITS.setPredicate(this::isHitSound);
        SoundCategory.STORAGE.setPredicate(this::isStorageSound);
        SoundCategory.GRASS.setPredicate(this::isGrassSound);
        SoundCategory.TOTEM.setPredicate(this::isTotemSound);
        SoundCategory.ANVIL.setPredicate(this::isAnvilSound);
        SoundCategory.XP.setPredicate(this::isXpSound);
        SoundCategory.MINING.setPredicate(this::isMiningSound);
        SoundCategory.WOOD.setPredicate(this::isWoodSound);
        SoundCategory.LAVA_WATER.setPredicate(this::isLavaWaterSound);
        SoundCategory.FARM.setPredicate(this::isFarmSound);
        SoundCategory.MOB.setPredicate(this::isMobSound);
        SoundCategory.ENDER_PORTAL.setPredicate(this::isEnderPortalSound);
        SoundCategory.ACHIEVEMENTS.setPredicate(this::isAchievementSound);
    }

    /// Фильтрация звуков
    public boolean shouldPlaySound(String soundPath) {
        if (!isEnabledInternal() || soundPath == null) {
            return true;
        }

        return !combinedRule.test(soundPath.toLowerCase(Locale.ROOT));
    }

    private void rebuildCombinedRule() {
        combinedRule = path -> false;

        for (SoundCategory category : enabledModes) {
            Predicate<String> predicate = category.getPredicate();
            if (predicate != null) {
                combinedRule = combinedRule.or(predicate);
            }
        }
    }

    /// Методы определения звуков
    private boolean isExplosionSound(String path) {
        return path.contains("explosion") ||
                path.contains("entity.generic.explode") ||
                path.contains("entity.creeper.primed") ||
                path.contains("entity.tnt.primed") ||
                path.contains("tnt");
    }

    private boolean isEnderDragonSound(String path) {
        return path.contains("entity.ender_dragon") ||
                path.contains("entity.dragon") ||
                path.contains("dragon");
    }

    private boolean isPistonSound(String path) {
        return path.contains("block.piston") ||
                path.contains("tile.piston");
    }

    private boolean isIceSound(String path) {
        return path.contains("block.glass") ||
                path.contains("block.frosted_ice") ||
                path.contains("entity.generic.freeze") ||
                path.contains("entity.player.hurt_freeze") ||
                path.contains("block.ice") ||
                path.contains("block.frost") ||
                path.contains("block.glass.break") ||
                path.contains("block.glass.place") ||
                path.contains("block.glass.step") ||
                path.contains("block.glass.hit");
    }

    private boolean isVillagerSound(String path) {
        return path.contains("entity.villager") ||
                path.contains("entity.wandering_trader") ||
                path.contains("villager.trade");
    }

    private boolean isMoodSound(String path) {
        return path.contains("ambient.cave") ||
                path.contains("ambient.nether") ||
                path.contains("ambient.end") ||
                path.contains("ambient.basalt_deltas") ||
                path.contains("ambient.crimson_forest") ||
                path.contains("ambient.soul_sand_valley") ||
                path.contains("ambient.warped_forest") ||
                path.contains("music.nether") ||
                path.contains("music.end");
    }

    private boolean isThunderSound(String path) {
        return path.contains("entity.lightning") ||
                path.contains("entity.lightning_bolt") ||
                path.contains("ambient.weather.thunder") ||
                path.contains("lightning") ||
                path.contains("thunder");
    }

    private boolean isFireSound(String path) {
        return path.contains("block.fire") ||
                path.contains("entity.generic.burn") ||
                path.contains("entity.player.hurt_on_fire") ||
                path.contains("item.firecharge") ||
                path.contains("item.flintandsteel") ||
                path.contains("fire.extinguish") ||
                path.contains("block.fire.ambient");
    }

    private boolean isEatSound(String path) {
        return path.contains("entity.generic.eat") ||
                path.contains("entity.player.burp") ||
                path.contains("eat") ||
                (path.contains("item") && path.contains("consume"));
    }

    private boolean isDrinkSound(String path) {
        return path.contains("entity.generic.drink") ||
                path.contains("drink") ||
                (path.contains("item") && path.contains("drink"));
    }

    private boolean isHitSound(String path) {
        return path.contains("entity.player.attack") ||
                path.contains("entity.player.hurt") ||
                path.contains("entity.generic.hurt") ||
                path.contains("entity.generic.death") ||
                path.contains("damage") ||
                (path.contains("hit") && !path.contains("anvil"));
    }

    private boolean isStorageSound(String path) {
        return path.contains("block.chest") ||
                path.contains("block.shulker_box") ||
                path.contains("block.barrel") ||
                path.contains("block.ender_chest") ||
                path.contains("block.trapped_chest") ||
                path.contains("container") ||
                path.contains("shulker");
    }

    private boolean isGrassSound(String path) {
        return (path.contains("block.grass") &&
                (path.contains("break") || path.contains("place"))) ||
                (path.contains("block.tall_grass") && (path.contains("break") || path.contains("place"))) ||
                path.contains("block.vine") ||
                path.contains("block.lily_pad") ||
                path.contains("block.fern") ||
                path.contains("block.leaves") ||
                path.contains("block.sweet_berry_bush");
    }

    private boolean isTotemSound(String path) {
        return path.contains("item.totem.use") ||
                path.contains("totem");
    }

    private boolean isAnvilSound(String path) {
        return path.contains("block.anvil") ||
                path.contains("anvil");
    }

    private boolean isXpSound(String path) {
        return path.contains("entity.experience_orb") ||
                path.contains("entity.player.levelup") ||
                path.contains("xp") ||
                path.contains("experience") ||
                path.contains("block.glass") ||
                path.contains("block.frosted_ice") ||
                path.contains("block.glass.break") ||
                path.contains("block.glass.place") ||
                path.contains("block.glass.step") ||
                path.contains("block.glass.hit") ||
                path.contains("entity.experience_bottle") ||
                path.contains("experience_bottle") ||
                path.contains("bottle.break") ||
                path.contains("entity.splash_potion.break") ||
                path.contains("item.bottle");
    }

    private boolean isMiningSound(String path) {
        return path.contains("block.stone") ||
                path.contains("block.deepslate") ||
                path.contains("block.andesite") ||
                path.contains("block.diorite") ||
                path.contains("block.granite") ||
                path.contains("block.tuff") ||
                path.contains("block.cobblestone") ||
                path.contains("block.ore") ||
                path.contains("block.netherrack") ||
                path.contains("block.end_stone") ||
                path.contains("block.basalt") ||
                path.contains("block.blackstone") ||
                path.contains("block.calcite") ||
                path.contains("block.dripstone") ||
                path.contains("block.pointed_dripstone");
    }

    private boolean isWoodSound(String path) {
        return path.contains("block.wood") ||
                path.contains("block.planks") ||
                path.contains("block.log") ||
                path.contains("block.stripped") ||
                path.contains("block.fence") ||
                path.contains("block.gate") ||
                path.contains("block.door") ||
                path.contains("block.trapdoor") ||
                path.contains("block.sign") ||
                path.contains("block.chest") ||
                path.contains("block.barrel") ||
                path.contains("block.crafting_table") ||
                path.contains("block.loom") ||
                path.contains("block.cartography_table") ||
                path.contains("block.fletching_table") ||
                path.contains("block.smithing_table") ||
                path.contains("block.grindstone") ||
                path.contains("block.lectern") ||
                path.contains("block.composter") ||
                path.contains("block.beehive") ||
                path.contains("block.bookshelf") ||
                path.contains("block.chiseled_bookshelf") ||
                path.contains("block.ladder") ||
                path.contains("block.scaffolding") ||
                path.contains("oak") ||
                path.contains("spruce") ||
                path.contains("birch") ||
                path.contains("jungle") ||
                path.contains("acacia") ||
                path.contains("dark_oak") ||
                path.contains("mangrove") ||
                path.contains("cherry") ||
                path.contains("bamboo");
    }

    private boolean isLavaWaterSound(String path) {
        return path.contains("block.lava") ||
                path.contains("block.water") ||
                (path.contains("block.bubble_column") && (path.contains("lava") || path.contains("water"))) ||
                (path.contains("block.fire") && path.contains("extinguish"));
    }

    private boolean isFarmSound(String path) {
        return isPistonSound(path) ||
                path.contains("block.observer") ||
                path.contains("block.composter") ||
                path.contains("block.dispenser") ||
                path.contains("block.dropper") ||
                path.contains("block.note_block") ||
                path.contains("block.cauldron") ||
                path.contains("entity.minecart") ||
                path.contains("entity.armor_stand") ||
                path.contains("block.armor_stand") ||
                path.contains("block.wooden_trapdoor") ||
                path.contains("block.iron_trapdoor") ||
                path.contains("block.fence_gate") ||
                path.contains("block.pressure_plate") ||
                path.contains("block.tripwire") ||
                path.contains("block.grindstone") ||
                path.contains("block.loom") ||
                path.contains("block.scaffolding") ||
                path.contains("block.smoker") ||
                path.contains("block.lectern") ||
                path.contains("block.stonecutter");
    }

    private boolean isMobSound(String path) {
        return path.contains("entity.skeleton") ||
                path.contains("entity.stray") ||
                path.contains("entity.wither_skeleton") ||
                path.contains("entity.zombie") ||
                path.contains("entity.husk") ||
                path.contains("entity.drowned") ||
                path.contains("entity.zombie_villager") ||
                path.contains("entity.zombified_piglin") ||
                path.contains("entity.creeper") ||
                path.contains("entity.spider") ||
                path.contains("entity.warden") ||
                path.contains("entity.phantom") ||
                path.contains("entity.cave_spider") ||
                path.contains("entity.enderman") ||
                path.contains("entity.blaze") ||
                path.contains("entity.ghast") ||
                path.contains("entity.piglin") ||
                path.contains("entity.piglin_brute") ||
                path.contains("entity.hoglin") ||
                path.contains("entity.zoglin") ||
                path.contains("entity.witch") ||
                path.contains("entity.slime") ||
                path.contains("entity.magma_cube") ||
                path.contains("entity.shulker") ||
                path.contains("entity.vex") ||
                path.contains("entity.vindicator") ||
                path.contains("entity.evoker") ||
                path.contains("entity.pillager") ||
                path.contains("entity.ravager") ||
                path.contains("entity.wither") ||
                path.contains("entity.guardian") ||
                path.contains("entity.elder_guardian");
    }

    private boolean isEnderPortalSound(String path) {
        return path.contains("block.end_portal.spawn") ||
                path.contains("block.end_portal_frame.fill") ||
                path.contains("entity.enderman.portal") ||
                path.contains("block.portal.trigger") ||
                path.contains("item.ender_eye") ||
                (path.contains("portal") && (path.contains("travel") || path.contains("trigger")));
    }

    private boolean isAchievementSound(String path) {
        return path.contains("ui.toast.challenge_complete") ||
                path.contains("ui.toast.achievement") ||
                path.contains("event.achievement") ||
                path.contains("entity.player.levelup");
    }
}
// v1.0