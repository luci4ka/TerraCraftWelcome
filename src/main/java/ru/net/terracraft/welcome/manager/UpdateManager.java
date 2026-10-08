package ru.net.terracraft.welcome.manager;

import ru.net.terracraft.welcome.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class UpdateManager {

    private final Main plugin;
    private final LoggerUtil logger;
    private final String currentVersion;
    private String latestVersion;
    private String downloadUrl;
    private boolean updateAvailable = false;
    private boolean checkComplete = false;

    // Свой API для проверки обновлений
    private static final String UPDATE_API_URL = "http://grandmc.ddns.net/api/plugins/welcome/version.json";

    // Запасной вариант - GitHub
    private static final String GITHUB_API_URL = "https://api.github.com/repos/luci/Welcome/releases/latest";

    public UpdateManager(Main plugin) {
        this.plugin = plugin;
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-Update"));
        this.currentVersion = plugin.getDescription().getVersion();
    }

    /**
     * Проверяет наличие обновлений (сначала свой сайт, потом GitHub)
     */
    public void checkForUpdates() {
        if (checkComplete) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                // Сначала проверяем свой API
                if (checkFromCustomAPI()) {
                    checkComplete = true;
                    return;
                }

                // Если свой API не ответил, пробуем GitHub
                logger.debug("Свой API не ответил, пробуем GitHub...");
                if (checkFromGitHub()) {
                    checkComplete = true;
                    return;
                }

                checkComplete = true;
                logger.debug("Проверка обновлений завершена. Обновлений нет.");
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Проверка обновлений через свой API
     */
    private boolean checkFromCustomAPI() {
        try {
            URI uri = new URI(UPDATE_API_URL);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Welcome-Plugin/" + currentVersion);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                logger.debug("Свой API вернул код: " + responseCode);
                return false;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            String json = response.toString();

            // Парсим JSON ответ
            latestVersion = extractJsonValue(json, "version");
            downloadUrl = extractJsonValue(json, "download_url");

            // Если download_url нет, формируем сами
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                downloadUrl = "http://grandmc.ddns.net/plugins/welcome/Welcome-" + latestVersion + ".jar";
            }

            if (latestVersion != null && !latestVersion.equals(currentVersion)) {
                updateAvailable = true;
                notifyUpdate();
                return true;
            }

            return true;

        } catch (Exception e) {
            logger.debug("Ошибка проверки через свой API: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверка обновлений через GitHub API (запасной вариант)
     */
    private boolean checkFromGitHub() {
        try {
            URI uri = new URI(GITHUB_API_URL);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "Welcome-Plugin/" + currentVersion);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 403) {
                logger.debug("GitHub API ограничил запросы.");
                return false;
            }
            if (responseCode != 200) return false;

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            String json = response.toString();
            latestVersion = extractJsonValue(json, "tag_name");
            downloadUrl = extractJsonValue(json, "html_url");

            if (latestVersion != null && latestVersion.startsWith("v")) {
                latestVersion = latestVersion.substring(1);
            }

            if (latestVersion != null && !latestVersion.equals(currentVersion)) {
                updateAvailable = true;
                notifyUpdate();
                return true;
            }

            return true;

        } catch (Exception e) {
            logger.debug("Ошибка проверки через GitHub: " + e.getMessage());
            return false;
        }
    }

    /**
     * Извлекает значение из JSON строки
     */
    private String extractJsonValue(String json, String key) {
        // Ищем "key":"value"
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            // Ищем "key":"value" (с пробелом)
            searchKey = "\"" + key + "\": \"";
            startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return null;
        }

        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) return null;

        return json.substring(startIndex, endIndex);
    }

    /**
     * Уведомляет консоль и админов о новом обновлении
     */
    private void notifyUpdate() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            logger.warn("════════════════════════════════════════════");
            logger.warn("📦 Доступна новая версия Welcome плагина!");
            logger.warn("Текущая: v" + currentVersion + " → Новая: v" + latestVersion);

            if (downloadUrl != null) {
                Component message = Component.text()
                        .append(Component.text("Скачать: ", NamedTextColor.YELLOW))
                        .append(Component.text(downloadUrl, NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.openUrl(downloadUrl)))
                        .build();
                Bukkit.getConsoleSender().sendMessage(message);
            }

            logger.warn("════════════════════════════════════════════");

            // Оповещение админов в игре
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("welcome.admin"))
                    .forEach(p -> {
                        p.sendMessage(Component.text("📦 Доступно обновление!", NamedTextColor.GOLD));
                        p.sendMessage(Component.text("Текущая: v" + currentVersion + " → Новая: v" + latestVersion,
                                NamedTextColor.YELLOW));
                        if (downloadUrl != null) {
                            p.sendMessage(Component.text("Скачать: " + downloadUrl, NamedTextColor.AQUA)
                                    .clickEvent(ClickEvent.openUrl(downloadUrl)));
                        }
                    });
        });
    }

    /**
     * Принудительная проверка обновлений
     */
    public void forceCheck() {
        checkComplete = false;
        checkForUpdates();
    }

    // Геттеры
    public boolean isUpdateAvailable() { return updateAvailable; }
    public boolean isCheckComplete() { return checkComplete; }
    public String getLatestVersion() { return latestVersion; }
    public String getCurrentVersion() { return currentVersion; }
    public String getDownloadUrl() { return downloadUrl; }
}