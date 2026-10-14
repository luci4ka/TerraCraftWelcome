package ru.terracraft.welcome.core.integration;

import org.bstats.bukkit.Metrics;
import ru.terracraft.welcome.WelcomePlugin;

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

    public void shutdown() {
        if (metrics != null) {
            try {
                metrics.shutdown();
            } catch (Exception ignored) {}
            metrics = null;
        }
    }

    public Metrics getMetrics() {
        return metrics;
    }
}