package com.luci.welcome.listener;

import com.luci.welcome.Main;
import com.luci.welcome.config.BossFightConfig;
import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class BossFightListener implements Listener {

    private final Main plugin;
    private final BossFightConfig bossConfig;
    private final LoggerUtil logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Map<UUID, Double>> bossDamageMap = new HashMap<>();

    public BossFightListener(Main plugin) {
        this.plugin = plugin;
        this.bossConfig = new BossFightConfig(plugin.getConfigHandler());
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-BossFight"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!bossConfig.isEnabled() || !bossConfig.trackDamage()) return;

        Entity entity = event.getEntity();
        if (!isBoss(entity)) return;

        Player damager = getDamager(event);
        if (damager == null) return;

        double damage = event.getFinalDamage();
        if (damage < bossConfig.getMinDamage()) return;

        UUID bossId = entity.getUniqueId();
        UUID playerId = damager.getUniqueId();

        bossDamageMap.computeIfAbsent(bossId, k -> new HashMap<>())
                .merge(playerId, damage, Double::sum);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBossDeath(EntityDeathEvent event) {
        if (!bossConfig.isEnabled() || !bossConfig.showTopHitters()) return;

        Entity entity = event.getEntity();
        if (!isBoss(entity)) return;

        Map<UUID, Double> damageMap = bossDamageMap.remove(entity.getUniqueId());
        if (damageMap == null || damageMap.isEmpty()) {
            broadcastNoDamageMessage();
            return;
        }

        double totalDamage = damageMap.values().stream().mapToDouble(Double::doubleValue).sum();

        List<Map.Entry<UUID, Double>> topDamage = damageMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(bossConfig.getMaxTopPlayers())
                .collect(Collectors.toList());

        announceBossDeath(entity, topDamage, totalDamage);
        logger.info("Босс {} убит. Топ игроков отправлен", entity.getType().name());

        cleanOldData();
    }

    private Player getDamager(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) return player;
        if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player) return player;
        if (event.getDamager() instanceof Tameable tameable
                && tameable.getOwner() instanceof Player player) return player;
        return null;
    }

    private void announceBossDeath(Entity boss, List<Map.Entry<UUID, Double>> topDamage, double totalDamage) {
        String bossName = getBossName(boss);
        List<Component> messages = new ArrayList<>();

        // Используем готовые компоненты из BossFightConfig
        messages.add(bossConfig.getBossKillMessageComponent(bossName));
        messages.add(bossConfig.getBossTopIntroComponent());

        int position = 1;
        for (Map.Entry<UUID, Double> entry : topDamage) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                double damage = entry.getValue();
                double percent = totalDamage > 0 ? (damage / totalDamage) * 100 : 0;

                // Используем готовый компонент
                messages.add(bossConfig.formatBossTopPlayerAdvancedComponent(
                        position, player.getName(), damage, percent));

                bossConfig.playTopPlayerEffect(player);
                giveRewards(player, position);
                position++;
            }
        }

        // Заключительное сообщение
        messages.add(bossConfig.getBossTopOutroComponent());

        // Title о смерти босса
        sendBossDeathTitle(bossName);

        // Отправка всем игрокам
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            for (Component message : messages) {
                onlinePlayer.sendMessage(message);
            }
            bossConfig.playBossDeathEffect(onlinePlayer);
            bossConfig.playBossSpawnEffect(onlinePlayer);
        }
    }

    private void sendBossDeathTitle(String bossName) {
        // Используем getBossSpawnMessageComponent для консоли
        Bukkit.getConsoleSender().sendMessage(bossConfig.getBossSpawnMessageComponent(bossName));

        Title title = Title.title(
                miniMessage.deserialize(bossConfig.getBossSpawnTitle().replace("{boss_name}", bossName)),
                miniMessage.deserialize(bossConfig.getBossSpawnSubtitle()),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(title);
        }
    }

    private void broadcastNoDamageMessage() {
        // Используем готовый компонент
        Component message = bossConfig.getNoDamageComponent();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    private String getBossName(Entity entity) {
        if (entity instanceof EnderDragon) return "Эндер Дракон";
        if (entity instanceof Wither) return "Визер";
        if (entity instanceof ElderGuardian) return "Древний Страж";
        if (entity instanceof Warden) return "Хранитель";
        return entity.getType().name();
    }

    private boolean isBoss(Entity entity) {
        if (entity == null) return false;
        if (!bossConfig.isBossWorld(entity.getWorld().getName())) return false;

        String entityType = entity.getType().name();
        if (bossConfig.isBossEntity(entityType)) return true;

        return entity instanceof EnderDragon
                || entity instanceof Wither
                || entity instanceof ElderGuardian
                || entity instanceof Warden;
    }

    private void cleanOldData() {
        bossDamageMap.entrySet().removeIf(entry -> {
            UUID bossId = entry.getKey();
            Entity boss = Bukkit.getEntity(bossId);
            return boss == null || boss.isDead();
        });
    }

    public Map<UUID, Double> getBossStats(UUID bossId) {
        return bossDamageMap.getOrDefault(bossId, new HashMap<>());
    }

    private void giveRewards(Player player, int place) {
        if (!bossConfig.isRewardsEnabled()) return;

        String placeKey = "place_" + place;

        // 1. Команды
        List<String> commands = bossConfig.getRewardCommands(placeKey);
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("%player%", player.getName()));
        }

        // 2. Предметы
        List<String> items = bossConfig.getRewardItems(placeKey);
        for (String itemString : items) {
            giveItem(player, itemString);
        }

        // 3. Деньги через Vault
        double money = bossConfig.getRewardMoney(placeKey);
        if (money > 0 && plugin.getHookManager().isVaultEnabled()) {
            plugin.getHookManager().getVaultHook().deposit(player, money);
            player.sendMessage(miniMessage.deserialize(
                    "<gold>+ " + String.format("%.0f", money) + " монет</gold>"
            ));
        }

        // 4. Опыт
        int exp = bossConfig.getRewardExperience(placeKey);
        if (exp > 0) {
            player.giveExp(exp);
            player.sendMessage(miniMessage.deserialize(
                    "<aqua>+ " + exp + " опыта</aqua>"
            ));
        }

        // Итоговое сообщение
        player.sendMessage(miniMessage.deserialize(
                "<gold>🏆 Награда за " + place + " место в битве с боссом!</gold>"
        ));
    }

    /**
     * Выдаёт предмет по строке формата: МАТЕРИАЛ:КОЛИЧЕСТВО:ЧАРА:УРОВЕНЬ
     */
    private void giveItem(Player player, String itemString) {
        try {
            String[] parts = itemString.split(":");
            Material material = Material.valueOf(parts[0].toUpperCase());
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();

            // Чары
            if (parts.length > 2 && meta != null) {
                for (int i = 2; i < parts.length - 1; i += 2) {
                    if (i + 1 < parts.length) {
                        String enchantName = parts[i];
                        int level = Integer.parseInt(parts[i + 1]);
                        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantName.toLowerCase()));
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, level, true);
                        }
                    }
                }
                item.setItemMeta(meta);
            }

            // Выдаём предмет
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                // Если инвентарь полон - кидаем на землю
                for (ItemStack is : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), is);
                }
                player.sendMessage(miniMessage.deserialize("<yellow>⚠ Инвентарь полон! Предметы выброшены рядом.</yellow>"));
            }

        } catch (Exception e) {
            logger.error("Ошибка выдачи предмета '" + itemString + "' игроку " + player.getName(), e);
        }
    }

    public void resetStats() {
        bossDamageMap.clear();
        logger.info("Статистика боссов сброшена");
    }
}