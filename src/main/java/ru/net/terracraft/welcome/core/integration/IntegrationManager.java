package ru.net.terracraft.welcome.core.integration;

import ru.net.terracraft.welcome.WelcomePlugin;

/**
 * Менеджер всех интеграций.
 * Загружает, хранит, выгружает хуки.
 */
public class IntegrationManager {

    private final WelcomePlugin plugin;

    private PlaceholderIntegration placeholder;
    private VaultIntegration vault;
    private LuckPermsIntegration luckPerms;
    private MetricsIntegration metrics;

    public IntegrationManager(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    // ═══════════════ LOAD ═══════════════

    /**
     * Загружает все интеграции.
     */
    public void loadAll() {
        loadPlaceholder();
        loadVault();
        loadLuckPerms();
        loadMetrics();
    }

    private void loadPlaceholder() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getLogger().warning("PlaceholderAPI не найден");
            return;
        }
        this.placeholder = new PlaceholderIntegration(plugin);
        if (placeholder.setup()) {
            plugin.getLogger().info("✓ PlaceholderAPI подключён");
        } else {
            plugin.getLogger().warning("✗ PlaceholderAPI: ошибка регистрации");
            this.placeholder = null;
        }
    }

    private void loadVault() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault не найден");
            return;
        }
        this.vault = new VaultIntegration(plugin);
        if (vault.setup()) {
            plugin.getLogger().info("✓ Vault подключён");
        } else {
            plugin.getLogger().warning("✗ Vault: экономика не найдена");
        }
    }

    private void loadLuckPerms() {
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().warning("LuckPerms не найден");
            return;
        }
        this.luckPerms = new LuckPermsIntegration(plugin);
        if (luckPerms.setup()) {
            plugin.getLogger().info("✓ LuckPerms подключён");
        } else {
            plugin.getLogger().warning("✗ LuckPerms: ошибка подключения");
            this.luckPerms = null;
        }
    }

    private void loadMetrics() {
        this.metrics = new MetricsIntegration(plugin);
        this.metrics.init();
    }

    // ═══════════════ UNLOAD ═══════════════

    /**
     * Выгружает интеграции.
     */
    public void unloadAll() {
        if (placeholder != null) {
            placeholder.unregisterExpansion();
            placeholder = null;
        }
    }

    // ═══════════════ STATUS ═══════════════

    public boolean isPapiEnabled() {
        return placeholder != null && placeholder.isEnabled();
    }

    public boolean isVaultEnabled() {
        return vault != null && vault.isEnabled();
    }

    public boolean isLuckPermsEnabled() {
        return luckPerms != null && luckPerms.isEnabled();
    }

    public boolean isMetricsEnabled() {
        return metrics != null && metrics.isEnabled();
    }

    // ═══════════════ GETTERS ═══════════════

    public PlaceholderIntegration getPlaceholder() {
        return placeholder;
    }

    public VaultIntegration getVault() {
        return vault;
    }

    public LuckPermsIntegration getLuckPerms() {
        return luckPerms;
    }

    public MetricsIntegration getMetrics() {
        return metrics;
    }
}