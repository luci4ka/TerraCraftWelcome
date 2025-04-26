package luci.welcome;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class EventListener implements Listener {
    private final Main plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();

    public EventListener(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("EventListener загружен и зарегистрирован!");
    }

    private String parsePlaceholders(Player player, String message) {
        if (plugin.getConfig().getBoolean("placeholderapi_support") && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        return message.replace("{player}", player.getName());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null);

        if (plugin.getConfig().getBoolean("messages.enable_motd")) {
            String motd = plugin.getConfig().getString("messages.motd", "");
            if (!motd.isEmpty()) {
                motd = parsePlaceholders(player, motd);
                player.sendMessage(miniMessage.deserialize(motd));
            }
        }

        if (plugin.getConfig().getBoolean("messages.enable_join_message")) {
            List<String> joinMessages = plugin.getConfig().getStringList("messages.default_join_message");
            if (!joinMessages.isEmpty()) {
                String joinMessage = joinMessages.get(random.nextInt(joinMessages.size()));
                joinMessage = parsePlaceholders(player, joinMessage);
                Bukkit.broadcast(miniMessage.deserialize(joinMessage));
            }
        }

        int delay = plugin.getConfig().getInt("effects.delay_ticks", 0);
        if (delay > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    playEffects(player);
                }
            }.runTaskLater(plugin, delay);
        } else {
            playEffects(player);
        }
    }

    private void playEffects(Player player) {
        if (plugin.getConfig().getBoolean("sound.enable_sound")) {
            try {
                String soundName = plugin.getConfig().getString("sound.sound_type");
                float volume = (float) plugin.getConfig().getDouble("sound.volume", 1.0);
                float pitch = (float) plugin.getConfig().getDouble("sound.pitch", 1.0);
                player.playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при воспроизведении звука для игрока " + player.getName());
            }
        }

        if (plugin.getConfig().getBoolean("particle_effect.enable_particles")) {
            try {
                String particleName = plugin.getConfig().getString("particle_effect.particle_type");
                int amount = plugin.getConfig().getInt("particle_effect.amount", 20);
                player.getWorld().spawnParticle(Particle.valueOf(particleName), player.getLocation(), amount);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при спавне частиц для игрока " + player.getName());
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        if (plugin.getConfig().getBoolean("messages.enable_leave_message")) {
            String leaveMessage = plugin.getConfig().getString("messages.leave_message", "");
            leaveMessage = parsePlaceholders(player, leaveMessage);
            Bukkit.broadcast(miniMessage.deserialize(leaveMessage));
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.setDeathMessage(null);

        if (plugin.getConfig().getBoolean("messages.enable_death_message")) {
            String deathMessage = plugin.getConfig().getString("messages.death_message", "");
            deathMessage = parsePlaceholders(player, deathMessage);
            Bukkit.broadcast(miniMessage.deserialize(deathMessage));
        }
    }
}
