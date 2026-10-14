package ru.terracraft.welcome.api;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.core.PluginContext.ServiceRegistry;

import java.util.Collection;

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
     * Отменяет регистрацию API (только те, что зарегистрировал этот плагин).
     */
    public static void unregister(WelcomePlugin plugin) {
        var services = plugin.getServer().getServicesManager();

        Collection<RegisteredServiceProvider<WelcomeAPI>> regs =
                services.getRegistrations(WelcomeAPI.class);

        if (regs == null || regs.isEmpty()) {
            return;
        }

        int removed = 0;
        for (RegisteredServiceProvider<WelcomeAPI> reg : regs) {
            if (reg.getPlugin() != null && reg.getPlugin().equals(plugin)) {
                services.unregister(WelcomeAPI.class, reg.getProvider());
                removed++;
            }
        }

        if (removed > 0) {
            plugin.getLogger().info("WelcomeAPI отменён в ServicesManager (" + removed + ")");
        }
    }
}