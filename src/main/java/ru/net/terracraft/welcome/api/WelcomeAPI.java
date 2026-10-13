package ru.net.terracraft.welcome.api;

import org.bukkit.entity.Player;
import ru.net.terracraft.welcome.WelcomePlugin;
import ru.net.terracraft.welcome.config.ConfigLoader;
import ru.net.terracraft.welcome.core.PluginContext.ServiceRegistry;
import ru.net.terracraft.welcome.core.feature.HolidayService;
import ru.net.terracraft.welcome.core.i18n.LanguageManager;
import ru.net.terracraft.welcome.core.util.Utils;

/**
 * Публичный API плагина Welcome.
 * Получить: Bukkit.getServicesManager().load(WelcomeAPI.class)
 */
public class WelcomeAPI {

    private final WelcomePlugin plugin;
    private final ServiceRegistry registry;

    public WelcomeAPI(WelcomePlugin plugin, ServiceRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    // ═══════════════ ACTIONS ═══════════════

    /**
     * Отправить приветствие игроку.
     */
    public void sendWelcomeMessage(Player player) {
        if (player == null || !player.isOnline()) return;

        String template = "<gold>Добро пожаловать, <yellow>%player_name%</yellow>!</gold>";
        player.sendMessage(Utils.parse(template, player));
    }

    /**
     * Перезагрузить конфиг и языки.
     */
    public void reload() {
        try {
            registry.get(ConfigLoader.class).load();
            registry.get(LanguageManager.class).loadLanguages();
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка reload через API: " + e.getMessage());
        }
    }

    // ═══════════════ LANGUAGE ═══════════════

    /**
     * Получить текущий язык.
     */
    public String getCurrentLanguage() {
        try {
            return registry.get(LanguageManager.class).getCurrentLanguage();
        } catch (Exception e) {
            return "ru";
        }
    }

    /**
     * Сменить язык.
     */
    public boolean setLanguage(String code) {
        try {
            var lang = registry.get(LanguageManager.class);
            if (!lang.hasLanguage(code)) return false;
            lang.setLanguage(code);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ═══════════════ HOLIDAYS ═══════════════

    /**
     * Проверить, включены ли праздники.
     */
    public boolean isHolidaysEnabled() {
        try {
            return registry.get(HolidayService.class).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    // ═══════════════ INFO ═══════════════

    /**
     * Получить версию плагина.
     */
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    /**
     * Проверить наличие PlaceholderAPI.
     */
    public boolean isPlaceholderAPIEnabled() {
        return plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    /**
     * Проверить наличие Vault.
     */
    public boolean isVaultEnabled() {
        return plugin.getServer().getPluginManager().getPlugin("Vault") != null;
    }
}