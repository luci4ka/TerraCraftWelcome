package luci.welcome;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);  // Сохраняем дефолтные значения

        // Регистрируем слушателя событий
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        // Логируем запуск плагина
        getLogger().info("WelcomePlugin включен!");
    }

    @Override
    public void onDisable() {
        // Логируем выключение плагина
        getLogger().info("WelcomePlugin выключен.");
    }
}
