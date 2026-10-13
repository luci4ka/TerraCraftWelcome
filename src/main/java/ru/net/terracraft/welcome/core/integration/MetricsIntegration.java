package ru.net.terracraft.welcome.core.integration;

import org.bstats.bukkit.Metrics;
import ru.net.terracraft.welcome.WelcomePlugin;

/**
 * Интеграция bStats.
 */
public class MetricsIntegration {

    private static final int PLUGIN_ID = 27073;

    private final WelcomePlugin plugin;
    private Metrics metrics;

    public MetricsIntegration(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Инициализирует bStats.
     */
    public void init() {
        try {
            this.metrics = new Metrics(plugin, PLUGIN_ID);
            plugin.getLogger().info("bStats инициализирован");
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось инициализировать bStats: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return metrics != null;
    }

    public Metrics getMetrics() {
        return metrics;
    }
}