package ru.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.core.lang.LanguageManager;
import ru.terracraft.welcome.core.lang.MessageKey;

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
                        plugin.context().reload();

                        long time = System.currentTimeMillis() - start;

                        LanguageManager lang = plugin.getServiceRegistry().get(LanguageManager.class);
                        String raw = lang.getMessage(MessageKey.COMMAND_RELOAD_SUCCESS)
                                .replace("%time%", String.valueOf(time));

                        sender.sendMessage(lang.render(raw));

                    } catch (Exception e) {
                        LanguageManager lang = plugin.getServiceRegistry().get(LanguageManager.class);
                        String raw = lang.getMessage(MessageKey.COMMAND_RELOAD_ERROR)
                                .replace("%error%", String.valueOf(e.getMessage()));

                        sender.sendMessage(lang.render(raw));
                        plugin.getLogger().warning("Ошибка reload: " + e.getMessage());
                    }
                });
    }
}