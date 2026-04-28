package com.luci.welcome.api;

import com.luci.welcome.Main;
import com.luci.welcome.config.ConfigHandler;
import com.luci.welcome.hook.HookManager;
import org.bukkit.entity.Player;

public class WelcomeAPI {

    private final Main plugin;

    public WelcomeAPI(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Отправить приветственное сообщение игроку
     */
    public void sendWelcomeMessage(Player player) {
        ConfigHandler config = plugin.getConfigHandler();
        String message = config.getConfig().getString("messages.welcome", "Добро пожаловать, %player%!");
        message = plugin.getHookManager().isPapiEnabled()
                ? plugin.getHookManager().getPapiHook().parse(player, message)
                : message.replace("%player%", player.getName());

        player.sendMessage(message);
    }

    /**
     * Проверить, загружен ли PlaceholderAPI
     */
    public boolean isPlaceholderAPIEnabled() {
        HookManager hooks = plugin.getHookManager();
        return hooks != null && hooks.isPapiEnabled();
    }

    /**
     * Проверить, загружен ли Vault
     */
    public boolean isVaultEnabled() {
        HookManager hooks = plugin.getHookManager();
        return hooks != null && hooks.isVaultEnabled();
    }

    /**
     * Получить версию плагина
     */
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * Перезагрузить конфигурацию
     */
    public void reload() {
        plugin.getConfigHandler().reloadConfig();
    }
}