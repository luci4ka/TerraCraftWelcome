package com.welcome.listener;

import com.welcome.config.ConfigHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Player;

import java.util.*;

public class BossFightListener implements Listener {

    private final ConfigHandler config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Map<UUID, Double>> bossDamageMap = new HashMap<>();

    public BossFightListener(ConfigHandler config) {
        this.config = config;
    }

    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!config.getBoolean("boss_fight.track_damage", true)) return;

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
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!config.getBoolean("boss_fight.show_top_hitters", true)) return;

        Entity entity = event.getEntity();
        if (!isBoss(entity)) return;

        Map<UUID, Double> damageMap = bossDamageMap.remove(entity.getUniqueId());
        if (damageMap == null || damageMap.isEmpty()) return;

        announceBossDeath(entity, damageMap);
    }

    private void announceBossDeath(Entity boss, Map<UUID, Double> damageMap) {
        double totalDamage = damageMap.values().stream().mapToDouble(Double::doubleValue).sum();
        List<String> lines = new ArrayList<>();

        // Заголовок
        String headerTemplate = config.getString("boss_fight.messages.boss_kill",
                "<gold>⚔️ Победа над боссом: <red>{boss_name}</red>!</gold>");
        lines.add(headerTemplate.replace("{boss_name}", boss.getType().name()));

        // Подпись
        String introTemplate = config.getString("boss_fight.messages.boss_top_intro",
                "<green>Игроки, нанесшие урон:</green>");
        lines.add(introTemplate);

        // Сортировка и форматирование
        damageMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null) {
                        double damage = entry.getValue();
                        double percent = (damage / totalDamage) * 100;
                        String playerTemplate = config.getString("boss_fight.messages.boss_top_player",
                                "<yellow>{player_name}</yellow>: {damage}❤ (<gray>{percent}%</gray>)");

                        String line = playerTemplate
                                .replace("{player_name}", player.getName())
                                .replace("{damage}", String.format("%.1f", damage))
                                .replace("{percent}", String.format("%.1f", percent));
                        lines.add(line);
                    }
                });

        // Отправка
        for (Player player : Bukkit.getOnlinePlayers()) {
            lines.forEach(line -> player.sendMessage(miniMessage.deserialize(line)));
        }

        // Эффекты
        for (Player player : Bukkit.getOnlinePlayers()) {
            config.playEffect(player, "effects.death.sound", "effects.death.particle");
        }
    }

    private boolean isBoss(Entity entity) {
        return entity instanceof EnderDragon
                || entity instanceof Wither
                || entity instanceof ElderGuardian;
    }
}
