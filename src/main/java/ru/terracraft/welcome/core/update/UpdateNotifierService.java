package ru.terracraft.welcome.core.update;

import com.github.ezplugins.updater.UpdateChecker;
import com.github.ezplugins.updater.UpdateResult;
import ru.terracraft.welcome.WelcomePlugin;

/**
 * Обёртка над ez-plugins/github-update-checker.
 * Проверяет обновления через GitHub Releases API.
 */
public class UpdateNotifierService {

    private final WelcomePlugin plugin;
    private final String owner;
    private final String repo;

    private UpdateChecker checker;

    public UpdateNotifierService(WelcomePlugin plugin, String owner, String repo) {
        this.plugin = plugin;
        this.owner = owner;
        this.repo = repo;
    }

    /**
     * Инициализирует UpdateChecker.
     * Можно передать GitHub-токен через env GITHUB_TOKEN для приватных репозиториев.
     */
    public void init() {
        try {
            String currentVersion = plugin.getPluginMeta().getVersion();

            this.checker = UpdateChecker.builder(owner, repo, currentVersion)
                    .build();

            plugin.getLogger().info("UpdateChecker инициализирован ("
                    + owner + "/" + repo + ")");
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось инициализировать UpdateChecker: "
                    + e.getMessage());
            this.checker = null;
        }
    }

    /**
     * Асинхронная проверка обновлений.
     */
    public void checkAsync() {
        if (checker == null) {
            plugin.getLogger().warning("UpdateChecker не инициализирован");
            return;
        }

        try {
            checker.checkNowAsync().thenAccept(result -> {
                if (result == null) return;

                if (result.hasError()) {
                    Throwable error = result.getError().orElse(null);
                    plugin.getLogger().warning("Ошибка проверки обновлений: "
                            + (error != null ? error.getMessage() : "unknown"));
                    return;
                }

                if (result.isUpdateAvailable()) {
                    String latest = result.getLatestVersion().orElse("unknown");
                    String url = result.getReleaseUrl().orElse("n/a");
                    String current = plugin.getPluginMeta().getVersion();

                    plugin.getLogger().warning("════════════════════════════════════════");
                    plugin.getLogger().warning("📦 Доступна новая версия Welcome!");
                    plugin.getLogger().warning("Текущая: " + current + " → Новая: " + latest);
                    plugin.getLogger().warning("Скачать: " + url);
                    plugin.getLogger().warning("════════════════════════════════════════");
                } else {
                    plugin.getLogger().info("Обновлений нет (текущая: "
                            + plugin.getPluginMeta().getVersion() + ")");
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка async-проверки обновлений: "
                    + e.getMessage());
        }
    }

    /**
     * Синхронная проверка (для команд).
     * @return UpdateResult или null при ошибке.
     */
    public UpdateResult checkNow() {
        if (checker == null) return null;
        try {
            return checker.checkNow();
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка проверки обновлений: " + e.getMessage());
            return null;
        }
    }

    public boolean isReady() {
        return checker != null;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }
}