package ru.net.terracraft.welcome.core;

import org.bukkit.plugin.PluginManager;
import ru.net.terracraft.welcome.WelcomePlugin;
import ru.net.terracraft.welcome.api.WelcomeAPI;
import ru.net.terracraft.welcome.api.WelcomeApiProvider;
import ru.net.terracraft.welcome.command.CommandRegistrar;
import ru.net.terracraft.welcome.config.ConfigLoader;
import ru.net.terracraft.welcome.config.MainConfig;
import ru.net.terracraft.welcome.core.feature.*;
import ru.net.terracraft.welcome.core.group.GroupManager;
import ru.net.terracraft.welcome.core.i18n.LanguageManager;
import ru.net.terracraft.welcome.core.integration.IntegrationManager;
import ru.net.terracraft.welcome.core.listener.PlayerListener;
import ru.net.terracraft.welcome.core.scheduler.TaskScheduler;
import ru.net.terracraft.welcome.core.update.UpdateNotifierService;

import java.util.HashMap;
import java.util.Map;

/**
 * Контекст плагина: DI-контейнер + центральная точка инициализации.
 * Содержит ServiceRegistry как вложенный класс.
 */
public class PluginContext {

    private static final String GITHUB_OWNER = "luci";
    private static final String GITHUB_REPO = "Welcome";

    private final WelcomePlugin plugin;
    private final ServiceRegistry registry = new ServiceRegistry();

    public PluginContext(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    // ═══════════════ START ═══════════════

    public void start() {
        plugin.getLogger().info("Инициализация PluginContext...");

        // 1. Config
        ConfigLoader configLoader = new ConfigLoader(plugin);
        configLoader.load();
        registry.register(ConfigLoader.class, configLoader);

        MainConfig config = configLoader.get();

        // 2. i18n
        LanguageManager languageManager = new LanguageManager(plugin, config.language);
        registry.register(LanguageManager.class, languageManager);

        // 3. Integration
        IntegrationManager integrationManager = new IntegrationManager(plugin);
        integrationManager.loadAll();
        registry.register(IntegrationManager.class, integrationManager);

        // 4. Группы + счётчик
        GroupManager groupManager = new GroupManager(plugin);
        registry.register(GroupManager.class, groupManager);

        FirstJoinCounter firstJoinCounter = new FirstJoinCounter(plugin);
        registry.register(FirstJoinCounter.class, firstJoinCounter);

        // 5. Feature services
        JoinService joinService = new JoinService(
                plugin, config, languageManager, integrationManager,
                firstJoinCounter, groupManager
        );
        registry.register(JoinService.class, joinService);

        DeathService deathService = new DeathService(plugin, config, languageManager, integrationManager);
        registry.register(DeathService.class, deathService);

        HolidayService holidayService = new HolidayService(plugin, config, languageManager, integrationManager);
        registry.register(HolidayService.class, holidayService);

        GuiService guiService = new GuiService(plugin, config, configLoader, languageManager);
        registry.register(GuiService.class, guiService);

        // 6. Listeners
        registerListeners(joinService, deathService, guiService);

        // 7. Commands
        new CommandRegistrar(plugin).registerAll();

        // 8. Scheduler
        TaskScheduler scheduler = new TaskScheduler(plugin, holidayService);
        scheduler.startAll();
        registry.register(TaskScheduler.class, scheduler);

        // 9. API
        WelcomeAPI api = WelcomeApiProvider.register(plugin, registry);
        registry.register(WelcomeAPI.class, api);

        // 10. Update checker
        UpdateNotifierService updateNotifier = new UpdateNotifierService(plugin, GITHUB_OWNER, GITHUB_REPO);
        updateNotifier.init();
        updateNotifier.checkAsync();
        registry.register(UpdateNotifierService.class, updateNotifier);

        plugin.getLogger().info("PluginContext инициализирован");
    }

    public void stop() {
        plugin.getLogger().info("Остановка PluginContext...");

        if (registry.has(TaskScheduler.class)) {
            registry.get(TaskScheduler.class).stopAll();
        }

        if (registry.has(IntegrationManager.class)) {
            registry.get(IntegrationManager.class).unloadAll();
        }

        WelcomeApiProvider.unregister(plugin);
        registry.clear();
    }

    private void registerListeners(JoinService joinService, DeathService deathService, GuiService guiService) {
        plugin.getServer().getPluginManager().registerEvents(
                new PlayerListener(joinService, deathService, guiService),
                plugin
        );

        plugin.getLogger().info("Зарегистрирован слушатель PlayerListener");
    }

    public WelcomePlugin plugin() {
        return plugin;
    }

    public ServiceRegistry registry() {
        return registry;
    }

    // ═══════════════════════════════════════════
    //  SERVICE REGISTRY (вложенный класс)
    // ═══════════════════════════════════════════

    public static class ServiceRegistry {

        private final Map<Class<?>, Object> services = new HashMap<>();

        public <T> void register(Class<T> type, T instance) {
            if (instance == null) {
                throw new IllegalArgumentException("Нельзя зарегистрировать null для " + type.getName());
            }
            services.put(type, instance);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(Class<T> type) {
            Object service = services.get(type);
            if (service == null) {
                throw new IllegalStateException("Сервис не зарегистрирован: " + type.getName());
            }
            return (T) service;
        }

        public boolean has(Class<?> type) {
            return services.containsKey(type);
        }

        public void unregister(Class<?> type) {
            services.remove(type);
        }

        public void clear() {
            services.clear();
        }
    }
}