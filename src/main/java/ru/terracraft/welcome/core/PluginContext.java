package ru.terracraft.welcome.core;

import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.api.WelcomeAPI;
import ru.terracraft.welcome.api.WelcomeApiProvider;
import ru.terracraft.welcome.command.CommandRegistrar;
import ru.terracraft.welcome.config.ConfigLoader;
import ru.terracraft.welcome.config.MainConfig;
import ru.terracraft.welcome.core.feature.*;
import ru.terracraft.welcome.core.group.GroupManager;
import ru.terracraft.welcome.core.i18n.LanguageManager;
import ru.terracraft.welcome.core.integration.IntegrationManager;
import ru.terracraft.welcome.core.listener.PlayerListener;
import ru.terracraft.welcome.core.scheduler.TaskScheduler;
import ru.terracraft.welcome.core.update.UpdateNotifierService;

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
    private volatile boolean stopped = false;

    public void start() {
        if (stopped) {
            throw new IllegalStateException("PluginContext уже остановлен, перезапуск невозможен");
        }

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
        registerListeners();

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

    public void reload() {
        if (stopped) {
            plugin.getLogger().warning("Нельзя перезагрузить остановленный PluginContext");
            return;
        }

        plugin.getLogger().info("Перезагрузка PluginContext...");

        // 1. Останавливаем то, что можно
        if (registry.has(TaskScheduler.class)) {
            registry.get(TaskScheduler.class).stopAll();
        }
        if (registry.has(IntegrationManager.class)) {
            registry.get(IntegrationManager.class).unloadAll();
        }

        // 2. Config — перечитываем
        ConfigLoader configLoader = registry.get(ConfigLoader.class);
        configLoader.load();
        MainConfig config = configLoader.get();

        // 3. Языки — перечитываем
        LanguageManager languageManager = registry.get(LanguageManager.class);
        languageManager.loadLanguages();
        if (config.language != null && languageManager.hasLanguage(config.language)) {
            languageManager.setLanguage(config.language);
        }

        // 4. Интеграции — пересоздаём
        IntegrationManager integrationManager = new IntegrationManager(plugin);
        integrationManager.loadAll();
        registry.register(IntegrationManager.class, integrationManager);

        // 5. Пересоздаём feature-сервисы (config/lang/integrations изменились)
        JoinService joinService = new JoinService(
                plugin, config, languageManager, integrationManager,
                registry.get(FirstJoinCounter.class),
                registry.get(GroupManager.class)
        );
        registry.register(JoinService.class, joinService);

        DeathService deathService = new DeathService(plugin, config, languageManager, integrationManager);
        registry.register(DeathService.class, deathService);

        HolidayService holidayService = new HolidayService(plugin, config, languageManager, integrationManager);
        registry.register(HolidayService.class, holidayService);

        GuiService guiService = new GuiService(plugin, config, configLoader, languageManager);
        registry.register(GuiService.class, guiService);

        // 6. Scheduler — пересоздаём
        TaskScheduler scheduler = new TaskScheduler(plugin, holidayService);
        scheduler.startAll();
        registry.register(TaskScheduler.class, scheduler);

        // 7. API — перерегистрируем
        WelcomeApiProvider.unregister(plugin);
        WelcomeAPI api = WelcomeApiProvider.register(plugin, registry);
        registry.register(WelcomeAPI.class, api);

        plugin.getLogger().info("PluginContext перезагружен");
    }

    public void stop() {
        if (stopped) return;
        stopped = true;

        plugin.getLogger().info("Остановка PluginContext...");

        try {
            if (registry.has(TaskScheduler.class)) {
                registry.get(TaskScheduler.class).stopAll();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка остановки TaskScheduler: " + e.getMessage());
        }

        try {
            if (registry.has(FirstJoinCounter.class)) {
                registry.get(FirstJoinCounter.class).shutdown();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка остановки FirstJoinCounter: " + e.getMessage());
        }

        try {
            if (registry.has(IntegrationManager.class)) {
                registry.get(IntegrationManager.class).unloadAll();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка выгрузки интеграций: " + e.getMessage());
        }

        try {
            WelcomeApiProvider.unregister(plugin);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка отмены регистрации API: " + e.getMessage());
        }

        registry.clear();
    }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(
                new PlayerListener(plugin),
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