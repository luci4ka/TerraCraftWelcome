package luci.welcome.event;

import luci.welcome.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class PlayerDeathListener implements Listener {
    private final Main plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Logger logger;
    private final Random random = new Random();
    private final boolean placeholderApiEnabled;

    public PlayerDeathListener(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage() != null ? event.getDeathMessage() : "";
        logger.info("Обработка смерти игрока: " + player.getName() + " | Оригинальное сообщение смерти: " + deathMessage);

        if (!plugin.getConfig().getBoolean("global.death", true)) {
            logger.info("Глобальная смерть отключена в конфиге. Пропускаем обработку.");
            return;
        }

        // Убираем стандартное сообщение смерти
        event.setDeathMessage(null);

        // Обработка смерти от PvP
        if (player.getKiller() != null) {
            List<String> pvpMessages = plugin.getConfig().getStringList("messages.global.pvp_message.messages");
            logger.info("Обработка смерти от PvP для игрока: " + player.getName());
            sendRandomMessage(player, player.getKiller(), pvpMessages);
        }
        // Обработка смерти от падения
        else if (deathMessage.toLowerCase().contains("fell from a high place")) {
            List<String> fallMessages = plugin.getConfig().getStringList("messages.global.fall_message.messages");
            logger.info("Обработка смерти от падения для игрока: " + player.getName());
            sendRandomMessage(player, fallMessages);
        }
        // Обработка смерти от лавы
        else if (deathMessage.toLowerCase().contains("tried to swim in lava") ||
                deathMessage.toLowerCase().contains("burned to death") ||
                deathMessage.toLowerCase().contains("went up in flames") ||
                deathMessage.toLowerCase().contains("was burned to death")) {
            List<String> lavaMessages = plugin.getConfig().getStringList("messages.global.lava_message.messages");
            logger.info("Обработка смерти от лавы для игрока: " + player.getName());
            sendRandomMessage(player, lavaMessages);
        }
        // Обработка смерти от моба
        else if (event.getEntity().getKiller() == null && event.getEntity().getLastDamageCause() != null &&
                event.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            EntityDamageEvent lastDamage = player.getLastDamageCause();
            if (lastDamage instanceof EntityDamageByEntityEvent entityDamageByEntityEvent) {
                if (entityDamageByEntityEvent.getDamager() instanceof Mob mob) {
                    String mobType = mob.getType().name(); // например, "SKELETON"
                    logger.info("Смерть от моба: " + mobType);

                    List<String> mobDeathMessages = plugin.getConfig().getStringList("messages.global.mob_death_message." + mobType);
                    if (mobDeathMessages.isEmpty()) {
                        mobDeathMessages = plugin.getConfig().getStringList("messages.global.mob_death_message.default");
                    }

                    logger.info("Обработка смерти от моба для игрока: " + player.getName() + " от " + mobType);
                    sendRandomMessage(player, mobDeathMessages);
                    return;
                }
            }
        }
        // Обработка других случаев смерти
        else {
            List<String> deathMessages = plugin.getConfig().getStringList("messages.global.player_kill_message.messages");
            logger.info("Обработка общей смерти для игрока: " + player.getName());
            sendRandomMessage(player, deathMessages);
        }

        handleDeathEffects(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("global.respawn", true)) {
            List<String> respawnMessages = plugin.getConfig().getStringList("messages.respawn_message.messages");
            logger.info("Обработка респауна для игрока: " + player.getName());
            sendRandomMessage(player, respawnMessages);
        }
    }

    private void sendRandomMessage(Player player, List<String> messages) {
        if (messages.isEmpty()) {
            logger.warning("Список сообщений пуст для игрока: " + player.getName() + " | Проверь конфиг.");
            return;
        }
        String randomMessage = messages.get(random.nextInt(messages.size()));
        String parsedMessage = applyPlaceholders(player, randomMessage);
        Component component = miniMessage.deserialize(parsedMessage);
        Bukkit.broadcast(component);
        logger.info("Отправлено сообщение для игрока: " + player.getName() + " | Сообщение: " + parsedMessage);
    }

    private void sendRandomMessage(Player player, Player killer, List<String> messages) {
        if (messages.isEmpty()) {
            logger.warning("Список PvP сообщений пуст для игрока: " + player.getName());
            return;
        }
        String randomMessage = messages.get(random.nextInt(messages.size()))
                .replace("{killer}", killer.getName())
                .replace("{victim}", player.getName())
                .replace("{player}", player.getName())
                .replace("%player_name%", player.getName());
        String parsedMessage = applyPlaceholders(player, randomMessage);
        Component component = miniMessage.deserialize(parsedMessage);
        Bukkit.broadcast(component);
        logger.info("Отправлено PvP сообщение для игрока: " + player.getName() + " | Сообщение: " + parsedMessage);
    }

    private void handleDeathEffects(Player player) {
        if (plugin.getConfig().getBoolean("particle_effect.enable_particles", true)) {
            String particleName = plugin.getConfig().getString("effects.death.particle");
            if (particleName == null) particleName = "SMOKE_NORMAL";
            int amount = plugin.getConfig().getInt("particle_effect.amount", 5);

            try {
                Particle particle = Particle.valueOf(particleName.toUpperCase());
                player.getWorld().spawnParticle(particle, player.getLocation(), amount);
                logger.info("Появились частицы смерти для игрока: " + player.getName() + " | Частица: " + particleName);
            } catch (IllegalArgumentException e) {
                logger.warning("Неверное название частицы: " + particleName);
            }
        }

        if (plugin.getConfig().getBoolean("sound.enable_sound", true)) {
            String soundName = plugin.getConfig().getString("effects.death.sound");
            if (soundName == null) soundName = "ENTITY_PLAYER_HURT";
            float volume = (float) plugin.getConfig().getDouble("sound.volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("sound.pitch", 1.0);

            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
                logger.info("Проигран звук смерти для игрока: " + player.getName() + " | Звук: " + soundName);
            } catch (IllegalArgumentException e) {
                logger.warning("Неверное название звука: " + soundName);
            }
        }
    }

    private String applyPlaceholders(Player player, String message) {
        if (placeholderApiEnabled) {
            logger.info("PlaceholderAPI найден. Применяю плейсхолдеры.");
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        logger.info("PlaceholderAPI не найден. Плейсхолдеры не будут применены.");
        return message;
    }
}
