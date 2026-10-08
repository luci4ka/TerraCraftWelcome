package ru.net.terracraft.welcome;

import org.bstats.bukkit.Metrics;
import ru.net.terracraft.welcome.api.WelcomeAPI;
import ru.net.terracraft.welcome.command.CommandManager;
import ru.net.terracraft.welcome.config.ConfigHandler;
import ru.net.terracraft.welcome.config.AutoMessageConfig;
import ru.net.terracraft.welcome.config.HolidayConfig;
import ru.net.terracraft.welcome.hook.HookManager;
import ru.net.terracraft.welcome.listener.GuiListener;
import ru.net.terracraft.welcome.listener.PlayerDeathListener;
import ru.net.terracraft.welcome.listener.PlayerJoinListener;
import ru.net.terracraft.welcome.manager.LanguageManager;
import ru.net.terracraft.welcome.manager.UpdateManager;
import ru.net.terracraft.welcome.task.AutoMessageTask;
import ru.net.terracraft.welcome.task.HolidayEffectTask;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main extends JavaPlugin {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final double CONFIG_VERSION = 2.2;
    private static final int BSTATS_PLUGIN_ID = 27073;

    private static Main instance;

    private ConfigHandler config;
    private HookManager hookManager;
    private LanguageManager languageManager;
    private UpdateManager updateManager;
    private AdapterManager adapterManager;
    private AutoMessageTask autoMessageTask;
    private HolidayEffectTask holidayEffectTask;
    private WelcomeAPI api;
    private CommandManager commandManager;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Загрузка конфигурации
        config = new ConfigHandler(this);
        config.checkConfigVersion(CONFIG_VERSION);

        // Инициализация менеджеров (порядок важен!)
        this.languageManager = new LanguageManager(this);
        this.updateManager = new UpdateManager(this);
        this.adapterManager = new AdapterManager(this);

        // Инициализация хуков
        this.hookManager = new HookManager(this);
        this.hookManager.loadHooks();

        // Регистрация команд через Paper Lifecycle API
        registerCommands();

        // Регистрация событий
        registerEvents();

        // Инициализация API
        this.api = new WelcomeAPI(this);

        // Инициализация метрик
        initMetrics();

        // Запуск периодических задач
        initTasks();

        // Проверка обновлений (асинхронно)
        updateManager.checkForUpdates();

        // Информация о включении
        printStartupInfo();
    }

    @Override
    public void onDisable() {
        // Остановка задач
        if (autoMessageTask != null) {
            autoMessageTask.cancel();
            autoMessageTask = null;
        }
        if (holidayEffectTask != null) {
            holidayEffectTask.cancel();
            holidayEffectTask = null;
        }

        // Отключение хуков
        if (hookManager != null) {
            hookManager.unloadHooks();
        }

        log.warn("════════════════════════════════════════════");
        log.warn("❌ Welcome-плагин был отключён.");
        log.warn("Версия: {} - Спасибо за использование!", getDescription().getVersion());
        log.warn("════════════════════════════════════════════");

        // Обнуляем статический инстанс, чтобы getInstance() не возвращал "мёртвый" объект
        instance = null;
    }

    /**
     * Регистрация команд через Paper Brigadier API
     */
    private void registerCommands() {
        var lifecycleManager = this.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            this.commandManager = new CommandManager(this, event.registrar());
        });
    }

    /**
     * Регистрация слушателей событий
     */
    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new GuiListener(this), this);
    }

    /**
     * Инициализация метрик bStats
     */
    private void initMetrics() {
        try {
            new Metrics(this, BSTATS_PLUGIN_ID);
        } catch (Exception e) {
            log.warn("Не удалось инициализировать bStats", e);
        }
    }

    /**
     * Запуск периодических задач
     */
    private void initTasks() {
        // Авто-сообщения
        if (config.getBoolean("auto_messages.enabled", true)) {
            AutoMessageConfig amConfig = new AutoMessageConfig(config);
            autoMessageTask = new AutoMessageTask(this, amConfig);
            autoMessageTask.start();
        }

        // Праздничные эффекты
        if (config.getBoolean("settings.holidays.enabled", false)) {
            HolidayConfig hConfig = new HolidayConfig(config);
            holidayEffectTask = new HolidayEffectTask(this, hConfig);
            holidayEffectTask.start();
        }
    }

    /**
     * Вывод информации о запуске плагина
     */
    private void printStartupInfo() {
        String papiStatus = hookManager.isPapiEnabled() ? "✓ Да" : "✗ Нет";
        String vaultStatus = hookManager.isVaultEnabled() ? "✓ Да" : "✗ Нет";
        String autoMsgStatus = status(config.getBoolean("auto_messages.enabled", true), "Включены", "Выключены");
        String holidayStatus = status(config.getBoolean("settings.holidays.enabled", false), "Включены", "Выключены");

        log.info("════════════════════════════════════════════");
        log.info("🌟 Welcome плагин успешно включен!");
        log.info("Версия: {} by luci 👨‍💻", getDescription().getVersion());
        log.info("Сервер: {} {}", getServer().getName(), getServer().getMinecraftVersion());
        log.info("PlaceholderAPI: {}", papiStatus);
        log.info("Vault: {}", vaultStatus);
        log.info("Язык: {}", languageManager.getCurrentLanguage());
        log.info("Авто-сообщения: {}", autoMsgStatus);
        log.info("Праздничные эффекты: {}", holidayStatus);
        log.info("════════════════════════════════════════════");

        // Красивый баннер
        printBanner();
    }

    /**
     * Универсальный форматтер статуса "включено/выключено"
     */
    private String status(boolean enabled, String on, String off) {
        return enabled ? "✓ " + on : "✗ " + off;
    }

    /**
     * Вывод ASCII баннера
     */
    private void printBanner() {
        Component banner = Component.text()
                .append(Component.text("\n╔══════════════════════════════════╗\n", NamedTextColor.GOLD))
                .append(Component.text("║     ", NamedTextColor.GOLD))
                .append(Component.text("Welcome Plugin v" + getDescription().getVersion(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text("     ║\n", NamedTextColor.GOLD))
                .append(Component.text("║   Made with ", NamedTextColor.GOLD))
                .append(Component.text("♥", NamedTextColor.RED))
                .append(Component.text(" by luci   ║\n", NamedTextColor.GOLD))
                .append(Component.text("╚══════════════════════════════════╝\n", NamedTextColor.GOLD))
                .build();

        Bukkit.getConsoleSender().sendMessage(banner);
    }

    // ==================== Геттеры ====================

    public ConfigHandler getConfigHandler() { return config; }
    public HookManager getHookManager() { return hookManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public UpdateManager getUpdateManager() { return updateManager; }
    public AdapterManager getAdapterManager() { return adapterManager; }
    public WelcomeAPI getAPI() { return api; }
    public AutoMessageTask getAutoMessageTask() { return autoMessageTask; }
    public HolidayEffectTask getHolidayEffectTask() { return holidayEffectTask; }
    public CommandManager getCommandManager() { return commandManager; }
}