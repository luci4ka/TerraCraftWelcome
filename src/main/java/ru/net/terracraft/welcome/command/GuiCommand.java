package ru.net.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.net.terracraft.welcome.WelcomePlugin;
import ru.net.terracraft.welcome.core.feature.GuiService;

/**
 * /welcome gui — открыть меню настроек.
 */
public class GuiCommand {

    private final WelcomePlugin plugin;

    public GuiCommand(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand build() {
        return new CommandAPICommand("gui")
                .withPermission(CommandPermission.fromString("welcome.admin"))
                .executesPlayer((player, args) -> {
                    try {
                        var guiService = plugin.getServiceRegistry().get(GuiService.class);
                        guiService.open(player);
                    } catch (Exception e) {
                        player.sendMessage(Component.text(
                                "✘ Ошибка открытия меню: " + e.getMessage(),
                                NamedTextColor.RED));
                    }
                });
    }
}