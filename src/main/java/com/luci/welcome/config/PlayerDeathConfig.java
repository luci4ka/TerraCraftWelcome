package com.luci.welcome.config;

import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class PlayerDeathConfig {

    private final ConfigHandler config;
    private final LoggerUtil logger;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Random random = new Random();

    public PlayerDeathConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(config.getPlugin().getSLF4JLogger());
    }

    public boolean isDeathEnabled() {
        return config.getBoolean("messages.global.death", true);
    }

    public boolean isRespawnEnabled() {
        return config.getBoolean("messages.global.respawn", true);
    }

    // -------------------
    // PVP death
    // -------------------
    public void broadcastPvpDeath(Player victim, Player killer) {
        List<String> messages = config.getMessageList("messages.global.pvp_message.messages");
        if (messages.isEmpty()) return;

        String msg = messages.get(random.nextInt(messages.size()))
                .replace("{killer}", killer.getName())
                .replace("{victim}", victim.getName())
                .replace("{player}", victim.getName())
                .replace("%player_name%", victim.getName());

        broadcast(msg);
        logger.info("PVP-сообщение: " + killer.getName() + " убил " + victim.getName());
    }

    // -------------------
    // Mob death
    // -------------------
    public void broadcastMobDeath(Player player, Mob mob) {
        String mobKey = mob.getType().name().toUpperCase();
        List<String> messages = config.getMessageList("messages.global.mob_death_message." + mobKey);
        if (messages.isEmpty()) {
            messages = config.getMessageList("messages.global.mob_death_message.default");
        }

        for (String msg : messages) {
            msg = msg.replace("%player_name%", player.getName());
            broadcast(msg);
        }

        logger.info("Сообщение о смерти игрока " + player.getName() + " от моба " + mob.getType().name() + " отправлено");
    }

    // -------------------
    // Environment / generic death
    // -------------------
    public void broadcastEnvironmentDeath(Player player) {
        List<String> messages = config.getMessageList("messages.global.player_kill_message.messages");
        if (messages.isEmpty()) return;

        String msg = messages.get(random.nextInt(messages.size()))
                .replace("%player_name%", player.getName());

        broadcast(msg);
    }

    // -------------------
    // Respawn
    // -------------------
    public void sendRespawn(Player player) {
        List<String> messages = config.getMessageList("messages.global.respawn_message");
        if (messages.isEmpty()) return;

        String msg = messages.get(random.nextInt(messages.size()))
                .replace("%player_name%", player.getName());

        player.sendMessage(mini.deserialize(msg));
        logger.info("Игрок " + player.getName() + " возродился");
    }

    // -------------------
    // Effects
    // -------------------
    public void playDeathEffect(Player player) {
        config.playEffect(player, "settings.effects.death.sound", "settings.effects.death.particle");
    }

    // -------------------
    // Вспомогательные
    // -------------------
    private void broadcast(String msg) {
        Bukkit.broadcast(mini.deserialize(msg));
    }
}
