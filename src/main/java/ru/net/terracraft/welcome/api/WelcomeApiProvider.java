package ru.net.terracraft.welcome.api;

import org.bukkit.plugin.ServicePriority;
import ru.net.terracraft.welcome.WelcomePlugin;
import ru.net.terracraft.welcome.core.PluginContext.ServiceRegistry;

/**
 * Регистрирует WelcomeAPI в Bukkit ServicesManager.
 * Другие плагины могут получить через:
 *   WelcomeAPI api = Bukkit.getServicesManager().load(WelcomeAPI.class);
 */
public final class WelcomeApiProvider {

    private WelcomeApiProvider() {
    }

    /**
     * Регистрирует API.
     */
    public static WelcomeAPI register(WelcomePlugin plugin, ServiceRegistry registry) {
        WelcomeAPI api = new WelcomeAPI(plugin, registry);

        plugin.getServer().getServicesManager().register(
                WelcomeAPI.class,
                api,
                plugin,
                ServicePriority.Normal
        );

        plugin.getLogger().info("WelcomeAPI зарегистрирован в ServicesManager");
        return api;
    }

    /**
     * Отменяет регистрацию API.
     */
    public static void unregister(WelcomePlugin plugin) {
        plugin.getServer().getServicesManager().unregisterAll(plugin);
    }
}