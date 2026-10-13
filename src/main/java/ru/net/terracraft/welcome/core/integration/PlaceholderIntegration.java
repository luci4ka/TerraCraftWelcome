package ru.net.terracraft.welcome.core.integration;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.net.terracraft.welcome.WelcomePlugin;
import ru.net.terracraft.welcome.core.feature.FirstJoinCounter;
import ru.net.terracraft.welcome.core.i18n.LanguageManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Единая PlaceholderAPI-интеграция.
 * Сам является expansion'ом (extends PlaceholderExpansion).
 * Идентификатор: %welcome_<placeholder>%
 */
public class PlaceholderIntegration extends PlaceholderExpansion {

    private final WelcomePlugin plugin;

    // Данные для плейсхолдеров
    private final Map<UUID, Long> lastJoinTimes = new HashMap<>();
    private final Map<UUID, Integer> deathCounts = new HashMap<>();
    private final Map<UUID, String> lastDeathCause = new HashMap<>();

    private boolean registered = false;

    public PlaceholderIntegration(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    // ═══════════════ EXPANSION META ═══════════════

    @Override
    public @NotNull String getIdentifier() {
        return "welcome";
    }

    @Override
    public @NotNull String getAuthor() {
        return "luci";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    // ═══════════════ REGISTER / UNREGISTER ═══════════════

    public boolean setup() {
        try {
            if (register()) {
                this.registered = true;
                return true;
            }
            plugin.getLogger().warning("Не удалось зарегистрировать PAPI-expansion");
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка регистрации PAPI: " + e.getMessage());
            return false;
        }
    }

    public void unregisterExpansion() {
        if (!registered) return;
        try {
            unregister();
        } catch (Exception ignored) {
        }
        registered = false;
    }

    public boolean isEnabled() {
        return registered;
    }

    // ═══════════════ PLACEHOLDER LOGIC ═══════════════

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return "";
        }

        UUID uuid = offlinePlayer.getUniqueId();

        switch (params.toLowerCase()) {
            // ─── Последний вход ───
            case "last_join": {
                Long lastJoin = lastJoinTimes.get(uuid);
                if (lastJoin == null) return "Никогда";
                long seconds = (System.currentTimeMillis() - lastJoin) / 1000;
                if (seconds < 60) return "Только что";
                if (seconds < 3600) return (seconds / 60) + " мин. назад";
                if (seconds < 86400) return (seconds / 3600) + " ч. назад";
                return (seconds / 86400) + " дн. назад";
            }

            // ─── Смерти ───
            case "deaths":
                return String.valueOf(deathCounts.getOrDefault(uuid, 0));

            case "last_death_cause":
                return lastDeathCause.getOrDefault(uuid, "Нет данных");

            // ─── Инфо о плагине ───
            case "plugin_version":
                return plugin.getPluginMeta().getVersion();

            case "online_count":
                return String.valueOf(plugin.getServer().getOnlinePlayers().size());

            case "player_name":
                return offlinePlayer.getName() != null ? offlinePlayer.getName() : "";

            case "max_players":
                return String.valueOf(plugin.getServer().getMaxPlayers());

            case "vk_url": {
                String text = getLinkText("links.vk");
                return text;
            }
            case "site_url": {
                return getLinkText("links.site");
            }
            case "discord_url": {
                return getLinkText("links.discord");
            }
            case "telegram_url": {
                return getLinkText("links.telegram");
            }
            case "youtube_url": {
                return getLinkText("links.youtube");
            }
            // ─── Номер первого входа ───
            case "first_join_number": {
                int number = getFirstJoinNumber(uuid);
                return number > 0 ? String.valueOf(number) : "—";
            }

            case "first_join_number_with_hash": {
                int number = getFirstJoinNumber(uuid);
                return number > 0 ? "#" + number : "—";
            }

            case "total_players": {
                return String.valueOf(getTotalPlayers());
            }

            default:
                return null;
        }
    }

    // ═══════════════ FIRST JOIN HELPERS ═══════════════

    /**
     * Безопасно получить номер первого входа игрока.
     * @return номер (1+), или -1 если игрок не найден / ошибка
     */
    private int getFirstJoinNumber(UUID uuid) {
        if (uuid == null) return -1;

        try {
            FirstJoinCounter counter = plugin.getServiceRegistry()
                    .get(FirstJoinCounter.class);

            if (counter == null) return -1;

            return counter.getNumber(uuid);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Получить ссылку из lang/*.yml.
     * Возвращает MiniMessage-строку (например, <click:...>ВК</click>).
     */
    private String getLinkText(String path) {
        try {
            LanguageManager lang = plugin.getServiceRegistry()
                    .get(LanguageManager.class);
            if (lang == null) return "";

            // Строка из YAML
            String value = lang.getString(path);
            return value != null ? value : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Безопасно получить общее количество игроков.
     */
    private int getTotalPlayers() {
        try {
            FirstJoinCounter counter = plugin.getServiceRegistry()
                    .get(FirstJoinCounter.class);

            if (counter == null) return 0;

            return counter.getTotal();
        } catch (Exception e) {
            return 0;
        }
    }

    // ═══════════════ PAPI PARSE ═══════════════

    /**
     * Парсит плейсхолдеры в тексте через PAPI.
     */
    public String parse(Player player, String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (player == null) {
            return text;
        }
        try {
            return PlaceholderAPI.setPlaceholders(player, text);
        } catch (Exception e) {
            return text;
        }
    }

    // ═══════════════ DATA RECORDING ═══════════════

    public void recordJoin(UUID uuid) {
        if (uuid == null) return;
        lastJoinTimes.put(uuid, System.currentTimeMillis());
    }

    public void recordDeath(UUID uuid, String cause) {
        if (uuid == null) return;
        deathCounts.merge(uuid, 1, Integer::sum);
        if (cause != null) {
            lastDeathCause.put(uuid, cause);
        }
    }
}