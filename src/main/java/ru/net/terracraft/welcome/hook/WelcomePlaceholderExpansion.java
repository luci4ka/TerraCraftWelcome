package ru.net.terracraft.welcome.hook;

import ru.net.terracraft.welcome.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WelcomePlaceholderExpansion extends PlaceholderExpansion {

    private final Main plugin;
    // Кэш для хранения данных игроков
    private final Map<UUID, Long> lastJoinTimes = new HashMap<>();
    private final Map<UUID, Integer> deathCounts = new HashMap<>();
    private final Map<UUID, String> lastDeathCause = new HashMap<>();

    public WelcomePlaceholderExpansion(Main plugin) {
        this.plugin = plugin;
    }

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
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";

        UUID uuid = offlinePlayer.getUniqueId();

        switch (params.toLowerCase()) {
            case "last_join":
                Long lastJoin = lastJoinTimes.get(uuid);
                if (lastJoin == null) return "Никогда";
                long seconds = (System.currentTimeMillis() - lastJoin) / 1000;
                if (seconds < 60) return "Только что";
                if (seconds < 3600) return (seconds / 60) + " мин. назад";
                if (seconds < 86400) return (seconds / 3600) + " ч. назад";
                return (seconds / 86400) + " дн. назад";

            case "deaths":
                return String.valueOf(deathCounts.getOrDefault(uuid, 0));

            case "last_death_cause":
                return lastDeathCause.getOrDefault(uuid, "Нет данных");

            case "plugin_version":
                return plugin.getDescription().getVersion();

            case "online_count":
                return String.valueOf(plugin.getServer().getOnlinePlayers().size());

            default:
                return null;
        }
    }

    // Методы для обновления данных (вызывать из листенеров)
    public void recordJoin(UUID uuid) {
        lastJoinTimes.put(uuid, System.currentTimeMillis());
    }

    public void recordDeath(UUID uuid, String cause) {
        deathCounts.merge(uuid, 1, Integer::sum);
        lastDeathCause.put(uuid, cause);
    }

    public void saveData() {
        // Здесь можно сохранить в файл/БД
    }
}