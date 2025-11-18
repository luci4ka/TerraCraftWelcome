package com.luci.welcome.config;

import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class PlayerJoinConfig {

    private final ConfigHandler config;
    private final LoggerUtil logger;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Random random = new Random();

    public PlayerJoinConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(config.getPlugin().getSLF4JLogger());
    }

    // -------------------
    // MOTD
    // -------------------
    public void sendMOTD(Player player) {
        ConfigurationSection motd = config.getConfigSection("messages.local.motd");
        if (motd == null || !motd.getBoolean("enabled", true)) return;

        List<String> lines = motd.getStringList("lines");
        if (lines.isEmpty()) return;

        lines.forEach(line -> {
            if (line == null || line.isEmpty()) return;
            sendLine(player, line);
        });
    }

    private void sendLine(Player player, String line) {
        try {
            line = line.replace("%player_name%", player.getName());
            line = config.replaceLinksMsg(line);
            line = config.parseMessage(player, line);

            Component component = mini.deserialize(line);
            player.sendMessage(component);
        } catch (Exception e) {
            logger.warn("Ошибка при отправке MOTD игроку " + player.getName() + ": " + e.getMessage());
        }
    }

    // -------------------
    // Join/Quit messages
    // -------------------
    public void broadcastRandomMessage(Player player, String path) {
        List<String> messages = config.getMessageList(path);
        if (messages.isEmpty()) return;

        String message = messages.get(random.nextInt(messages.size()))
                .replace("%player_name%", player.getName());
        message = config.replaceLinksMsg(message);
        message = config.parseMessage(player, message);

        Bukkit.broadcast(mini.deserialize(message));
        logger.info("Отправлено глобальное сообщение типа " + path + " игроку " + player.getName());
    }

    public void broadcastPermissionMessage(Player player, String type) {
        String path;
        if (player.hasPermission("welcome.admin")) {
            path = "messages.global.admin_" + type + "_message";
        } else if (player.hasPermission("welcome.vip")) {
            path = "messages.global.vip_" + type + "_message";
        } else {
            path = "messages.global.default_" + type + "_message";
        }
        broadcastRandomMessage(player, path);
    }

    // -------------------
    // Effects
    // -------------------
    public void playJoinEffects(Player player, boolean firstJoin) {
        if (firstJoin) {
            config.playSound(player, "settings.effects.first_join.sound");
            config.playParticle(player, "settings.effects.first_join.particle");
        } else {

            config.playSound(player, "settings.effects.normal_join.sound");
            config.playParticle(player, "settings.effects.normal_join.particle");
        }
    }

    public void playLeaveEffects(Player player) {
        config.playSound(player, "settings.effects.leave.sound");
        config.playParticle(player, "settings.effects.leave.particle");
    }

    // -------------------
    // Global flags
    // -------------------
    public boolean isJoinEnabled() {
        return config.getBoolean("messages.global.join", true);
    }

    public boolean isQuitEnabled() {
        return config.getBoolean("messages.global.quit", true);
    }
}
