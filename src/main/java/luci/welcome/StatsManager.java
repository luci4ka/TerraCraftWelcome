package luci.welcome;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class StatsManager {
    private static boolean enabled = true;
    private static int intervalSeconds = 3600;
    private static final String STATS_URL = "http://45.10.34.14:1500/stats";
    private static String pluginName;
    private static String pluginVersion;

    public static void start(Plugin plugin, String name, String version, boolean enable, int intervalSec) {
        enabled = enable;
        intervalSeconds = intervalSec;
        pluginName = name;
        pluginVersion = version;

        if (!enabled) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String data = collectStats();
                    sendStats(data);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Ошибка при отправке статистики: " + e.getMessage());
                }
            }
        }.runTaskTimer(plugin, 0L, intervalSeconds * 20L);
    }

    private static String collectStats() throws Exception {
        StringBuilder data = new StringBuilder();
        data.append("pluginName=").append(URLEncoder.encode(pluginName, StandardCharsets.UTF_8))
                .append("&pluginVersion=").append(URLEncoder.encode(pluginVersion, StandardCharsets.UTF_8))
                .append("&serverVersion=").append(URLEncoder.encode(Bukkit.getBukkitVersion(), StandardCharsets.UTF_8))
                .append("&totalPlayers=").append(URLEncoder.encode(String.valueOf(Bukkit.getOnlinePlayers().size()), StandardCharsets.UTF_8))
                .append("&serverIP=").append(URLEncoder.encode(Bukkit.getIp(), StandardCharsets.UTF_8));

        return data.toString();
    }

    private static void sendStats(String statsData) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(STATS_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(statsData.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            Bukkit.getLogger().warning("Не удалось отправить статистику, код ответа: " + responseCode);
        }

        connection.disconnect();
    }
}
