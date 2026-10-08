package ru.net.terracraft.welcome.config;

import ru.net.terracraft.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
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

        // Проверяем, включён ли MOTD в config.yml
        if (!isMOTDEnabled()) return;

        // ✅ Берём строки из языкового файла
        List<String> lines = config.getLangMessageList("motd.lines");

        if (lines.isEmpty()) {
            logger.debug("MOTD не настроен в языковом файле, пропускаем");
            return;
        }

        for (String line : lines) {
            if (line == null || line.isEmpty()) continue;
            sendLine(player, line);
        }
    }

    private boolean sendLine(Player player, String line) {
        try {
            line = line.replace("%player_name%", player.getName())
                    .replace("%player_displayname%", player.getDisplayName())
                    .replace("%player_world%", player.getWorld().getName())
                    .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()));

            line = config.replaceLinksWithMiniMessage(line);
            line = config.parsePlaceholders(player, line);

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

        String path = firstJoin ? "join.first_join" : "join.default";
        broadcastRandomMessage(player, path);
    }

    public void broadcastQuitMessage(Player player) {
        if (!isQuitEnabled() || player == null) return;
        broadcastRandomMessage(player, "quit.default");
    }

    public void broadcastRandomMessage(Player player, String path) {
        if (player == null) return;

        // ✅ Берём сообщения из языкового файла
        List<String> messages = config.getLangMessageList(path);
        if (messages.isEmpty()) {
            messages = getDefaultMessages(path);
        }

        String template = messages.get(random.nextInt(messages.size()));
        String message = processMessage(template, player);

        Bukkit.broadcast(miniMessage.deserialize(message));
    }

    public void broadcastPermissionMessage(Player player, String type) {
        if (player == null) return;

        String permissionPath;
        if (player.hasPermission("welcome.admin")) {
            permissionPath = type + ".admin";
        } else if (player.hasPermission("welcome.vip")) {
            permissionPath = type + ".vip";
        } else {
            permissionPath = type + ".default";
        }

        broadcastRandomMessage(player, permissionPath);
    }

    // -------------------
    // Titles
    // -------------------
    public void sendJoinTitle(Player player, boolean firstJoin) {
        if (player == null || !player.isOnline() || !isTitlesEnabled()) return;

        String titleKey = firstJoin ? "titles.first_join.title" : "titles.join.title";
        String subtitleKey = firstJoin ? "titles.first_join.subtitle" : "titles.join.subtitle";

        String title = config.getLangMessage(titleKey, firstJoin ?
                "<gold>Добро пожаловать!</gold>" : "<green>С возвращением!</green>");
        String subtitle = config.getLangMessage(subtitleKey, "<gray>%player_name%</gray>");

        title = processMessage(title, player);
        subtitle = processMessage(subtitle, player);

        player.sendTitlePart(TitlePart.TITLE, miniMessage.deserialize(title));
        player.sendTitlePart(TitlePart.SUBTITLE, miniMessage.deserialize(subtitle));

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

            Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(effect);
            meta.setPower(1);
            firework.setFireworkMeta(meta);
            firework.detonate();
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

        message = message.replace("%player_name%", player.getName())
                .replace("%player_displayname%", player.getDisplayName())
                .replace("%player_world%", player.getWorld().getName())
                .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()));

        message = config.replaceLinksWithMiniMessage(message);
        message = config.parsePlaceholders(player, message);

        return message;
    }

    private List<String> getDefaultMessages(String path) {
        if (path.contains("join") || path.contains("first_join")) {
            return List.of(
                    "<green>➜ %player_name% присоединился к серверу!</green>",
                    "<gold>Рады видеть тебя, %player_name%!</gold>"
            );
        } else if (path.contains("quit")) {
            return List.of(
                    "<red>⬅ %player_name% покинул сервер</red>",
                    "<gray>%player_name% ушёл... Но обещал вернуться!</gray>"
            );
        }
        return List.of("<gray>%player_name%</gray>");
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}