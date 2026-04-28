package com.luci.welcome.config;

import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class PlayerDeathConfig {

    private final ConfigHandler config;
    private final LoggerUtil logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();

    public PlayerDeathConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-PlayerDeath"));
    }

    public boolean isDeathEnabled() {
        return config.getBoolean("messages.global.death", true);
    }

    public boolean isRespawnEnabled() {
        return config.getBoolean("messages.global.respawn", true);
    }

    // -------------------
    // PVP death
    // -------------------
    public void broadcastPvpDeath(Player victim, Player killer) {
        if (victim == null || killer == null) return;

        List<String> messages = config.getStringList("messages.global.pvp_message.messages");
        if (messages.isEmpty()) {
            messages = getDefaultPvpMessages();
        }

        String template = messages.get(random.nextInt(messages.size()));
        String msg = template
                .replace("{killer}", killer.getName())
                .replace("{victim}", victim.getName())
                .replace("{player}", victim.getName())
                .replace("%player_name%", victim.getName());

        msg = config.replaceLinksWithMiniMessage(msg);
        broadcast(msg);

        logger.debug("PVP: " + killer.getName() + " убил " + victim.getName());
    }

    // -------------------
    // Mob death
    // -------------------
    public void broadcastMobDeath(Player player, Mob mob) {
        if (player == null || mob == null) return;

        String mobKey = mob.getType().name().toUpperCase();
        List<String> messages = config.getStringList("messages.global.mob_death_message." + mobKey);

        // Если нет сообщений для конкретного моба, используем default
        if (messages.isEmpty()) {
            messages = config.getStringList("messages.global.mob_death_message.default");
        }

        // Если и default нет, используем запасное сообщение
        if (messages.isEmpty()) {
            messages = List.of("<gray>%player_name% был убит мобом</gray>");
        }

        for (String template : messages) {
            String msg = template.replace("%player_name%", player.getName())
                    .replace("{mob}", mob.getName())
                    .replace("%mob%", mob.getName());
            msg = config.replaceLinksWithMiniMessage(msg);
            broadcast(msg);
        }

        logger.debug("Игрок " + player.getName() + " убит мобом: " + mob.getType().name());
    }

    // -------------------
    // Environment / generic death
    // -------------------
    public void broadcastEnvironmentDeath(Player player) {
        if (player == null) return;

        List<String> messages = config.getStringList("messages.global.player_kill_message.messages");
        if (messages.isEmpty()) {
            messages = getDefaultEnvironmentMessages();
        }

        String template = messages.get(random.nextInt(messages.size()));
        String msg = template.replace("%player_name%", player.getName())
                .replace("{player}", player.getName());
        msg = config.replaceLinksWithMiniMessage(msg);
        broadcast(msg);

        logger.debug("Игрок " + player.getName() + " умер от окружения");
    }

    // -------------------
    // Custom death messages
    // -------------------
    public void broadcastCustomDeath(Player player, String deathType) {
        if (player == null || deathType == null) return;

        List<String> messages = config.getStringList("messages.global." + deathType + ".messages");
        if (messages.isEmpty()) {
            broadcastEnvironmentDeath(player);
            return;
        }

        String template = messages.get(random.nextInt(messages.size()));
        String msg = template.replace("%player_name%", player.getName());
        msg = config.replaceLinksWithMiniMessage(msg);
        broadcast(msg);
    }

    // -------------------
    // Respawn
    // -------------------
    public void sendRespawn(Player player) {
        if (player == null || !player.isOnline()) return;

        List<String> messages = config.getStringList("messages.global.respawn_message");
        if (messages.isEmpty()) {
            messages = List.of("<green>Вы возродились!</green>");
        }

        String template = messages.get(random.nextInt(messages.size()));
        String msg = template.replace("%player_name%", player.getName());
        msg = config.replaceLinksWithMiniMessage(msg);

        Component component = miniMessage.deserialize(msg);
        player.sendMessage(component);

        logger.debug("Игрок " + player.getName() + " возродился");
    }

    // -------------------
    // Title on death
    // -------------------
    public void sendDeathTitle(Player player) {
        if (player == null || !player.isOnline()) return;

        String title = config.getString("messages.global.death_title.title", "<red>Вы погибли!</red>");
        String subtitle = config.getString("messages.global.death_title.subtitle", "<gray>Но вы ещё вернётесь...</gray>");

        player.sendTitlePart(
                net.kyori.adventure.title.TitlePart.TITLE,
                miniMessage.deserialize(title)
        );
        player.sendTitlePart(
                net.kyori.adventure.title.TitlePart.SUBTITLE,
                miniMessage.deserialize(subtitle)
        );
    }

    // -------------------
    // Effects
    // -------------------
    public void playDeathEffect(Player player) {
        if (player == null || !player.isOnline()) return;
        config.playEffect(player, "settings.effects.death.sound", "settings.effects.death.particle");
    }

    public void playRespawnEffect(Player player) {
        if (player == null || !player.isOnline()) return;
        config.playEffect(player, "settings.effects.respawn.sound", "settings.effects.respawn.particle");
    }

    public void playKillerEffect(Player killer) {
        if (killer == null || !killer.isOnline()) return;
        config.playEffect(killer, "settings.effects.killer.sound", "settings.effects.killer.particle");
    }

    // -------------------
    // Default messages
    // -------------------
    private List<String> getDefaultPvpMessages() {
        return List.of(
                "<red>⚔ {killer} убил {victim} в PvP битве!</red>",
                "<gold>{victim} был повержен игроком {killer}</gold>"
        );
    }

    private List<String> getDefaultEnvironmentMessages() {
        return List.of(
                "<gray>☠ %player_name% погиб</gray>",
                "<dark_gray>RIP %player_name%</dark_gray>"
        );
    }

    // -------------------
    // Broadcast helper
    // -------------------
    private void broadcast(String msg) {
        if (msg == null || msg.isEmpty()) return;

        try {
            Component component = miniMessage.deserialize(msg);
            Bukkit.broadcast(component);
        } catch (Exception e) {
            logger.error("Ошибка отправки broadcast сообщения: " + msg, e);
        }
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}