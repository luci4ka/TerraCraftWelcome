package com.welcome.listener;

import com.welcome.config.ConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

public class PlayerDeathListener implements Listener {

    private final ConfigHandler config;
    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PlayerDeathListener(ConfigHandler config, JavaPlugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Проверка метки
        boolean suppressStandardMessage = player.hasMetadata("suppressDeathMessage");

        // Убираем стандартное сообщение
        event.setDeathMessage(null);

        if (!config.getBoolean("messages.global.death", true)) return;

        if (player.getKiller() != null) {
            sendPVPMessage(player, player.getKiller());
        } else if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent dmgEvent) {
            if (dmgEvent.getDamager() instanceof Mob mob) {
                sendMobDeathMessage(player, mob.getType().name());
                return;
            }
        } else {
            broadcastRandomMessage(player, "messages.global.player_kill_message.messages");
        }

        // Эффекты
        config.playEffect(player, "effects.death.sound", "effects.death.particle");

        // Убираем метку после смерти
        if (suppressStandardMessage) {
            player.removeMetadata("suppressDeathMessage", plugin);
        }
    }


    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (config.getBoolean("messages.global.respawn", true)) {
            sendRandomMessageToPlayer(player, "messages.global.respawn_message");
        }
    }

    public void markPlayerForSuppression(Player player) {
        // Ставим метку перед вызовом /kill
        player.setMetadata("suppressDeathMessage", new FixedMetadataValue(plugin, true));
    }

    private void sendPVPMessage(Player victim, Player killer) {
        List<String> messages = config.getMessageList("messages.global.pvp_message.messages");
        if (messages.isEmpty()) return;

        String msg = messages.get(random.nextInt(messages.size()))
                .replace("{killer}", killer.getName())
                .replace("{victim}", victim.getName())
                .replace("{player}", victim.getName())
                .replace("%player_name%", victim.getName());

        msg = config.replaceLinks(msg);
        Component component = miniMessage.deserialize(msg);
        Bukkit.broadcast(component);
    }

    private void sendMobDeathMessage(Player player, String mobType) {
        List<String> messages = config.getMessageList("messages.global.mob_death_message." + mobType);
        if (messages.isEmpty()) {
            messages = config.getMessageList("messages.global.mob_death_message.default");
        }

        for (String msg : messages) {
            msg = msg.replace("%player_name%", player.getName());
            msg = config.replaceLinks(msg);
            Component component = miniMessage.deserialize(msg);
            Bukkit.broadcast(component);
        }
    }

    private void broadcastRandomMessage(Player player, String path) {
        List<String> messages = config.getMessageList(path);
        if (messages.isEmpty()) return;

        String msg = messages.get(random.nextInt(messages.size()));
        msg = msg.replace("%player_name%", player.getName());
        msg = config.replaceLinks(msg);

        Component component = miniMessage.deserialize(msg);
        Bukkit.broadcast(component);
    }
    private void sendRandomMessageToPlayer(Player player, String path) {
        List<String> messages = config.getMessageList(path);
        if (messages.isEmpty()) return;

        String msg = messages.get(random.nextInt(messages.size()))
                .replace("%player_name%", player.getName());
        msg = config.replaceLinks(msg);

        Component component = miniMessage.deserialize(msg);
        player.sendMessage(component);
    }
}
