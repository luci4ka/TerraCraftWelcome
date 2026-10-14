package ru.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.core.feature.GuiService;
import ru.terracraft.welcome.core.i18n.LanguageManager;
import ru.terracraft.welcome.core.i18n.MessageKey;

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
                    LanguageManager lang = plugin.getServiceRegistry().get(LanguageManager.class);

                    try {
                        GuiService guiService = plugin.getServiceRegistry().get(GuiService.class);
                        guiService.open(player);
                    } catch (Exception e) {
                        String raw = lang.getMessage(MessageKey.ERROR_GUI_OPEN)
                                .replace("%error%", String.valueOf(e.getMessage()));
                        player.sendMessage(lang.render(raw, player));
                        plugin.getLogger().warning("Ошибка открытия GUI: " + e.getMessage());
                    }
                });
    }
}