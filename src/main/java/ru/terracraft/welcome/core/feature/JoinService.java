package ru.terracraft.welcome.core.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.api.event.WelcomeJoinEvent;
import ru.terracraft.welcome.config.MainConfig;
import ru.terracraft.welcome.core.group.GroupManager;
import ru.terracraft.welcome.core.i18n.LanguageManager;
import ru.terracraft.welcome.core.i18n.MessageKey;
import ru.terracraft.welcome.core.integration.IntegrationManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Сервис входа/выхода игрока.
 * Все сообщения — на локали клиента.
 * Группы поддерживают наследование (inherits).
 */
public class JoinService {

    private final WelcomePlugin plugin;
    private final MainConfig config;
    private final LanguageManager lang;
    private final IntegrationManager integrations;
    private final FirstJoinCounter firstJoinCounter;
    private final GroupManager groupManager;
    private final Random random = new Random();

    public JoinService(WelcomePlugin plugin, MainConfig config,
                       LanguageManager lang, IntegrationManager integrations,
                       FirstJoinCounter firstJoinCounter,
                       GroupManager groupManager) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.integrations = integrations;
        this.firstJoinCounter = firstJoinCounter;
        this.groupManager = groupManager;
    }

    // ═══════════════ PUBLIC API ═══════════════

    public void handleJoin(Player player) {
        if (player == null) return;

        int playerNumber = (firstJoinCounter != null)
                ? firstJoinCounter.getNumber(player.getUniqueId())
                : -1;

        boolean firstJoin = (playerNumber < 0) && !player.hasPlayedBefore();
        if (playerNumber > 0) firstJoin = false;

        // ═══════════════ EVENT ═══════════════
        WelcomeJoinEvent event = new WelcomeJoinEvent(player, firstJoin);
        try {
            Bukkit.getPluginManager().callEvent(event);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка вызова WelcomeJoinEvent: " + e.getMessage());
        }
        if (event.isCancelled()) return;
        // ═════════════════════════════════════

        int assignedNumber = -1;
        if (firstJoin && firstJoinCounter != null) {
            assignedNumber = firstJoinCounter.getOrAssign(player.getUniqueId());
        }

        if (config.join.motd) sendMotd(player);
        if (config.join.titles) sendTitle(player, firstJoin, assignedNumber);

        if (config.join.join) {
            if (firstJoin) {
                broadcastFirstJoin(player, assignedNumber);
                if (config.join.firstJoinFirework) spawnFirework(player);
                plugin.getLogger().info("Игрок " + player.getName()
                        + " впервые зашёл на сервер! (#" + assignedNumber + ")");
            } else {
                broadcastGroup(player, "join");
            }
        }

        if (config.join.actionBar) sendActionBar(player, firstJoin);
        playJoinEffects(player, firstJoin);
        recordJoin(player);
    }

    public void handleQuit(Player player) {
        if (player == null) return;

        if (config.join.quit) broadcastGroup(player, "quit");
        playQuitEffects(player);

        // Очистка PAPI-кэша
        if (integrations != null && integrations.isPapiEnabled()
                && integrations.getPlaceholder() != null) {
            try {
                integrations.getPlaceholder().clearPlayer(player.getUniqueId());
            } catch (Exception ignored) {}
        }
    }

    // ═══════════════ MOTD ═══════════════

    private void sendMotd(Player player) {
        if (player == null || !player.isOnline()) return;

        List<String> lines = lang.getMessageList(player, MessageKey.MOTD_LINES);
        if (lines.isEmpty()) return;

        for (String line : lines) {
            if (line == null || line.isEmpty()) continue;
            player.sendMessage(render(line, player));
        }
    }

    // ═══════════════ TITLES ═══════════════

    private void sendTitle(Player player, boolean firstJoin, int number) {
        if (player == null || !player.isOnline()) return;

        MessageKey titleKey = firstJoin ? MessageKey.TITLE_FIRST_JOIN_TITLE : MessageKey.TITLE_JOIN_TITLE;
        MessageKey subKey = firstJoin ? MessageKey.TITLE_FIRST_JOIN_SUBTITLE : MessageKey.TITLE_JOIN_SUBTITLE;

        String titleRaw = lang.getMessage(player, titleKey);
        String subRaw = lang.getMessage(player, subKey);

        titleRaw = applyNumber(titleRaw, number);
        subRaw = applyNumber(subRaw, number);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(3500),
                Duration.ofMillis(1000)
        );

        player.showTitle(Title.title(
                render(titleRaw, player),
                render(subRaw, player),
                times
        ));
    }

    private void sendActionBar(Player player, boolean firstJoin) {
        if (player == null || !player.isOnline()) return;

        MessageKey key = firstJoin ? MessageKey.ACTIONBAR_FIRST_JOIN : MessageKey.ACTIONBAR_JOIN;
        String text = lang.getMessage(player, key, "");

        if (text.isEmpty()) return;

        player.sendActionBar(render(text, player));
    }

    // ═══════════════ MESSAGES ═══════════════

    /**
     * Первый вход — broadcast каждому на его локали.
     */
    private void broadcastFirstJoin(Player context, int number) {
        if (context == null) return;

        for (Player online : Bukkit.getOnlinePlayers()) {
            try {
                List<String> messages = lang.getMessageList(online, MessageKey.JOIN_FIRST);
                if (messages.isEmpty()) {
                    messages = getGroupMessages(online, context, "join");
                }
                if (messages.isEmpty()) continue;

                String template = messages.get(random.nextInt(messages.size()));
                template = applyNumber(template, number);

                online.sendMessage(renderFor(online, template, context));
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Обычный вход/выход — из группы контекста.
     * Каждый получатель видит на своём языке.
     */
    private void broadcastGroup(Player context, String type) {
        if (context == null) return;

        for (Player online : Bukkit.getOnlinePlayers()) {
            try {
                List<String> messages = getGroupMessages(online, context, type);
                if (messages.isEmpty()) continue;

                String template = messages.get(random.nextInt(messages.size()));
                online.sendMessage(renderFor(online, template, context));
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Получить сообщения группы для получателя (по его локали).
     * Группа берётся от context (игрока, о котором сообщение).
     *
     * ⚠️ Использует ЦЕПОЧКУ групп с наследованием:
     *   legend → admin → moderator → vip → default
     * Все сообщения из цепочки собираются и рандомятся.
     */
    private List<String> getGroupMessages(Player receiver, Player context, String type) {
        // Получаем активную группу контекста
        GroupManager.Group group = groupManager.getSelectedGroup(context);

        // Цепочка групп с наследованием
        List<String> chain = groupManager.getGroupChain(group);

        // Собираем ВСЕ сообщения из цепочки
        List<String> allMessages = new ArrayList<>();

        for (String groupId : chain) {
            String path = "groups." + groupId + "." + type;
            List<String> messages = lang.getMessageList(receiver, path);
            if (!messages.isEmpty()) {
                allMessages.addAll(messages);
            }
        }

        // Fallback — lang join.default / quit.default
        if (allMessages.isEmpty()) {
            MessageKey fallback = type.equals("join")
                    ? MessageKey.JOIN_DEFAULT
                    : MessageKey.QUIT_DEFAULT;
            allMessages = new ArrayList<>(lang.getMessageList(receiver, fallback));
        }

        return allMessages;
    }

    // ═══════════════ EFFECTS ═══════════════

    private void playJoinEffects(Player player, boolean firstJoin) {
        if (player == null || !player.isOnline()) return;

        if (config.effects.soundEnabled) {
            Sound sound = firstJoin
                    ? Sound.ENTITY_PLAYER_LEVELUP
                    : Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
            player.playSound(player.getLocation(), sound,
                    (float) config.effects.soundVolume, (float) config.effects.soundPitch);
        }

        if (config.effects.particlesEnabled) {
            try {
                player.getWorld().spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        player.getLocation().add(0, 1, 0),
                        config.effects.particleAmount
                );
            } catch (Exception ignored) {}
        }
    }

    private void playQuitEffects(Player player) {
        if (player == null) return;

        if (config.effects.soundEnabled) {
            player.playSound(player.getLocation(),
                    Sound.ENTITY_ITEM_BREAK,
                    (float) config.effects.soundVolume, (float) config.effects.soundPitch);
        }
    }

    private void spawnFirework(Player player) {
        if (player == null || !player.isOnline()) return;

        try {
            FireworkEffect effect = FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.AQUA)
                    .withColor(Color.LIME)
                    .withFade(Color.YELLOW)
                    .withFlicker()
                    .withTrail()
                    .build();

            Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(effect);
            meta.setPower(1);
            firework.setFireworkMeta(meta);
            firework.detonate();
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка фейерверка: " + e.getMessage());
        }
    }

    // ═══════════════ PAPI ═══════════════

    private void recordJoin(Player player) {
        if (player == null) return;
        if (integrations == null || !integrations.isPapiEnabled()) return;
        if (integrations.getPlaceholder() == null) return;

        try {
            integrations.getPlaceholder().recordJoin(player.getUniqueId());
        } catch (Exception ignored) {}
    }

    // ═══════════════ HELPERS ═══════════════

    private String applyNumber(String text, int number) {
        if (text == null || text.isEmpty()) return text;
        if (number <= 0) {
            return text
                    .replace("%number%", "")
                    .replace("%total%", "");
        }

        int total = (firstJoinCounter != null) ? firstJoinCounter.getTotal() : number;

        return text
                .replace("%number%", String.valueOf(number))
                .replace("%total%", String.valueOf(total));
    }

    /**
     * Рендер для конкретного получателя (receiver).
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