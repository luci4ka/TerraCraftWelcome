package com.welcome;

import com.welcome.bstat.Metrics;
import com.welcome.command.CommandManager;
import com.welcome.config.ConfigHandler;
import com.welcome.listener.BossFightListener;
import com.welcome.listener.PlayerDeathListener;
import com.welcome.listener.PlayerJoinListener;
import com.welcome.task.AutoMessageTask;
import com.welcome.task.HolidayEffectTask;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    private boolean papiEnabled;
    private ConfigHandler config;

    public static Main getInstance() {
        return instance;
    }

    public ConfigHandler getConfigHandler() {
        return config;
    }

    @Override
    public void onEnable() {
        instance = this;

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
            new AutoMessageTask(this, config).start();
        }

        // Holiday эффекты
        if (config.getBoolean("holidays.enabled", false)) {
            new HolidayEffectTask(this, config).start();
        }

        // Консоль
        printConsoleInfo();
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§8════════════════════════════════════════════");
        getServer().getConsoleSender().sendMessage("§4❌ Welcome-плагин был отключён.");
        getServer().getConsoleSender().sendMessage("§8════════════════════════════════════════════");
    }

    public String parsePlaceholders(org.bukkit.entity.Player player, String text) {
        if (text == null) return "";
        if (papiEnabled && player != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    private void printConsoleInfo() {
        boolean papiEnabled = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        boolean autoMessagesEnabled = config.getBoolean("auto_messages.enabled", true);
        boolean holidaysEnabled = config.getBoolean("settings.holidays.enabled", false);
        boolean bossfightEnabled = config.getBoolean("settings.boss_fight.enabled", true);

        getServer().getConsoleSender().sendMessage("§8════════════════════════════════════════════");
        getServer().getConsoleSender().sendMessage("§b[Welcome] 🌟 Плагин успешно включен!");
        getServer().getConsoleSender().sendMessage("§6Версия: §a2.2 §7by §3luci §f👨‍💻");
        getServer().getConsoleSender().sendMessage("§6PlaceholderAPI: " + (papiEnabled ? "§3Да ✅" : "§cНет ❌"));
        getServer().getConsoleSender().sendMessage("§6Авто-сообщения: " + (autoMessagesEnabled ? "§aВключены ✅" : "§cВыключены ❌"));
        getServer().getConsoleSender().sendMessage("§6Праздничные эффекты: " + (holidaysEnabled ? "§aВключены ✅" : "§cВыключены ❌"));
        getServer().getConsoleSender().sendMessage("§6Эффекты боссов: " + (bossfightEnabled ? "§aВключены ✅" : "§cВыключены ❌"));
        getServer().getConsoleSender().sendMessage("§8════════════════════════════════════════════");
    }
}
