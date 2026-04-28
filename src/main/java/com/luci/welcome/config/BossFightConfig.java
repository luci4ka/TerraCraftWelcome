package com.luci.welcome.config;

import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BossFightConfig {

    private final ConfigHandler config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LoggerUtil logger;

    public BossFightConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-BossFight"));
    }

    // Основные настройки
    public boolean isEnabled() {
        return config.getBoolean("settings.boss_fight.enabled", true);
    }

    public Map<String, String> getBossList() {
        ConfigurationSection section = config.getConfigurationSection("settings.boss_fight.boss_list");
        Map<String, String> bosses = new LinkedHashMap<>();

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".name", key);
                bosses.put(key.toUpperCase(), name);
            }
        }

        // Стандартные боссы если список пуст
        if (bosses.isEmpty()) {
            bosses.put("ENDER_DRAGON", "Эндер Дракон");
            bosses.put("WITHER", "Визер");
            bosses.put("ELDER_GUARDIAN", "Древний Страж");
            bosses.put("WARDEN", "Хранитель");
        }

        return bosses;
    }

    public boolean isBossEntity(String entityType) {
        return getBossList().containsKey(entityType.toUpperCase());
    }

    public String getBossDisplayName(String entityType) {
        return getBossList().getOrDefault(entityType.toUpperCase(), entityType);
    }

    public double getBossHealthMultiplier(String entityType) {
        return config.getDouble("settings.boss_fight.boss_list." + entityType.toUpperCase() + ".health_multiplier", 1.0);
    }

    public boolean trackDamage() {
        return config.getBoolean("settings.boss_fight.track_damage", true);
    }

    public boolean showTopHitters() {
        return config.getBoolean("settings.boss_fight.show_top_hitters", true);
    }

    public int getMaxTopPlayers() {
        return Math.max(1, config.getInt("settings.boss_fight.max_top_players", 5));
    }

    public double getMinDamage() {
        return config.getDouble("settings.boss_fight.min_damage", 0.0);
    }

    // Сообщения
    public String getBossKillMessage(String bossName) {
        return config.getString(
                "settings.boss_fight.messages.boss_kill",
                "<gold>⚔ Победа над боссом: <red>{boss_name}</red>!</gold>"
        ).replace("{boss_name}", bossName);
    }

    public String getBossTopIntro() {
        return config.getString(
                "settings.boss_fight.messages.boss_top_intro",
                "<green>Игроки, нанесшие урон:</green>"
        );
    }

    public String getBossTopOutro() {
        return config.getString(
                "settings.boss_fight.messages.boss_top_outro",
                "<green>Поздравляем победителей!</green>"
        );
    }

    public String formatBossTopPlayer(String playerName, double damage, double percent) {
        String template = config.getString(
                "settings.boss_fight.messages.boss_top_player",
                "<yellow>{player_name}</yellow>: {damage}❤ (<gray>{percent}%</gray>)"
        );
        return template
                .replace("{player_name}", playerName)
                .replace("{damage}", String.format("%.1f", damage))
                .replace("{percent}", String.format("%.1f", percent));
    }

    public String formatBossTopPlayerAdvanced(int position, String playerName, double damage, double percent) {
        // Используем formatBossTopPlayer внутри!
        String base = formatBossTopPlayer(playerName, damage, percent);
        return "<gold>#" + position + "</gold> " + base;
    }

    public String getNoDamageMessage() {
        return config.getString(
                "settings.boss_fight.messages.no_damage",
                "<gray>Никто не нанёс урон боссу</gray>"
        );
    }

    // Компоненты для отправки (используем в листенере)
    public Component getBossKillMessageComponent(String bossName) {
        return miniMessage.deserialize(getBossKillMessage(bossName));
    }

    public Component getBossTopIntroComponent() {
        return miniMessage.deserialize(getBossTopIntro());
    }

    public Component getBossTopOutroComponent() {
        return miniMessage.deserialize(getBossTopOutro());
    }

    public Component formatBossTopPlayerComponent(String playerName, double damage, double percent) {
        return miniMessage.deserialize(formatBossTopPlayer(playerName, damage, percent));
    }

    public Component formatBossTopPlayerAdvancedComponent(int position, String playerName, double damage, double percent) {
        return miniMessage.deserialize(formatBossTopPlayerAdvanced(position, playerName, damage, percent));
    }

    public Component getNoDamageComponent() {
        return miniMessage.deserialize(getNoDamageMessage());
    }

    // Эффекты
    public void playBossDeathEffect(Player player) {
        if (player == null || !player.isOnline()) return;
        config.playEffect(player, "settings.effects.death.sound", "settings.effects.death.particle");
    }

    public void playBossSpawnEffect(Player player) {
        if (player == null || !player.isOnline()) return;
        config.playEffect(player, "settings.effects.spawn.sound", "settings.effects.spawn.particle");
    }

    public void playTopPlayerEffect(Player player) {
        if (player == null || !player.isOnline()) return;
        config.playEffect(player, "settings.effects.top_player.sound", "settings.effects.top_player.particle");
    }

    // Награды
    public boolean isRewardsEnabled() {
        return config.getBoolean("settings.boss_fight.rewards.enabled", false);
    }

    public List<String> getRewardCommands(String place) {
        return config.getStringList("settings.boss_fight.rewards." + place);
    }

    public double getRewardMoney(String place) {
        return config.getDouble("settings.boss_fight.rewards." + place + "_money", 0.0);
    }

    // Информация о боссах
    public List<String> getBossNames() {
        List<String> names = config.getStringList("settings.boss_fight.boss_names");
        if (names.isEmpty()) {
            names = List.of("ENDER_DRAGON", "WITHER", "ELDER_GUARDIAN", "WARDEN");
        }
        return names;
    }

    public boolean isBossWorld(String worldName) {
        List<String> worlds = config.getStringList("settings.boss_fight.worlds");
        return worlds.isEmpty() || worlds.contains(worldName) || worlds.contains("*");
    }

    public int getRewardExperience(String place) {
        return config.getInt("settings.boss_fight.rewards." + place + ".experience", 0);
    }

    public List<String> getRewardItems(String place) {
        return config.getStringList("settings.boss_fight.rewards." + place + ".items");
    }

    // Уведомления (используем getBossSpawnMessage в компоненте)
    public String getBossSpawnMessage(String bossName) {
        return config.getString(
                "settings.boss_fight.messages.boss_spawn",
                "<red>⚠ Босс {boss_name} появился!</red>"
        ).replace("{boss_name}", bossName);
    }

    public Component getBossSpawnMessageComponent(String bossName) {
        return miniMessage.deserialize(getBossSpawnMessage(bossName));
    }

    public String getBossSpawnTitle() {
        return config.getString("settings.boss_fight.messages.boss_title", "<red>Босс появился!</red>");
    }

    public String getBossSpawnSubtitle() {
        return config.getString("settings.boss_fight.messages.boss_subtitle", "<gold>Приготовьтесь к битве!</gold>");
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}