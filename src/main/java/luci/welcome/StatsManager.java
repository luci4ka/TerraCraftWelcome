package luci.welcome;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class StatsManager {
    private static final String STATS_URL = "http://45.10.34.14:1500/stats";
    private static UUID serverUuid;

    public static void startStatsCollection(Plugin plugin, int interval) {
        FileConfiguration config = plugin.getConfig();

        // Проверяем, включён ли сбор статистики
        boolean statsEnabled = config.getBoolean("stats.enable", false);
        int intervalSeconds = config.getInt("stats.interval", 3600);

        if (!statsEnabled) {
            Bukkit.getConsoleSender().sendMessage("§7[Welcome] Сбор статистики отключён через конфиг.");
            return;
        }

        if (serverUuid == null) {
            serverUuid = loadOrGenerateUuid(plugin);
        }

        Bukkit.getConsoleSender().sendMessage("§3[Welcome] Сбор статистики включён. Интервал: " + intervalSeconds + " сек.");

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            JSONObject statsData = collectStats();
            try {
                sendStatsToServer(statsData.toJSONString());
            } catch (Exception ignored) {
                // Не мешаем работе сервера
            }
        }, 0L, intervalSeconds * 20L); // Интервал в тиках
    }

    private static UUID loadOrGenerateUuid(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        String uuidStr = config.getString("stats.uuid");

        if (uuidStr == null || uuidStr.isEmpty()) {
            UUID newUuid = UUID.randomUUID();
            config.set("stats.uuid", newUuid.toString());
            plugin.saveConfig();
            return newUuid;
        }

        return UUID.fromString(uuidStr);
    }

    @SuppressWarnings("unchecked")
    static JSONObject collectStats() {
        JSONObject json = new JSONObject();
        json.put("pluginName", "Welcome");
        json.put("pluginVersion", getPluginVersion());
        json.put("serverVersion", Bukkit.getBukkitVersion());
        json.put("playersOnline", Bukkit.getOnlinePlayers().size());
        json.put("serverIP", Bukkit.getIp());
        json.put("serverUUID", serverUuid.toString());

        return json;
    }

    static void sendStatsToServer(String jsonData) throws Exception {
        // Создание URI и отправка запроса с использованием HttpClient
        try {
            URI uri = new URI(STATS_URL);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
                    .build();

            // Отправка запроса и получение ответа
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                // Не логируем ошибку, тихая отправка
                // В случае ошибки можно добавить дополнительное логирование
            }
        } catch (Exception e) {
            throw new Exception("Ошибка при отправке статистики на сервер", e);
        }
    }

    private static String getPluginVersion() {
        return Bukkit.getPluginManager().getPlugin("Welcome").getDescription().getVersion();
    }
}
