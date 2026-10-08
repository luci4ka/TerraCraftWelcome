package ru.net.terracraft.welcome.listener;

import ru.net.terracraft.welcome.Main;
import ru.net.terracraft.welcome.config.PlayerDeathConfig;
import ru.net.terracraft.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PlayerDeathListener implements Listener {

    private final Main plugin;
    private final PlayerDeathConfig deathConfig;
    private final LoggerUtil logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private static final String SUPPRESS_METADATA = "welcome_suppress_death";

    public PlayerDeathListener(Main plugin) {
        this.plugin = plugin;
        this.deathConfig = new PlayerDeathConfig(plugin.getConfigHandler());
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-PlayerDeath"));
    }

    /**
     * Обработка смерти игрока
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Проверяем, нужно ли подавить сообщение
        boolean suppress = hasMetadata(player);

        // Убираем стандартное сообщение о смерти
        event.deathMessage(null);

        // Если сообщения о смерти отключены - выходим
        if (!deathConfig.isDeathEnabled()) {
            if (suppress) removeMetadata(player);
            return;
        }

        // Отправляем титул о смерти
        deathConfig.sendDeathTitle(player);

        // Определяем тип смерти и отправляем сообщение
        Player killer = player.getKiller();

        if (killer != null) {
            // PvP убийство
            deathConfig.broadcastPvpDeath(player, killer);
            deathConfig.playKillerEffect(killer);


        } else if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
            // Смерть от сущности
            handleEntityDeath(player, damageEvent);

        } else {
            // Смерть от окружения (падение, лава, утопление и т.д.)
            deathConfig.broadcastEnvironmentDeath(player);
        }

        if (plugin.getHookManager().isPapiEnabled() && plugin.getHookManager().getPapiHook().getExpansion() != null) {
            String cause = event.getDeathMessage() != null ?
                    event.getDeathMessage().replace(player.getName(), "") : "Неизвестно";
            plugin.getHookManager().getPapiHook().getExpansion().recordDeath(
                    player.getUniqueId(), cause);
        }

        // Эффекты для умершего
        deathConfig.playDeathEffect(player);

        // Убираем метаданные если были
        if (suppress) removeMetadata(player);
    }

    /**
     * Обработка смерти от сущности
     */
    private void handleEntityDeath(Player player, EntityDamageByEntityEvent damageEvent) {
        Entity damager = damageEvent.getDamager();

        if (damager instanceof Mob mob) {
            // Смерть от моба
            deathConfig.broadcastMobDeath(player, mob);
            logger.debug("{} убит мобом: {}");

        } else if (damager instanceof Player killer) {
            // PvP через projectiles (лук, трезубец и т.д.)
            deathConfig.broadcastPvpDeath(player, killer);
            deathConfig.playKillerEffect(killer);

        } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            // Убийство из лука/арбалета игроком
            deathConfig.broadcastPvpDeath(player, shooter);
            deathConfig.playKillerEffect(shooter);

        } else if (damager instanceof TNTPrimed || damager instanceof Explosive) {
            // Смерть от взрыва
            deathConfig.broadcastCustomDeath(player, "explosion");

        } else {
            // Остальные случаи
            deathConfig.broadcastEnvironmentDeath(player);
        }
    }

    /**
     * Обработка возрождения игрока
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (deathConfig.isRespawnEnabled() && player.isOnline()) {
            deathConfig.sendRespawn(player);
            deathConfig.playRespawnEffect(player);
        }
    }

    /**
     * Пометить игрока для подавления сообщения о смерти
     */
    public void markPlayerForSuppression(Player player) {
        if (player != null) {
            player.setMetadata(SUPPRESS_METADATA, new FixedMetadataValue(plugin, true));
        }
    }

    /**
     * Снять подавление сообщения о смерти
     */
    public void unmarkPlayerForSuppression(Player player) {
        if (player != null) {
            removeMetadata(player);
        }
    }

    /**
     * Проверка наличия метаданных
     */
    private boolean hasMetadata(Player player) {
        List<MetadataValue> values = player.getMetadata(PlayerDeathListener.SUPPRESS_METADATA);
        for (MetadataValue value : values) {
            if (value.getOwningPlugin() == plugin) {
                return value.asBoolean();
            }
        }
        return false;
    }

    /**
     * Удаление метаданных
     */
    private void removeMetadata(Player player) {
        player.removeMetadata(PlayerDeathListener.SUPPRESS_METADATA, plugin);
    }

    /**
     * Получить конфиг смерти
     */
    public PlayerDeathConfig getDeathConfig() {
        return deathConfig;
    }
}