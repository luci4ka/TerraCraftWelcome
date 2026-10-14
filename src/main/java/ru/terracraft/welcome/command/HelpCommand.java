package ru.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.core.i18n.LanguageManager;
import ru.terracraft.welcome.core.i18n.MessageKey;

import java.util.List;

/**
 * /welcome help — справка по командам.
 * Все строки — из lang/*.lang (help.*).
 */
public class HelpCommand {

    private final WelcomePlugin plugin;

    public HelpCommand(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand build() {
        return new CommandAPICommand("help")
                .executes((sender, args) -> {
                    boolean isAdmin = sender.hasPermission("welcome.admin");

                    LanguageManager lang = plugin.getServiceRegistry().get(LanguageManager.class);

                    // Заголовок
                    appendAll(sender, lang, MessageKey.HELP_HEADER);

                    // Общие команды
                    appendAll(sender, lang, MessageKey.HELP_COMMANDS);

                    // Админские
                    if (isAdmin) {
                        appendAll(sender, lang, MessageKey.HELP_ADMIN);
                    }

                    // Подвал
                    appendAll(sender, lang, MessageKey.HELP_FOOTER);
                });
    }

    /**
     * Отправить все строки из lang-списка получателю.
     */
    private void appendAll(CommandSender sender, LanguageManager lang, MessageKey key) {
        List<String> lines;

        if (sender instanceof Player player) {
            lines = lang.getMessageList(player, key);
        } else {
            lines = lang.getMessageList(key);
        }

        for (String line : lines) {
            if (line == null || line.isEmpty()) continue;

            Component component = (sender instanceof Player player)
                    ? lang.render(line, player)
                    : lang.render(line);

            sender.sendMessage(component);
        }
    }
}