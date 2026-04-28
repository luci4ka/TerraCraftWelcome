package com.luci.welcome;

import com.luci.welcome.api.WelcomeAPI;
import com.luci.welcome.bstat.Metrics;
import com.luci.welcome.command.CommandManager;
import com.luci.welcome.config.ConfigHandler;
import com.luci.welcome.config.AutoMessageConfig;
import com.luci.welcome.config.HolidayConfig;
import com.luci.welcome.hook.HookManager;
import com.luci.welcome.listener.BossFightListener;
import com.luci.welcome.listener.GuiListener;
import com.luci.welcome.listener.PlayerDeathListener;
import com.luci.welcome.listener.PlayerJoinListener;
import com.luci.welcome.manager.LanguageManager;
import com.luci.welcome.manager.UpdateManager;
import com.luci.welcome.manager.AdapterManager;
import com.luci.welcome.task.AutoMessageTask;
import com.luci.welcome.task.HolidayEffectTask;
import com.luci.welcome.utils.LoggerUtil;
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

    private static Main instance;
    private ConfigHandler config;
    private LoggerUtil logUtil;
    private HookManager hookManager;
    private LanguageManager languageManager;
    private UpdateManager updateManager;
    private AdapterManager adapterManager;
    private AutoMessageTask autoMessageTask;
    private HolidayEffectTask holidayEffectTask;
    private WelcomeAPI api;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Инициализация логгера
        Logger slf4jLogger = LoggerFactory.getLogger(getDescription().getName());
        logUtil = new LoggerUtil(slf4jLogger);

        // Сохраняем дефолтные ресурсы
        saveDefaultResources();

        // Загрузка конфигурации
        config = new ConfigHandler(this);
        config.checkConfigVersion(2.2);

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
        }
        if (holidayEffectTask != null) {
            holidayEffectTask.cancel();
        }

        // Отключение хуков
        if (hookManager != null) {
            hookManager.unloadHooks();
        }

        logUtil.warn("════════════════════════════════════════════");
        logUtil.warn("❌ Welcome-плагин был отключён.");
        logUtil.warn("Версия: " + getDescription().getVersion() + " - Спасибо за использование!");
        logUtil.warn("════════════════════════════════════════════");
    }

    /**
     * Сохраняет все дефолтные ресурсы
     */
    private void saveDefaultResources() {
        // Основной конфиг
        saveDefaultConfig();

        // Языковые файлы
        String[] languages = {"ru.yml", "en.yml"};
        for (String lang : languages) {
            if (!new java.io.File(getDataFolder(), "languages/" + lang).exists()) {
                saveResource("languages/" + lang, false);
            }
        }
    }

    /**
     * Регистрация команд через Paper Brigadier API
     */
    private void registerCommands() {
        var lifecycleManager = this.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> new CommandManager(this, event.registrar()));
    }

    /**
     * Регистрация слушателей событий
     */
    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new BossFightListener(this), this);
        pm.registerEvents(new GuiListener(this), this);
    }

    /**
     * Инициализация метрик bStats
     */
    private void initMetrics() {
        try {
            new Metrics(this, 27073);
            logUtil.debug("Метрики bStats успешно запущены");
        } catch (Exception e) {
            logUtil.warn("Не удалось запустить метрики bStats: " + e.getMessage());
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
        String autoMsgStatus = config.getBoolean("auto_messages.enabled", true) ? "✓ Включены" : "✗ Выключены";
        String holidayStatus = config.getBoolean("settings.holidays.enabled", false) ? "✓ Включены" : "✗ Выключены";
        String bossStatus = config.getBoolean("settings.boss_fight.enabled", true) ? "✓ Включены" : "✗ Выключены";

        logUtil.info("════════════════════════════════════════════");
        logUtil.info("🌟 Welcome плагин успешно включен!");
        logUtil.info("Версия: " + getDescription().getVersion() + " by luci 👨‍💻");
        logUtil.info("Сервер: " + getServer().getName() + " " + getServer().getMinecraftVersion());
        logUtil.info("PlaceholderAPI: " + papiStatus);
        logUtil.info("Vault: " + vaultStatus);
        logUtil.info("Язык: " + languageManager.getCurrentLanguage());
        logUtil.info("Авто-сообщения: " + autoMsgStatus);
        logUtil.info("Праздничные эффекты: " + holidayStatus);
        logUtil.info("Эффекты боссов: " + bossStatus);
        logUtil.info("════════════════════════════════════════════");

        // Красивый баннер
        printBanner();
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

    // ============================
    // Геттеры
    // ============================
    // Геттеры для внешнего API (используются другими плагинами)
    @SuppressWarnings("unused")
    public ConfigHandler getConfigHandler() { return config; }

    @SuppressWarnings("unused")
    public HookManager getHookManager() { return hookManager; }

    @SuppressWarnings("unused")
    public LanguageManager getLanguageManager() { return languageManager; }

    @SuppressWarnings("unused")
    public UpdateManager getUpdateManager() { return updateManager; }

    @SuppressWarnings("unused")
    public AdapterManager getAdapterManager() { return adapterManager; }

    @SuppressWarnings("unused")
    public LoggerUtil getLogUtil() { return logUtil; }

    @SuppressWarnings("unused")
    public WelcomeAPI getAPI() { return api; }

    @SuppressWarnings("unused")
    public AutoMessageTask getAutoMessageTask() { return autoMessageTask; }

    @SuppressWarnings("unused")
    public HolidayEffectTask getHolidayEffectTask() { return holidayEffectTask; }
}