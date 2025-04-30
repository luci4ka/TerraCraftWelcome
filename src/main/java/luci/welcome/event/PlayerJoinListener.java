package luci.welcome.event;

import luci.welcome.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Particle;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class PlayerJoinListener implements Listener {
    private final Main plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();

    public PlayerJoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);

        boolean isFirstJoin = !player.hasPlayedBefore();

        // Проверка: motd.enabled
        ConfigurationSection motdSection = plugin.getConfig().getConfigurationSection("messages.local.motd");
        if (motdSection != null && motdSection.getBoolean("enabled", true)) {
            List<String> motdLines = motdSection.getStringList("lines");
            sendPersonalMOTD(player, motdLines);
        }

        if (isFirstJoin) {
            List<String> firstJoinMessages = plugin.getConfig().getStringList("messages.global.first_join_message");
            broadcastRandomMessage(firstJoinMessages, player);
            playEffect(player, "effects.first_join.sound", "effects.first_join.particle");
        } else {
            if (plugin.getConfig().getBoolean("messages.global.join", true)) {
                broadcastJoinMessage(player);
            }
            playEffect(player, "effects.normal_join.sound", "effects.normal_join.particle");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.quitMessage(null);

        if (plugin.getConfig().getBoolean("messages.global.quit", true)) {
            broadcastQuitMessage(player);
        }

        playEffect(player, "effects.leave.sound", "effects.leave.particle");
    }

    private void sendPersonalMOTD(Player player, List<String> motdLines) {
        if (motdLines == null || motdLines.isEmpty()) return;

        Map<String, String> clickableLinks = getConfigLinks();

        for (String line : motdLines) {
            if (line == null || line.isEmpty()) continue;

            String parsedLine = line.replace("%player_name%", player.getName());

            for (Map.Entry<String, String> entry : clickableLinks.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.endsWith("_url")) {
                    parsedLine = parsedLine.replace("%" + key + "%", "<click:open_url:" + value + "><underlined><blue>" + value + "</blue></underlined></click>");
                } else {
                    parsedLine = parsedLine.replace("%" + key + "%", value);
                }
            }

            parsedLine = plugin.parsePlaceholders(player, parsedLine);
            player.sendMessage(miniMessage.deserialize(parsedLine));
        }
    }

    private Map<String, String> getConfigLinks() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("links");
        if (section == null) return Map.of();

        return section.getValues(false).entrySet().stream()
                .filter(entry -> entry.getValue() instanceof String)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (String) entry.getValue()
                ));
    }

    private void broadcastRandomMessage(List<String> messages, Player player) {
        if (messages == null || messages.isEmpty()) return;

        String message = messages.get(random.nextInt(messages.size()));
        String parsedMessage = plugin.parsePlaceholders(player, message.replace("%player_name%", player.getName()));
        Bukkit.broadcast(miniMessage.deserialize(parsedMessage));
    }

    private void broadcastJoinMessage(Player player) {
        List<String> messages;

        if (player.hasPermission("welcome.admin")) {
            messages = plugin.getConfig().getStringList("messages.global.admin_join_message");
        } else if (player.hasPermission("welcome.vip")) {
            messages = plugin.getConfig().getStringList("messages.global.vip_join_message");
        } else {
            messages = plugin.getConfig().getStringList("messages.global.default_join_message");
        }

        broadcastRandomMessage(messages, player);
    }

    private void broadcastQuitMessage(Player player) {
        List<String> messages;

        if (player.hasPermission("welcome.admin")) {
            messages = plugin.getConfig().getStringList("messages.global.admin_quit_message");
        } else if (player.hasPermission("welcome.vip")) {
            messages = plugin.getConfig().getStringList("messages.global.vip_quit_message");
        } else {
            messages = plugin.getConfig().getStringList("messages.global.default_quit_message");
        }

        broadcastRandomMessage(messages, player);
    }

    private void playEffect(Player player, String soundPath, String particlePath) {
        if (plugin.getConfig().getBoolean("sound.enable_sound", true)) {
            String soundName = plugin.getConfig().getString(soundPath);
            if (soundName != null && !soundName.isEmpty()) {
                try {
                    Sound sound = Sound.valueOf(soundName.toUpperCase());
                    float volume = (float) plugin.getConfig().getDouble("sound.volume", 1.0);
                    float pitch = (float) plugin.getConfig().getDouble("sound.pitch", 1.0);
                    player.playSound(player.getLocation(), sound, volume, pitch);
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning("[Welcome] Неверное имя звука: " + soundName);
                }
            }
        }

        if (plugin.getConfig().getBoolean("particle_effect.enable_particles", true)) {
            String particleName = plugin.getConfig().getString(particlePath);
            if (particleName != null && !particleName.isEmpty()) {
                try {
                    Particle particle = Particle.valueOf(particleName.toUpperCase());
                    int amount = plugin.getConfig().getInt("particle_effect.amount", 5);
                    player.getWorld().spawnParticle(particle, player.getLocation(), amount);
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning("[Welcome] Неверное имя частицы: " + particleName);
                }
            }
        }
    }
}
