package luci.welcome;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.Random;

public class EventListener implements Listener {
    private final Main plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public EventListener(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);  // Убираем стандартное сообщение о входе

        // Личное сообщение MOTD
        List<String> motd = plugin.getConfig().getStringList("messages.local.motd");
        for (String line : motd) {
            sendPersonalMessage(player, line);
        }

        // Сообщение о входе для всех
        List<String> joinMessages = plugin.getConfig().getStringList("messages.global.default_join_message");
        sendRandomMessageToAll(joinMessages, player);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.quitMessage(null); // Убираем стандартное сообщение о выходе

        // Сообщение о выходе для всех
        List<String> leaveMessages = plugin.getConfig().getStringList("messages.global.leave_message");
        sendRandomMessageToAll(leaveMessages, player);
        playEffects(player, "leave");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.deathMessage(null);  // Убираем стандартное сообщение о смерти

        if (plugin.getConfig().getBoolean("death.enabled")) {
            // Получаем все строки из конфигурации, связанные с смертью
            List<String> deathMessages = plugin.getConfig().getStringList("messages.death.message");

            // Объединяем все строки в одно сообщение
            StringBuilder fullMessage = new StringBuilder();
            for (String line : deathMessages) {
                fullMessage.append(line).append("\n");
            }

            // Отправляем полное сообщение игроку
            sendPersonalMessage(player, fullMessage.toString());
        }

        playEffects(player, "death");
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("respawn.enabled")) {
            List<String> respawnMessages = plugin.getConfig().getStringList("messages.respawn.message");

            // Объединяем все строки в одно сообщение
            StringBuilder fullMessage = new StringBuilder();
            for (String line : respawnMessages) {
                fullMessage.append(line).append("\n");
            }

            // Отправляем полное сообщение игроку
            sendPersonalMessage(player, fullMessage.toString());
        }

        playEffects(player, "respawn");
    }

    private void sendPersonalMessage(Player player, String message) {
        String parsedMessage = plugin.parsePlaceholders(player, message);
        Component component = miniMessage.deserialize(parsedMessage);  // Преобразуем строку с цветами в Component
        player.sendMessage(component);
    }

    private void sendRandomMessageToAll(List<String> messages, Player sender) {
        if (!messages.isEmpty()) {
            String randomMessage = messages.get(new Random().nextInt(messages.size()));
            randomMessage = randomMessage.replace("{player}", sender.getName());

            // Лог для отладки
            Bukkit.getLogger().info("Broadcasting message: " + randomMessage);

            // Отправляем сообщение всем игрокам
            Bukkit.broadcast(miniMessage.deserialize(randomMessage));  // Преобразуем строку с цветами в Component и отправляем всем
        }
    }

    private void sendRandomMessage(Player player, List<String> messages) {
        if (!messages.isEmpty()) {
            String randomMessage = messages.get(new Random().nextInt(messages.size()));
            String parsedMessage = plugin.parsePlaceholders(player, randomMessage.replace("{player}", player.getName()));
            Component component = miniMessage.deserialize(parsedMessage);  // Преобразуем строку с цветами в Component
            player.sendMessage(component);
        }
    }

    private void playEffects(Player player, String eventType) {
        String soundType = plugin.getConfig().getString("effects." + eventType + ".sound");
        String particleType = plugin.getConfig().getString("effects." + eventType + ".particle");

        if (plugin.getConfig().getBoolean("sound.enable_sound")) {
            try {
                assert soundType != null;
                Sound sound = Sound.valueOf(soundType);
                float volume = (float) plugin.getConfig().getDouble("sound.volume");
                float pitch = (float) plugin.getConfig().getDouble("sound.pitch");
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception ignored) {}
        }

        if (plugin.getConfig().getBoolean("particle_effect.enable_particles")) {
            try {
                Particle particle = Particle.valueOf(particleType);
                int amount = plugin.getConfig().getInt("particle_effect.amount");
                player.getWorld().spawnParticle(particle, player.getLocation(), amount);
            } catch (Exception ignored) {}
        }
    }
}
