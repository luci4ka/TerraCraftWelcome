package luci.welcome;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    private boolean papiEnabled;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        checkConfigVersion();
        new EventListener(this);
        new CommandManager(this);

        papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (papiEnabled) {
            getLogger().info("✅ PlaceholderAPI найден и будет использоваться.");
        } else {
            getLogger().warning("⚠ PlaceholderAPI не найден. Плейсхолдеры работать не будут.");
        }

        getLogger().info("✅ Welcome-плагин успешно загружен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ Welcome-плагин был отключён.");
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    public String parsePlaceholders(org.bukkit.entity.Player player, String text) {
        if (text == null) return "";
        if (papiEnabled && player != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    private void checkConfigVersion() {
        FileConfiguration config = getConfig();
        int version = config.getInt("config-version", 1);
        if (version < 2) {
            getLogger().warning("❗ Обнаружена старая версия конфига. Пересоздание...");
            getConfig().options().copyDefaults(true);
            saveResource("config.yml", true);
            reloadConfig();
        }
    }
}
