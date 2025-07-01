package luci.welcome;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class StatsManager {
    private static final String STATS_IP = "45.10.35.14";
    private static final int STATS_PORT = 1500;
    private static final String STATS_PATH = "/stats";
    private static final String AUTH_TOKEN = "b90f4fc383e84cde9129a67c4a3823db";

    private static UUID serverUuid;
    private static boolean statsCollectionStarted = false;

    private static JavaPlugin plugin() {
        return JavaPlugin.getPlugin(Main.class); // Убедись, что Main — это твой JavaPlugin-класс
    }

    public static void startStatsCollection() {
        if (statsCollectionStarted) return;

        FileConfiguration config = plugin().getConfig();
        boolean statsEnabled = config.getBoolean("stats.enable", false);
        int intervalSeconds = config.getInt("stats.interval", 3600);

        if (!statsEnabled || intervalSeconds <= 0) {
            Bukkit.getConsoleSender().sendMessage("§7[Welcome] Сбор статистики отключён через конфиг.");
            return;
        }

        if (serverUuid == null) {
            serverUuid = loadOrGenerateUuid();
        }

        Bukkit.getConsoleSender().sendMessage("§3[Welcome] Сбор статистики включён. Интервал: " + intervalSeconds + " сек.");
        statsCollectionStarted = true;
        long intervalTicks = intervalSeconds * 20L;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin(), () -> {
            JSONObject statsData = collectStats();
            sendStatsToServerAsync(statsData.toJSONString());
        }, 0L, intervalTicks);
    }

    private static UUID loadOrGenerateUuid() {
        FileConfiguration config = plugin().getConfig();
        String uuidStr = config.getString("stats.uuid");

        if (uuidStr == null || uuidStr.isEmpty()) {
            UUID newUuid = UUID.randomUUID();
            config.set("stats.uuid", newUuid.toString());
            plugin().saveConfig();
            return newUuid;
        }

        return UUID.fromString(uuidStr);
    }

    @SuppressWarnings("unchecked")
    public static JSONObject collectStats() {
        JSONObject json = new JSONObject();
        json.put("pluginName", "Welcome");
        json.put("pluginVersion", plugin().getDescription().getVersion());
        json.put("serverVersion", Bukkit.getBukkitVersion());
        json.put("playersOnline", Bukkit.getOnlinePlayers().size());
        json.put("serverUUID", serverUuid.toString());
        return json;
    }

    public static void sendStatsToServerAsync(String jsonData) {
        try {
            String fullUrl = String.format("http://%s:%d%s", STATS_IP, STATS_PORT, STATS_PATH);
            URI uri = new URI(fullUrl);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", AUTH_TOKEN)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            Bukkit.getLogger().warning("[Welcome] Ошибка отправки статистики: код " + response.statusCode());
                        }
                    })
                    .exceptionally(e -> {
                        Bukkit.getLogger().warning("[Welcome] Исключение при отправке статистики: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            Bukkit.getLogger().warning("[Welcome] Ошибка подготовки запроса статистики: " + e.getMessage());
        }
    }
}
