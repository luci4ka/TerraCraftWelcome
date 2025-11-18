package com.luci.welcome.listener;

import com.luci.welcome.config.ConfigHandler;
import com.luci.welcome.config.PlayerDeathConfig;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDeathListener implements Listener {

    private final PlayerDeathConfig deathConfig;
    private final JavaPlugin plugin;

    public PlayerDeathListener(ConfigHandler config, JavaPlugin plugin) {
        this.deathConfig = new PlayerDeathConfig(config);
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        boolean suppress = player.hasMetadata("suppressDeathMessage");
        event.setDeathMessage(null);

        if (!deathConfig.isDeathEnabled()) return;

        if (player.getKiller() != null) {
            deathConfig.broadcastPvpDeath(player, player.getKiller());
        } else if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent dmgEvent) {
            if (dmgEvent.getDamager() instanceof Mob mob) {
                deathConfig.broadcastMobDeath(player, mob);
                return;
            } else {
                deathConfig.broadcastEnvironmentDeath(player);
            }
        } else {
            deathConfig.broadcastEnvironmentDeath(player);
        }

        deathConfig.playDeathEffect(player);

        if (suppress) {
            player.removeMetadata("suppressDeathMessage", plugin);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (deathConfig.isRespawnEnabled()) {
            deathConfig.sendRespawn(event.getPlayer());
        }
    }

    public void markPlayerForSuppression(Player player) {
        player.setMetadata("suppressDeathMessage", new FixedMetadataValue(plugin, true));
    }
}
