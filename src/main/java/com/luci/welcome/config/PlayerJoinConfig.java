package com.luci.welcome.config;

import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class PlayerJoinConfig {

    private final ConfigHandler config;
    private final LoggerUtil logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();

    public PlayerJoinConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-PlayerJoin"));
    }

    // -------------------
    // MOTD
    // -------------------
    public void sendMOTD(Player player) {
        if (player == null || !player.isOnline()) return;

        ConfigurationSection motd = config.getConfigurationSection("messages.local.motd");
        if (motd == null || !motd.getBoolean("enabled", true)) return;

        List<String> lines = motd.getStringList("lines");
        if (lines.isEmpty()) {
            logger.debug("MOTD не настроен, пропускаем");
            return;
        }

        int sentCount = 0;
        for (String line : lines) {
            if (line == null || line.isEmpty()) continue;
            if (sendLine(player, line)) {
                sentCount++;
            }
        }
    }

    private boolean sendLine(Player player, String line) {
        try {
            // Замена базовых плейсхолдеров
            line = line.replace("%player_name%", player.getName())
                    .replace("%player_displayname%", player.getDisplayName())
                    .replace("%player_world%", player.getWorld().getName())
                    .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()));

            // Замена ссылок и плейсхолдеров
            line = config.replaceLinksWithMiniMessage(line);
            line = config.parsePlaceholders(player, line);

            // Парсим и отправляем
            Component component = miniMessage.deserialize(line);
            player.sendMessage(component);
            return true;
        } catch (Exception e) {
            logger.warn("Ошибка при отправке MOTD строки игроку " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    // -------------------
    // Join/Quit messages
    // -------------------
    public void broadcastJoinMessage(Player player, boolean firstJoin) {
        if (!isJoinEnabled() || player == null) return;

        String path = "messages.global.";
        if (firstJoin) {
            path += "first_join_message";
        } else {
            path += "join_message";
        }

        broadcastRandomMessage(player, path);
    }

    public void broadcastQuitMessage(Player player) {
        if (!isQuitEnabled() || player == null) return;
        broadcastRandomMessage(player, "messages.global.quit_message");
    }

    public void broadcastRandomMessage(Player player, String path) {
        if (player == null) return;

        List<String> messages = config.getStringList(path);
        if (messages.isEmpty()) {
            messages = getDefaultMessages(path);
        }

        String template = messages.get(random.nextInt(messages.size()));
        String message = processMessage(template, player);

        Bukkit.broadcast(miniMessage.deserialize(message));
    }

    public void broadcastPermissionMessage(Player player, String type) {
        if (player == null) return;

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
    // Titles
    // -------------------
    public void sendJoinTitle(Player player, boolean firstJoin) {
        if (player == null || !player.isOnline()) return;

        String titlePath = firstJoin ? "messages.local.first_join_title" : "messages.local.join_title";
        String subtitlePath = firstJoin ? "messages.local.first_join_subtitle" : "messages.local.join_subtitle";

        String title = config.getString(titlePath + ".title", firstJoin ?
                "<gold>Добро пожаловать!</gold>" : "<green>С возвращением!</green>");
        String subtitle = config.getString(subtitlePath + ".subtitle",
                "<gray>%player_name%</gray>");

        title = processMessage(title, player);
        subtitle = processMessage(subtitle, player);

        player.sendTitlePart(TitlePart.TITLE, miniMessage.deserialize(title));
        player.sendTitlePart(TitlePart.SUBTITLE, miniMessage.deserialize(subtitle));

        // Настройки титула
        int fadeIn = config.getInt("messages.local.join_title.fade_in", 10);
        int stay = config.getInt("messages.local.join_title.stay", 70);
        int fadeOut = config.getInt("messages.local.join_title.fade_out", 20);

        player.sendTitlePart(TitlePart.TIMES, Title.Times.times(
                java.time.Duration.ofMillis(fadeIn * 50),
                java.time.Duration.ofMillis(stay * 50),
                java.time.Duration.ofMillis(fadeOut * 50)
        ));
    }

    // -------------------
    // Effects
    // -------------------
    public void playJoinEffects(Player player, boolean firstJoin) {
        if (player == null || !player.isOnline()) return;

        if (firstJoin) {
            config.playEffect(player,
                    "settings.effects.first_join.sound",
                    "settings.effects.first_join.particle");
        } else {
            config.playEffect(player,
                    "settings.effects.normal_join.sound",
                    "settings.effects.normal_join.particle");
        }
    }

    public void playLeaveEffects(Player player) {
        if (player == null || !player.isOnline()) return;
        config.playEffect(player,
                "settings.effects.leave.sound",
                "settings.effects.leave.particle");
    }

    public void playWelcomeSound(Player player) {
        if (player == null || !player.isOnline()) return;
        config.playSound(player, "settings.effects.welcome.sound");
    }

    // -------------------
    // Firework on first join
    // -------------------
    public void spawnFirstJoinFirework(Player player) {
        if (player == null || !player.isOnline()) return;

        boolean enabled = config.getBoolean("settings.effects.first_join.firework", false);
        if (!enabled) return;

        try {
            FireworkEffect effect = FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.AQUA)
                    .withColor(Color.LIME)
                    .withFade(Color.YELLOW)
                    .withFlicker()
                    .withTrail()
                    .build();

            Firework firework = player.getWorld().spawn(
                    player.getLocation(),
                    Firework.class
            );

            // Правильный способ: через FireworkMeta
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(effect);
            meta.setPower(1); // Сила полёта (1-3)
            firework.setFireworkMeta(meta);

            // Запускаем фейерверк
            firework.detonate(); // мгновенный взрыв на месте
            // firework.launch(); // альтернатива: запустить в воздух

        } catch (Exception e) {
            logger.error("Ошибка спавна фейерверка для игрока " + player.getName(), e);
        }
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

    public boolean isMOTDEnabled() {
        return config.getBoolean("messages.local.motd.enabled", true);
    }

    public boolean isTitlesEnabled() {
        return config.getBoolean("messages.local.join_title.enabled", true);
    }

    // -------------------
    // Helper methods
    // -------------------
    private String processMessage(String message, Player player) {
        if (message == null || player == null) return "";

        // Замена базовых плейсхолдеров
        message = message.replace("%player_name%", player.getName())
                .replace("%player_displayname%", player.getDisplayName())
                .replace("%player_world%", player.getWorld().getName())
                .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()));

        // Замена ссылок с MiniMessage
        message = config.replaceLinksWithMiniMessage(message);

        // Парсинг PlaceholderAPI
        message = config.parsePlaceholders(player, message);

        return message;
    }

    private List<String> getDefaultMessages(String path) {
        if (path.contains("join")) {
            return List.of(
                    "<green>➜ %player_name% присоединился к серверу!</green>",
                    "<gold>Рады видеть тебя, %player_name%!</gold>"
            );
        } else if (path.contains("quit")) {
            return List.of(
                    "<red>⬅ %player_name% покинул сервер</red>",
                    "<gray>%player_name% ушёл... Но обещал вернуться!</gray>"
            );
        } else if (path.contains("first_join")) {
            return List.of(
                    "<gold>✨ %player_name% впервые на сервере! Поприветствуем! ✨</gold>"
            );
        }
        return List.of("<gray>%player_name%</gray>");
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}