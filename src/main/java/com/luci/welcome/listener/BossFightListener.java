package com.luci.welcome.listener;

import com.luci.welcome.config.BossFightConfig;
import com.luci.welcome.config.ConfigHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.slf4j.Logger;

import java.util.*;

public class BossFightListener implements Listener {

    private final ConfigHandler config;
    private final BossFightConfig bossConfig;
    private final Logger logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Map<UUID, Double>> bossDamageMap = new HashMap<>();

    public BossFightListener(ConfigHandler config) {
        this.config = config;
        this.bossConfig = new BossFightConfig(config);
        this.logger = config.getPlugin().getSLF4JLogger();
    }

    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!bossConfig.trackDamage()) return;

        Entity entity = event.getEntity();
        if (!isBoss(entity)) return;

        Player damager = null;
        if (event.getDamager() instanceof Player player) {
            damager = player;
        } else if (event.getDamager() instanceof Projectile proj) {
            if (proj.getShooter() instanceof Player player) damager = player;
        }

        if (damager == null) return;

        UUID bossId = entity.getUniqueId();
        UUID playerId = damager.getUniqueId();

        bossDamageMap.putIfAbsent(bossId, new HashMap<>());
        bossDamageMap.get(bossId).merge(playerId, event.getFinalDamage(), Double::sum);

        logger.debug("Игрок {} нанес {}❤ урону боссу {}", damager.getName(), event.getFinalDamage(), entity.getType().name());
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!bossConfig.showTopHitters()) return;

        Entity entity = event.getEntity();
        if (!isBoss(entity)) return;

        Map<UUID, Double> damageMap = bossDamageMap.remove(entity.getUniqueId());
        if (damageMap == null || damageMap.isEmpty()) return;

        announceBossDeath(entity, damageMap);
        logger.info("Босс {} был убит, статистика урона отправлена", entity.getType().name());
    }

    private void announceBossDeath(Entity boss, Map<UUID, Double> damageMap) {
        double totalDamage = damageMap.values().stream().mapToDouble(Double::doubleValue).sum();
        List<String> lines = new ArrayList<>();

        // Заголовок
        lines.add(bossConfig.getBossKillMessage(boss.getType().name()));

        // Вступление
        lines.add(bossConfig.getBossTopIntro());

        // Игроки
        damageMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null) {
                        double damage = entry.getValue();
                        double percent = (damage / totalDamage) * 100;

                        String line = bossConfig.formatBossTopPlayer(
                                player.getName(),
                                damage,
                                percent
                        );
                        lines.add(line);
                    }
                });

        // Отправка сообщений
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String line : lines) {
                try {
                    player.sendMessage(miniMessage.deserialize(line));
                } catch (Exception e) {
                    logger.warn("Ошибка отправки сообщения игроку {}: {}", player.getName(), e.getMessage());
                }
            }
        }

        // Эффекты
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossConfig.playBossDeathEffect(player);
        }
    }

    private boolean isBoss(Entity entity) {
        return entity instanceof EnderDragon
                || entity instanceof Wither
                || entity instanceof ElderGuardian;
    }
}
