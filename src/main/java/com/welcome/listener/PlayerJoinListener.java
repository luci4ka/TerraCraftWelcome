package com.welcome.listener;

import com.welcome.config.ConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlayerJoinListener implements Listener {

    private final ConfigHandler config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();

    public PlayerJoinListener(ConfigHandler config) {
        this.config = config;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);

        boolean isFirstJoin = !player.hasPlayedBefore();

        sendMOTD(player);

        if (isFirstJoin) {
            broadcastRandomMessage(player, "messages.global.first_join_message");
            playJoinEffects(player, true);
        } else {
            if (config.getBoolean("messages.global.join", true)) {
                broadcastPermissionMessage(player, "join");
            }
            playJoinEffects(player, false);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.quitMessage(null);

        if (config.getBoolean("messages.global.quit", true)) {
            broadcastPermissionMessage(player, "quit");
        }

        playLeaveEffects(player);
    }

    private void sendMOTD(Player player) {
        ConfigurationSection motdSection = config.getConfigSection("messages.local.motd");
        if (motdSection == null || !motdSection.getBoolean("enabled", true)) return;

        List<String> lines = motdSection.getStringList("lines");
        if (lines.isEmpty()) return;

        for (String line : lines) {
            if (line == null || line.isEmpty()) continue;
            sendLine(player, line);
        }
    }

    private void sendLine(Player player, String line) {
        // Заменяем плейсхолдер имени игрока
        line = line.replace("%player_name%", player.getName());

        // Разбираем ссылки из конфигурации
        for (Map.Entry<String, ConfigHandler.LinkEntry> entry : config.getLinks().entrySet()) {
            String key = entry.getKey();
            ConfigHandler.LinkEntry link = entry.getValue();

            if (line.contains("%" + key + "_url%") || line.contains("%" + key + "_text%")) {
                // Минимальное преобразование в MiniMessage кликабельную ссылку
                String clickable = "<click:open_url:" + link.getUrl() + "><underlined><blue>" + link.getText() + "</blue></underlined></click>";
                line = line.replace("%" + key + "_url%", clickable)
                        .replace("%" + key + "_text%", clickable);
            }
        }

        // Применяем PAPI и десериализуем MiniMessage
        line = config.parseMessage(player, line);
        Component component = miniMessage.deserialize(line);
        player.sendMessage(component);
    }

    private void broadcastRandomMessage(Player player, String path) {
        List<String> messages = config.getMessageList(path);
        if (messages.isEmpty()) return;

        String message = messages.get(random.nextInt(messages.size()));
        message = message.replace("%player_name%", player.getName());

        // Применяем ссылки
        for (Map.Entry<String, ConfigHandler.LinkEntry> entry : config.getLinks().entrySet()) {
            String key = entry.getKey();
            ConfigHandler.LinkEntry link = entry.getValue();
            String clickable = "<click:open_url:" + link.getUrl() + "><underlined><blue>" + link.getText() + "</blue></underlined></click>";
            message = message.replace("%" + key + "_url%", clickable)
                    .replace("%" + key + "_text%", clickable);
        }

        message = config.parseMessage(player, message);
        Component component = miniMessage.deserialize(message);
        Bukkit.broadcast(component);
    }

    private void broadcastPermissionMessage(Player player, String type) {
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

    private void playJoinEffects(Player player, boolean firstJoin) {
        if (firstJoin) {
            config.playSound(player, "effects.first_join.sound");
            config.playParticle(player, "effects.first_join.particle");
        } else {
            config.playSound(player, "effects.normal_join.sound");
            config.playParticle(player, "effects.normal_join.particle");
        }
    }

    private void playLeaveEffects(Player player) {
        config.playSound(player, "effects.leave.sound");
        config.playParticle(player, "effects.leave.particle");
    }
}
