package ru.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.api.WelcomeAPI;
import ru.terracraft.welcome.core.i18n.LanguageManager;
import ru.terracraft.welcome.core.i18n.MessageKey;

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
                    LanguageManager lang = plugin.getServiceRegistry().get(LanguageManager.class);

                    try {
                        WelcomeAPI api = plugin.getServiceRegistry().get(WelcomeAPI.class);
                        api.sendWelcomeMessage(player);
                    } catch (IllegalStateException e) {
                        String raw = lang.getMessage(MessageKey.ERROR_API_UNAVAILABLE);
                        player.sendMessage(lang.render(raw, player));
                        plugin.getLogger().warning("WelcomeAPI не зарегистрирован: " + e.getMessage());
                    } catch (Exception e) {
                        String raw = lang.getMessage(MessageKey.ERROR_GENERIC)
                                .replace("%error%", String.valueOf(e.getMessage()));
                        player.sendMessage(lang.render(raw, player));
                        plugin.getLogger().warning("Ошибка /welcome: " + e.getMessage());
                    }
                });
    }
}