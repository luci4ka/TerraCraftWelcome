package com.luci.welcome.config;

import org.bukkit.entity.Player;

public class BossFightConfig {

    private final ConfigHandler config;

    public BossFightConfig(ConfigHandler config) {
        this.config = config;
    }

    public boolean trackDamage() {
        return config.getBoolean("settings.boss_fight.track_damage", true);
    }

    public boolean showTopHitters() {
        return config.getBoolean("settings.boss_fight.show_top_hitters", true);
    }

    public String getBossKillMessage(String bossName) {
        String template = config.getString(
                "settings.boss_fight.messages.boss_kill",
                "<gold>⚔️ Победа над боссом: <red>{boss_name}</red>!</gold>"
        );
        return template.replace("{boss_name}", bossName);
    }

    public String getBossTopIntro() {
        return config.getString(
                "settings.boss_fight.messages.boss_top_intro",
                "<green>Игроки, нанесшие урон:</green>"
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

    public void playBossDeathEffect(Player player) {
        config.playEffect(player, "settings.effects.death.sound", "settings.effects.death.particle");
    }
}
