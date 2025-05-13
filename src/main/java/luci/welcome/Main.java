package luci.welcome;

import luci.welcome.event.PlayerDeathListener;
import luci.welcome.event.PlayerJoinListener;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private boolean papiEnabled;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        checkConfigVersion();
        new CommandManager(this);  // Команды, если они есть

        // Регистрация обработчиков событий
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);

        // Проверка и запуск статистики, если она включена в конфиге
        FileConfiguration config = getConfig();
        boolean statsEnabled = config.getBoolean("stats.enable", false);
        if (statsEnabled) {
            int interval = config.getInt("stats.interval", 3600);
            StatsManager.startStatsCollection(this, interval);
        }

        // Отложенная проверка PlaceholderAPI
        Bukkit.getScheduler().runTask(this, () -> {
            papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
            if (papiEnabled) {
                Bukkit.getConsoleSender().sendMessage("✅ §6PlaceholderAPI найден и будет использоваться.");
            } else {
                Bukkit.getConsoleSender().sendMessage("⚠ §6PlaceholderAPI не найден. Плейсхолдеры работать не будут.");
            }

            // Приветственное сообщение в консоль
            Bukkit.getConsoleSender().sendMessage("§8════════════════════════════════════════════");
            Bukkit.getConsoleSender().sendMessage("§6[Welcome] §eПлагин успешно загружен! 🌟");
            Bukkit.getConsoleSender().sendMessage("§6Версия: §a2.0 §7by luci");
            Bukkit.getConsoleSender().sendMessage("§6Используем PlaceholderAPI: " + (papiEnabled ? "§3Да" : "Нет"));
            Bukkit.getConsoleSender().sendMessage("§8════════════════════════════════════════════");
        });
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§4❌ Welcome-плагин был отключён.");
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    public String parsePlaceholders(org.bukkit.entity.Player player, String text) {
        if (text == null) return "";
        if (papiEnabled && player != null) {
            return PlaceholderAPI.setPlaceholders(player, text);  // Используем PlaceholderAPI
        }
        return text;
    }

    private void checkConfigVersion() {
        FileConfiguration config = getConfig();
        double version = config.getDouble("config-version", 2.1);
        if (version < 2.1) {
            Bukkit.getConsoleSender().sendMessage("§4❗ Обнаружена старая версия конфига. Пересоздание...");
            getConfig().options().copyDefaults(true);
            saveResource("config.yml", true);
            reloadConfig();
        }
    }
}
