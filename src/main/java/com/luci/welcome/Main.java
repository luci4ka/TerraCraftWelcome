package com.luci.welcome;

import com.luci.welcome.bstat.Metrics;
import com.luci.welcome.command.CommandManager;
import com.luci.welcome.config.AutoMessageConfig;
import com.luci.welcome.config.ConfigHandler;
import com.luci.welcome.config.HolidayConfig;
import com.luci.welcome.listener.BossFightListener;
import com.luci.welcome.listener.PlayerDeathListener;
import com.luci.welcome.listener.PlayerJoinListener;
import com.luci.welcome.task.AutoMessageTask;
import com.luci.welcome.task.HolidayEffectTask;
import com.luci.welcome.utils.LoggerUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public final class Main extends JavaPlugin {

    private static Main instance;
    private boolean papiEnabled;
    private ConfigHandler config;
    private LoggerUtil logUtil;

    public static Main getInstance() {
        return instance;
    }

    public ConfigHandler getConfigHandler() {
        return config;
    }

    @Override
    public void onEnable() {
        instance = this;

        Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(getDescription().getName());
        logUtil = new LoggerUtil(slf4jLogger);

        saveDefaultConfig();
        config = new ConfigHandler(this);

        // Проверка версии конфига
        double expectedVersion = 2.2;
        config.checkConfigVersion(expectedVersion);

        // Проверка PlaceholderAPI
        this.papiEnabled = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        config.setPapiEnabled(this.papiEnabled);

        // Регистрация команд
        new CommandManager(config);

        // События
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(config), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(config, this), this);
        getServer().getPluginManager().registerEvents(new BossFightListener(config), this);

        // bStats
        new Metrics(this, 27073);

        // Авто-сообщения
        if (config.getBoolean("auto_messages.enabled", true)) {
            AutoMessageConfig autoMessageConfig = new AutoMessageConfig(config);
            new AutoMessageTask(this, autoMessageConfig).start();
        }

        // Holiday эффекты
        if (config.getBoolean("holidays.enabled", false)) {
            HolidayConfig holidayConfig = new HolidayConfig(config);
            new HolidayEffectTask(this, holidayConfig).start();
        }


        // Эффекты боссов
        boolean bossfightEnabled = config.getBoolean("settings.boss_fight.enabled", true);

        // Информация о включении плагина
        printConsoleInfo();
    }

    @Override
    public void onDisable() {
        logUtil.warn("❌ Welcome-плагин был отключён.");
    }

    public String parsePlaceholders(org.bukkit.entity.Player player, String text) {
        if (text == null) return "";
        if (papiEnabled && player != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    private void printConsoleInfo() {
        logUtil.info("════════════════════════════════════════════", LoggerUtil.BLUE);
        logUtil.info("[Welcome] 🌟 Плагин успешно включен!");
        logUtil.info("Версия: 2.2 by luci 👨‍💻");
        logUtil.info("PlaceholderAPI: " + (papiEnabled ? "Да" : "Нет"));
        logUtil.info("Авто-сообщения: " + (config.getBoolean("auto_messages.enabled", true) ? "Включены" : "Выключены"));
        logUtil.info("Праздничные эффекты: " + (config.getBoolean("holidays.enabled", false) ? "Включены" : "Выключены"));
        logUtil.info("Эффекты боссов: " + (config.getBoolean("settings.boss_fight.enabled", true) ? "Включены" : "Выключены"));
        logUtil.info("════════════════════════════════════════════", LoggerUtil.BLUE);
    }
}
