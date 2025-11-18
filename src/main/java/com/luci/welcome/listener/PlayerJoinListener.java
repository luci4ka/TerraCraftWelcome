package com.luci.welcome.listener;

import com.luci.welcome.config.ConfigHandler;
import com.luci.welcome.config.PlayerJoinConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final PlayerJoinConfig joinConfig;

    public PlayerJoinListener(ConfigHandler config) {
        this.joinConfig = new PlayerJoinConfig(config);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);

        boolean firstJoin = !player.hasPlayedBefore();
        joinConfig.sendMOTD(player);

        if (firstJoin) {
            joinConfig.broadcastRandomMessage(player, "messages.global.first_join_message");
            joinConfig.playJoinEffects(player, true);
        } else {
            if (joinConfig.isJoinEnabled()) {
                joinConfig.broadcastPermissionMessage(player, "join");
            }
            joinConfig.playJoinEffects(player, false);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.quitMessage(null);

        if (joinConfig.isQuitEnabled()) {
            joinConfig.broadcastPermissionMessage(player, "quit");
        }
        joinConfig.playLeaveEffects(player);
    }
}
