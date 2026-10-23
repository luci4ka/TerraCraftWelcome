package ru.terracraft.welcome.core.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.api.event.WelcomeDeathEvent;
import ru.terracraft.welcome.config.MainConfig;
import ru.terracraft.welcome.core.lang.LanguageManager;
import ru.terracraft.welcome.core.lang.MessageKey;
import ru.terracraft.welcome.core.integration.IntegrationManager;

import java.util.List;
import java.util.Random;

/**
 * Сервис смерти и возрождения.
 * Все сообщения — на локали получателя.
 */
public class DeathService {

    private final WelcomePlugin plugin;
    private final MainConfig config;
    private final LanguageManager lang;
    private final IntegrationManager integrations;
    private final Random random = new Random();

    public DeathService(WelcomePlugin plugin, MainConfig config,
                        LanguageManager lang, IntegrationManager integrations) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.integrations = integrations;
    }

    // ═══════════════ PUBLIC API ═══════════════

    public void handleDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.deathMessage(null);

        if (!config.death.death) return;

        DeathCause cause = detectCause(event);
        Player killer = player.getKiller();

        WelcomeDeathEvent welcomeEvent = new WelcomeDeathEvent(
                player,
                killer,
                cause.name().toLowerCase()
        );
        try {
            Bukkit.getPluginManager().callEvent(welcomeEvent);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка вызова WelcomeDeathEvent: " + e.getMessage());
        }

        if (welcomeEvent.isCancelled()) return;

        if (config.death.titles) {
            sendDeathTitle(player);
        }

        switch (cause) {
            case PVP, PROJECTILE_PVP -> {
                if (killer != null) {
                    broadcastPvp(player, killer);
                    playKillerEffect(killer);
                } else {
                    broadcastEnvironment(player);
                }
            }
            case MOB -> {
                if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent dmg
                        && dmg.getDamager() instanceof Mob mob) {
                    broadcastMob(player, mob);
                } else {
                    broadcastEnvironment(player);
                }
            }
            case EXPLOSION -> broadcastCustom(player, MessageKey.DEATH_EXPLOSION);
            case LAVA      -> broadcastCustom(player, MessageKey.DEATH_LAVA);
            case FALL      -> broadcastCustom(player, MessageKey.DEATH_FALL);
            default        -> broadcastEnvironment(player);
        }

        playDeathEffect(player);
        recordDeath(player, cause);
    }

    public void handleRespawn(Player player) {
        if (!config.death.respawn) return;
        if (player == null || !player.isOnline()) return;

        sendRespawn(player);
        playRespawnEffect(player);
    }

    // ═══════════════ CAUSE DETECTOR ═══════════════

    public enum DeathCause {
        PVP, MOB, PROJECTILE_PVP, EXPLOSION, LAVA,
        FALL, DROWNING, FIRE, VOID, ENVIRONMENT
    }

    private DeathCause detectCause(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Player killer = player.getKiller();
        if (killer != null) return DeathCause.PVP;

        EntityDamageEvent lastDamage = player.getLastDamageCause();
        if (lastDamage == null) return DeathCause.ENVIRONMENT;

        if (lastDamage instanceof EntityDamageByEntityEvent damageByEntity) {
            Entity damager = damageByEntity.getDamager();

            if (damager instanceof Mob) return DeathCause.MOB;
            if (damager instanceof Player) return DeathCause.PVP;

            if (damager instanceof Projectile projectile
                    && projectile.getShooter() instanceof Player) {
                return DeathCause.PROJECTILE_PVP;
            }

            if (damager instanceof TNTPrimed) return DeathCause.EXPLOSION;
        }

        EntityDamageEvent.DamageCause damageCause = lastDamage.getCause();
        if (damageCause == null) return DeathCause.ENVIRONMENT;

        return switch (damageCause) {
            case LAVA -> DeathCause.LAVA;
            case FALL -> DeathCause.FALL;
            case DROWNING -> DeathCause.DROWNING;
            case FIRE, FIRE_TICK -> DeathCause.FIRE;
            case VOID -> DeathCause.VOID;
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> DeathCause.EXPLOSION;
            default -> DeathCause.ENVIRONMENT;
        };
    }

    // ═══════════════ MESSAGES ═══════════════

    private void broadcastPvp(Player victim, Player killer) {
        if (victim == null || killer == null) return;

        for (Player online : Bukkit.getOnlinePlayers()) {
            try {
                List<String> messages = lang.getMessageList(online, MessageKey.DEATH_PVP);
                if (messages.isEmpty()) continue;

                String template = messages.get(random.nextInt(messages.size()));
                String prepared = template
                        .replace("{killer}", killer.getName())
                        .replace("{victim}", victim.getName());

                online.sendMessage(renderFor(online, prepared, victim));
            } catch (Exception ignored) {}
        }
    }

    private void broadcastMob(Player victim, Mob mob) {
        if (victim == null || mob == null) return;

        String mobKey = mob.getType().name();

        for (Player online : Bukkit.getOnlinePlayers()) {
            try {
                List<String> messages = lang.getMessageList(online, "death.mob." + mobKey);
                if (messages.isEmpty()) {
                    messages = lang.getMessageList(online, MessageKey.DEATH_MOB_DEFAULT);
                }
                if (messages.isEmpty()) continue;

                String template = messages.get(random.nextInt(messages.size()));
                String prepared = template
                        .replace("%player_name%", victim.getName())
                        .replace("{mob}", mob.getName())
                        .replace("%mob%", mob.getName());

                online.sendMessage(renderFor(online, prepared, victim));
            } catch (Exception ignored) {}
        }
    }

    private void broadcastEnvironment(Player victim) {
        if (victim == null) return;
        broadcastKey(MessageKey.DEATH_ENVIRONMENT, victim);
    }

    private void broadcastCustom(Player victim, MessageKey key) {
        if (victim == null) return;
        broadcastKey(key, victim);
    }

    /**
     * Общий метод: broadcast по локали каждого получателя.
     * PAPI-контекст — victim.
     */
    private void broadcastKey(MessageKey key, Player victim) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            try {
                List<String> messages = lang.getMessageList(online, key);
                if (messages.isEmpty()) continue;

                String template = messages.get(random.nextInt(messages.size()));
                online.sendMessage(renderFor(online, template, victim));
            } catch (Exception ignored) {}
        }
    }

    private void sendRespawn(Player player) {
        if (player == null || !player.isOnline()) return;
        List<String> messages = lang.getMessageList(player, MessageKey.RESPAWN);
        if (messages.isEmpty()) return;
        String template = messages.get(random.nextInt(messages.size()));
        player.sendMessage(render(template, player));
    }

    private void sendDeathTitle(Player player) {
        if (player == null || !player.isOnline()) return;
        String titleStr = lang.getMessage(player, MessageKey.TITLE_DEATH_TITLE);
        String subtitleStr = lang.getMessage(player, MessageKey.TITLE_DEATH_SUBTITLE);

        player.showTitle(Title.title(
                render(titleStr, player),
                render(subtitleStr, player)
        ));
    }

    // ═══════════════ EFFECTS ═══════════════

    private void playDeathEffect(Player player) {
        if (player == null || !player.isOnline()) return;

        if (config.effects.soundEnabled) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1.2f);
        }
        if (config.effects.particlesEnabled) {
            try {
                player.getWorld().spawnParticle(Particle.SOUL,
                        player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.02);
            } catch (Exception ignored) {}
        }
    }

    private void playKillerEffect(Player killer) {
        if (killer == null || !killer.isOnline()) return;
        if (config.effects.soundEnabled) {
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
        }
    }

    private void playRespawnEffect(Player player) {
        if (player == null || !player.isOnline()) return;
        if (config.effects.soundEnabled) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.5f);
        }
        if (config.effects.particlesEnabled) {
            try {
                player.getWorld().spawnParticle(Particle.END_ROD,
                        player.getLocation().add(0, 1, 0), 20, 0.5, 1.0, 0.5, 0.05);
            } catch (Exception ignored) {}
        }
    }

    // ═══════════════ PAPI ═══════════════

    private void recordDeath(Player player, DeathCause cause) {
        if (player == null) return;
        if (integrations == null || !integrations.isPapiEnabled()) return;
        if (integrations.getPlaceholder() == null) return;

        try {
            integrations.getPlaceholder().recordDeath(
                    player.getUniqueId(), cause.name().toLowerCase());
        } catch (Exception ignored) {}
    }

    // ═══════════════ RENDER ═══════════════

    /**
     * Рендер для получателя.
     * PAPI — от context.
     * MiniMessage-плейсхолдеры — от receiver.
     */
    private Component renderFor(Player receiver, String template, Player context) {
        if (template == null || template.isEmpty()) return Component.empty();

        String prepared = template;

        if (integrations != null && integrations.isPapiEnabled()
                && integrations.getPlaceholder() != null && context != null) {
            prepared = integrations.getPlaceholder().parse(context, prepared);
        }

        return lang.render(prepared, receiver);
    }

    /**
     * Рендер для игрока (когда context == receiver).
     */
    private Component render(String text, Player player) {
        if (text == null || text.isEmpty()) return Component.empty();

        String prepared = text;
        if (integrations != null && integrations.isPapiEnabled()
                && integrations.getPlaceholder() != null && player != null) {
            prepared = integrations.getPlaceholder().parse(player, prepared);
        }

        return lang.render(prepared, player);
    }
}