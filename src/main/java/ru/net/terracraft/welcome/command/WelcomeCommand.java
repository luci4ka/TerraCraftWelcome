package ru.net.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.net.terracraft.welcome.WelcomePlugin;
import ru.net.terracraft.welcome.api.WelcomeAPI;

/**
 * Основная команда /welcome.
 * Показывает приветствие игроку.
 */
public class WelcomeCommand {

    private final WelcomePlugin plugin;

    public WelcomeCommand(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand build() {
        return new CommandAPICommand("welcome")
                .withPermission(CommandPermission.fromString("welcome.use"))
                .withAliases("welc", "wlc")
                .executesPlayer((player, args) -> {
                    try {
                        WelcomeAPI api = plugin.getServer().getServicesManager()
                                .load(WelcomeAPI.class);
                        if (api != null) {
                            api.sendWelcomeMessage(player);
                        } else {
                            player.sendMessage(Component.text(
                                    "Добро пожаловать, " + player.getName() + "!",
                                    NamedTextColor.GOLD));
                        }
                    } catch (Exception e) {
                        player.sendMessage(Component.text(
                                "Ошибка: " + e.getMessage(),
                                NamedTextColor.RED));
                    }
                });
    }
}