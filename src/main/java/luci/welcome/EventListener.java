package luci.welcome;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

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
        event.setJoinMessage(null);

        boolean isFirstJoin = !player.hasPlayedBefore();
        if (isFirstJoin) {
            handleFirstJoin(player);
        } else {
            handleNormalJoin(player);
        }

        playEffects(player);
    }

    private void handleFirstJoin(Player player) {
        String motd = plugin.getConfig().getString("messages.first_join_motd");
        if (motd != null && !motd.isEmpty()) {
            sendMessage(player, motd);
        }

        List<String> joinMessages = plugin.getConfig().getStringList("messages.first_join_message");
        if (!joinMessages.isEmpty()) {
            String random = joinMessages.get(new Random().nextInt(joinMessages.size()));
            broadcastMessage(player, random);
        }
    }

    private void handleNormalJoin(Player player) {
        String motd = plugin.getConfig().getString("messages.motd");
        if (motd != null && !motd.isEmpty()) {
            sendMessage(player, motd);
        }

        List<String> joinMessages = plugin.getConfig().getStringList("messages.default_join_message");
        if (!joinMessages.isEmpty()) {
            String random = joinMessages.get(new Random().nextInt(joinMessages.size()));
            broadcastMessage(player, random);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        String leave = plugin.getConfig().getString("messages.leave_message");
        if (leave != null) {
            broadcastMessage(player, leave);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.setDeathMessage(null);

        String death = plugin.getConfig().getString("messages.death_message");
        if (death != null) {
            broadcastMessage(player, death);
        }
    }

    public void playEffects(Player player) {
        if (plugin.getConfig().getBoolean("sound.enable_sound")) {
            try {
                Sound sound = Sound.valueOf(plugin.getConfig().getString("sound.sound_type"));
                float volume = (float) plugin.getConfig().getDouble("sound.volume");
                float pitch = (float) plugin.getConfig().getDouble("sound.pitch");
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception ignored) {}
        }

        if (plugin.getConfig().getBoolean("particle_effect.enable_particles")) {
            try {
                Particle particle = Particle.valueOf(plugin.getConfig().getString("particle_effect.particle_type"));
                int amount = plugin.getConfig().getInt("particle_effect.amount");
                player.getWorld().spawnParticle(particle, player.getLocation(), amount);
            } catch (Exception ignored) {}
        }
    }

    private void sendMessage(Player player, String message) {
        String parsed = plugin.parsePlaceholders(player, message);
        Component component = miniMessage.deserialize(parsed);
        player.sendMessage(component);
    }

    private void broadcastMessage(Player player, String message) {
        String parsed = plugin.parsePlaceholders(player, message.replace("{player}", player.getName()));
        Component component = miniMessage.deserialize(parsed);
        Bukkit.broadcast(component);
    }
}
