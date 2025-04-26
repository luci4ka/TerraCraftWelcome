package luci.welcome;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Random;

public class EventListener implements Listener {
    private final Main plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Audience audience = Bukkit.getServer(); // Отправляем сообщения через Adventure API
    private final Random random = new Random();

    public EventListener(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("EventListener загружен и зарегистрирован!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null); // Убираем стандартное сообщение

        sendMOTD(player);
        broadcastRandomJoinMessage(player);
        playSound(player);
        spawnParticles(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        String message = plugin.getConfig().getString("messages.leave_message");
        if (message != null) {
            broadcastFormattedMessage(message, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.setDeathMessage(null);

        String message = plugin.getConfig().getString("messages.death_message");
        if (message != null) {
            broadcastFormattedMessage(message, player);
        }
    }

    private void sendMOTD(Player player) {
        String motd = plugin.getConfig().getString("messages.motd");
        if (motd != null && !motd.isEmpty()) {
            Component motdComponent = miniMessage.deserialize(motd);
            player.sendMessage(motdComponent);
        } else {
            plugin.getLogger().warning("MOTD не найден в конфиге.");
        }
    }

    private void broadcastRandomJoinMessage(Player player) {
        List<String> joinMessages = plugin.getConfig().getStringList("messages.default_join_message");
        if (!joinMessages.isEmpty()) {
            String randomMessage = joinMessages.get(random.nextInt(joinMessages.size()));
            broadcastFormattedMessage(randomMessage, player);
        } else {
            plugin.getLogger().warning("Приветственные сообщения не найдены в конфиге.");
        }
    }

    private void broadcastFormattedMessage(String rawMessage, Player player) {
        if (rawMessage == null || rawMessage.isEmpty()) return;
        String formattedMessage = rawMessage.replace("{player}", player.getName());
        Component component = miniMessage.deserialize(formattedMessage);
        audience.sendMessage(component); // Через Adventure API отправляем сразу всем
    }

    private void playSound(Player player) {
        if (!plugin.getConfig().getBoolean("sound.enable_sound")) return;

        String soundType = plugin.getConfig().getString("sound.sound_type");
        float volume = (float) plugin.getConfig().getDouble("sound.volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble("sound.pitch", 1.0);

        try {
            Sound sound = Sound.valueOf(soundType);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException | NullPointerException e) {
            plugin.getLogger().warning("Не удалось воспроизвести звук: " + soundType);
        }
    }

    private void spawnParticles(Player player) {
        if (!plugin.getConfig().getBoolean("particle_effect.enable_particles")) return;

        String particleType = plugin.getConfig().getString("particle_effect.particle_type");
        int amount = plugin.getConfig().getInt("particle_effect.amount", 10);

        try {
            Particle particle = Particle.valueOf(particleType);
            player.getWorld().spawnParticle(particle, player.getLocation(), amount);
        } catch (IllegalArgumentException | NullPointerException e) {
            plugin.getLogger().warning("Не удалось воспроизвести эффект частиц: " + particleType);
        }
    }
}
