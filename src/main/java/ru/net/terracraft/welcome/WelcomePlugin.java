package ru.net.terracraft.welcome;

import org.bukkit.plugin.java.JavaPlugin;
import ru.net.terracraft.welcome.core.PluginContext;
import ru.net.terracraft.welcome.core.PluginContext.ServiceRegistry;
import ru.net.terracraft.welcome.core.PluginLogger;

public final class WelcomePlugin extends JavaPlugin {

    private static WelcomePlugin instance;

    private PluginContext context;

    public static WelcomePlugin getInstance() {
        return instance;
    }

    // ═══════════════ LIFECYCLE ═══════════════

    @Override
    public void onEnable() {
        instance = this;

        try {
            this.context = new PluginContext(this);
            this.context.start();
        } catch (Exception e) {
            getLogger().severe("Ошибка инициализации плагина: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Стартовый лог + баннер
        PluginLogger.logStartup(this);
    }

    @Override
    public void onDisable() {
        if (context != null) {
            try {
                context.stop();
            } catch (Exception e) {
                getLogger().warning("Ошибка при остановке: " + e.getMessage());
            }
            context = null;
        }

        PluginLogger.logShutdown(this);

        instance = null;
    }

    // ═══════════════ ACCESSORS ═══════════════

    /**
     * Доступ к PluginContext.
     */
    public PluginContext context() {
        return context;
    }

    /**
     * Доступ к ServiceRegistry.
     * Требуется командам для получения сервисов.
     */
    public ServiceRegistry getServiceRegistry() {
        if (context == null) {
            throw new IllegalStateException("PluginContext не инициализирован");
        }
        return context.registry();
    }
}