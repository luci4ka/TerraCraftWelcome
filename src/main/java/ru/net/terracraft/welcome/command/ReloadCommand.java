package ru.net.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.net.terracraft.welcome.WelcomePlugin;
import ru.net.terracraft.welcome.config.ConfigLoader;
import ru.net.terracraft.welcome.core.i18n.LanguageManager;

/**
 * /welcome reload — перезагрузка конфига и языков.
 */
public class ReloadCommand {

    private final WelcomePlugin plugin;

    public ReloadCommand(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand build() {
        return new CommandAPICommand("reload")
                .withPermission(CommandPermission.fromString("welcome.admin"))
                .executes((sender, args) -> {
                    long start = System.currentTimeMillis();

                    try {
                        // Перезагружаем конфиг
                        var configLoader = plugin.getServiceRegistry().get(ConfigLoader.class);
                        configLoader.load();

                        // Перезагружаем языки
                        var langManager = plugin.getServiceRegistry().get(LanguageManager.class);
                        langManager.loadLanguages();

                        long time = System.currentTimeMillis() - start;

                        sender.sendMessage(Component.text()
                                .append(Component.text("✔ ", NamedTextColor.GREEN))
                                .append(Component.text("Конфигурация перезагружена ",
                                        NamedTextColor.GREEN))
                                .append(Component.text("(" + time + "мс)", NamedTextColor.GRAY))
                                .build());
                    } catch (Exception e) {
                        sender.sendMessage(Component.text()
                                .append(Component.text("✘ Ошибка перезагрузки: ",
                                        NamedTextColor.RED))
                                .append(Component.text(e.getMessage(), NamedTextColor.WHITE))
                                .build());
                        plugin.getLogger().warning("Ошибка reload: " + e.getMessage());
                    }
                });
    }
}