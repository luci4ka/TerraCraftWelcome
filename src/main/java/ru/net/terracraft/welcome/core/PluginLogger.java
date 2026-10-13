package ru.net.terracraft.welcome.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import ru.net.terracraft.welcome.WelcomePlugin;

/**
 * Логирование старта/остановки плагина.
 * Стартовое сообщение + ASCII-баннер + прощание.
 */
public final class PluginLogger {

    private PluginLogger() {
    }

    // ═══════════════ START ═══════════════

    /**
     * Печатает информацию о запуске.
     */
    public static void logStartup(WelcomePlugin plugin) {
        String version = plugin.getPluginMeta().getVersion();
        String serverName = plugin.getServer().getName();
        String mcVersion = plugin.getServer().getMinecraftVersion();

        plugin.getLogger().info("════════════════════════════════════════════");
        plugin.getLogger().info("🌟 Welcome плагин успешно включён!");
        plugin.getLogger().info("Версия: " + version + " by luci");
        plugin.getLogger().info("Сервер: " + serverName + " " + mcVersion);
        plugin.getLogger().info("════════════════════════════════════════════");

        printBanner(plugin);
    }

    /**
     * ASCII-баннер в консоль.
     */
    private static void printBanner(WelcomePlugin plugin) {
        String version = plugin.getPluginMeta().getVersion();

        Component banner = Component.text()
                .append(Component.text("\n╔══════════════════════════════════╗\n", NamedTextColor.GOLD))
                .append(Component.text("║     ", NamedTextColor.GOLD))
                .append(Component.text("Welcome Plugin v" + version,
                        NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text("     ║\n", NamedTextColor.GOLD))
                .append(Component.text("║   Made with ", NamedTextColor.GOLD))
                .append(Component.text("♥", NamedTextColor.RED))
                .append(Component.text(" by luci   ║\n", NamedTextColor.GOLD))
                .append(Component.text("╚══════════════════════════════════╝\n", NamedTextColor.GOLD))
                .build();

        Bukkit.getConsoleSender().sendMessage(banner);
    }

    // ═══════════════ SHUTDOWN ═══════════════

    /**
     * Печатает информацию об остановке.
     */
    public static void logShutdown(WelcomePlugin plugin) {
        String version = plugin.getPluginMeta().getVersion();

        plugin.getLogger().warning("════════════════════════════════════════════");
        plugin.getLogger().warning("❌ Welcome-плагин отключён.");
        plugin.getLogger().warning("Версия: " + version + " — спасибо за использование!");
        plugin.getLogger().warning("════════════════════════════════════════════");
    }
}