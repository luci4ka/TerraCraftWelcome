package ru.net.terracraft.welcome.config;

import ru.net.terracraft.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class PlayerDeathConfig {

    private final ConfigHandler config;
    private final LoggerUtil logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();

    public PlayerDeathConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-PlayerDeath"));
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
        if (victim == null || killer == null) return;

        // ✅ Берём из языкового файла
        List<String> messages = config.getLangMessageList("death.pvp");
        if (messages.isEmpty()) {
            messages = List.of("<red>⚔ {killer} убил {victim} в PvP битве!</red>");
        }

        String template = messages.get(random.nextInt(messages.size()));
        String msg = template
                .replace("{killer}", killer.getName())
                .replace("{victim}", victim.getName())
                .replace("%player_name%", victim.getName());

        msg = config.replaceLinksWithMiniMessage(msg);
        broadcast(msg);
    }

    // -------------------
    // Mob death
    // -------------------
    public void broadcastMobDeath(Player player, Mob mob) {
        if (player == null || mob == null) return;

        String mobKey = mob.getType().name().toUpperCase();

        // ✅ Берём из языкового файла: death.mob.ZOMBIE, death.mob.SKELETON, death.mob.default
        List<String> messages = config.getLangMessageList("death.mob." + mobKey);
        if (messages.isEmpty()) {
            messages = config.getLangMessageList("death.mob.default");
        }
        if (messages.isEmpty()) {
            messages = List.of("<gray>%player_name% был убит мобом</gray>");
        }

        for (String template : messages) {
            String msg = template.replace("%player_name%", player.getName())
                    .replace("{mob}", mob.getName())
                    .replace("%mob%", mob.getName());
            msg = config.replaceLinksWithMiniMessage(msg);
            broadcast(msg);
        }
    }

    // -------------------
    // Environment / generic death
    // -------------------
    public void broadcastEnvironmentDeath(Player player) {
        if (player == null) return;

        // ✅ Берём из языкового файла: death.player_kill
        List<String> messages = config.getLangMessageList("death.player_kill");
        if (messages.isEmpty()) {
            messages = List.of("<gray>☠ %player_name% погиб</gray>");
        }

        String template = messages.get(random.nextInt(messages.size()));
        String msg = template.replace("%player_name%", player.getName());
        msg = config.replaceLinksWithMiniMessage(msg);
        broadcast(msg);
    }

    // -------------------
    // Custom death messages (лава, падение, взрыв...)
    // -------------------
    public void broadcastCustomDeath(Player player, String deathType) {
        if (player == null || deathType == null) return;

        // ✅ Берём из языкового файла: death.lava, death.fall, death.explosion
        List<String> messages = config.getLangMessageList("death." + deathType);
        if (messages.isEmpty()) {
            broadcastEnvironmentDeath(player);
            return;
        }

        String template = messages.get(random.nextInt(messages.size()));
        String msg = template.replace("%player_name%", player.getName());
        msg = config.replaceLinksWithMiniMessage(msg);
        broadcast(msg);
    }

    // -------------------
    // Respawn
    // -------------------
    public void sendRespawn(Player player) {
        if (player == null || !player.isOnline()) return;

        // ✅ Берём из языкового файла: respawn
        List<String> messages = config.getLangMessageList("respawn");
        if (messages.isEmpty()) {
            messages = List.of("<green>Вы возродились!</green>");
        }

        String template = messages.get(random.nextInt(messages.size()));
        String msg = template.replace("%player_name%", player.getName());
        msg = config.replaceLinksWithMiniMessage(msg);

        Component component = miniMessage.deserialize(msg);
        player.sendMessage(component);
    }

    // -------------------
    // Title on death
    // -------------------
    public void sendDeathTitle(Player player) {
        if (player == null || !player.isOnline()) return;

        // ✅ Берём из языкового файла: titles.death.title, titles.death.subtitle
        String title = config.getLangMessage("titles.death.title", "<red>Вы погибли!</red>");
        String subtitle = config.getLangMessage("titles.death.subtitle", "<gray>Но вы ещё вернётесь...</gray>");

        player.sendTitlePart(
                net.kyori.adventure.title.TitlePart.TITLE,
                miniMessage.deserialize(title)
        );
        player.sendTitlePart(
                net.kyori.adventure.title.TitlePart.SUBTITLE,
                miniMessage.deserialize(subtitle)
        );
    }

    // -------------------
    // Effects
    // -------------------
    public void playDeathEffect(Player player) {
        if (player == null || !player.isOnline()) return;
        config.playEffect(player, "settings.effects.death.sound", "settings.effects.death.particle");
    }

    public void playRespawnEffect(Player player) {
        if (player == null || !player.isOnline()) return;
        config.playEffect(player, "settings.effects.respawn.sound", "settings.effects.respawn.particle");
    }

    public void playKillerEffect(Player killer) {
        if (killer == null || !killer.isOnline()) return;
        config.playEffect(killer, "settings.effects.killer.sound", "settings.effects.killer.particle");
    }

    // -------------------
    // Broadcast helper
    // -------------------
    private void broadcast(String msg) {
        if (msg == null || msg.isEmpty()) return;

        try {
            Component component = miniMessage.deserialize(msg);
            Bukkit.broadcast(component);
        } catch (Exception e) {
            logger.error("Ошибка отправки broadcast сообщения: " + msg, e);
        }
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}