package ru.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import ru.terracraft.welcome.WelcomePlugin;

/**
 * Регистрация всех команд плагина.
 * Одна основная команда /welcome с подкомандами.
 */
public class CommandRegistrar {

    private final WelcomePlugin plugin;

    public CommandRegistrar(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Регистрирует все команды.
     */
    public void registerAll() {
        CommandAPICommand welcome = new WelcomeCommand(plugin).build();

        // Добавляем подкоманды
        welcome.withSubcommand(new ReloadCommand(plugin).build());
        welcome.withSubcommand(new InfoCommand(plugin).build());
        welcome.withSubcommand(new HelpCommand(plugin).build());
        welcome.withSubcommand(new GuiCommand(plugin).build());

        // Регистрируем
        welcome.register();

        plugin.getLogger().info("Зарегистрировано команд: 1 (/welcome с подкомандами)");
    }
}