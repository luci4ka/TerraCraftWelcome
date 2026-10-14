package ru.terracraft.welcome.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.core.PluginContext.ServiceRegistry;
import ru.terracraft.welcome.core.feature.HolidayService;
import ru.terracraft.welcome.core.i18n.LanguageManager;
import ru.terracraft.welcome.core.i18n.MessageKey;
import ru.terracraft.welcome.core.integration.IntegrationManager;

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
     * Текст берётся из lang/*.yml на локали игрока.
     * Если игрок первый раз — используется join.first_join,
     * иначе — join.default (первая строка).
     */
    public void sendWelcomeMessage(Player player) {
        if (player == null || !player.isOnline()) return;

        LanguageManager lang = getLanguageManager();
        if (lang == null) {
            // Fallback, если registry недоступен
            player.sendMessage(Component.text("Welcome, " + player.getName() + "!"));
            return;
        }

        boolean firstJoin = !player.hasPlayedBefore();

        MessageKey key = firstJoin ? MessageKey.JOIN_FIRST : MessageKey.JOIN_DEFAULT;
        var messages = lang.getMessageList(player, key);

        if (messages.isEmpty()) {
            // Совсем fallback
            player.sendMessage(Component.text("Welcome, " + player.getName() + "!"));
            return;
        }

        // Берём случайную строку (или первую — тут выбор за нами)
        String raw = messages.get(0);

        // Применяем PAPI (если доступно) + MiniMessage + плейсхолдеры
        Component message = render(player, raw);
        player.sendMessage(message);
    }

    /**
     * Перезагрузить конфиг, языки и сервисы.
     * Делегирует в PluginContext.reload().
     */
    public void reload() {
        if (plugin.context() == null) {
            plugin.getLogger().warning("Нельзя перезагрузить — PluginContext не инициализирован");
            return;
        }
        plugin.context().reload();
    }

    // ═══════════════ LANGUAGE ═══════════════

    /**
     * Получить текущий язык.
     */
    public String getCurrentLanguage() {
        LanguageManager lang = getLanguageManager();
        return lang != null ? lang.getCurrentLanguage() : "ru";
    }

    /**
     * Сменить язык.
     */
    public boolean setLanguage(String code) {
        LanguageManager lang = getLanguageManager();
        if (lang == null) return false;
        if (!lang.hasLanguage(code)) return false;
        lang.setLanguage(code);
        return true;
    }

    // ═══════════════ HOLIDAYS ═══════════════

    /**
     * Проверить, включены ли праздники.
     */
    public boolean isHolidaysEnabled() {
        try {
            HolidayService holiday = registry.get(HolidayService.class);
            return holiday != null && holiday.isEnabled();
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

    // ═══════════════ INTERNAL ═══════════════

    private LanguageManager getLanguageManager() {
        try {
            return registry.get(LanguageManager.class);
        } catch (Exception e) {
            return null;
        }
    }

    private IntegrationManager getIntegrationManager() {
        try {
            return registry.get(IntegrationManager.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Рендер: PAPI → MiniMessage + плейсхолдеры.
     */
    private Component render(Player player, String raw) {
        String prepared = raw;

        IntegrationManager integrations = getIntegrationManager();
        if (integrations != null && integrations.isPapiEnabled()
                && integrations.getPlaceholder() != null) {
            prepared = integrations.getPlaceholder().parse(player, prepared);
        }

        LanguageManager lang = getLanguageManager();
        if (lang == null) {
            return Component.text(prepared);
        }
        return lang.render(prepared, player);
    }
}