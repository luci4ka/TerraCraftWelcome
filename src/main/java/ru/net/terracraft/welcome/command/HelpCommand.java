package ru.net.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import ru.net.terracraft.welcome.WelcomePlugin;

/**
 * /welcome help — справка по командам.
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

                    var builder = Component.text()
                            .append(Component.text("╔══════════════════════════════════════╗\n",
                                    NamedTextColor.GOLD))
                            .append(Component.text("║   ", NamedTextColor.GOLD))
                            .append(Component.text("Welcome Plugin — Помощь",
                                    NamedTextColor.YELLOW, TextDecoration.BOLD))
                            .append(Component.text("\n", NamedTextColor.GOLD))
                            .append(Component.text("╠══════════════════════════════════════╣\n",
                                    NamedTextColor.GOLD));

                    // Общие
                    builder = builder
                            .append(Component.text("║ /welcome", NamedTextColor.YELLOW))
                            .append(Component.text(" — Приветствие\n", NamedTextColor.WHITE))
                            .append(Component.text("║ /welcome version", NamedTextColor.YELLOW))
                            .append(Component.text(" — Версия\n", NamedTextColor.WHITE))
                            .append(Component.text("║ /welcome help", NamedTextColor.YELLOW))
                            .append(Component.text(" — Эта справка\n", NamedTextColor.WHITE));

                    // Админские
                    if (isAdmin) {
                        builder = builder
                                .append(Component.text("╠══════════════════════════════════════╣\n",
                                        NamedTextColor.GOLD))
                                .append(Component.text("║ ", NamedTextColor.RED))
                                .append(Component.text("Администратор:", NamedTextColor.RED, TextDecoration.BOLD))
                                .append(Component.text("\n", NamedTextColor.GOLD))
                                .append(Component.text("║ /welcome reload", NamedTextColor.YELLOW))
                                .append(Component.text(" — Перезагрузка\n", NamedTextColor.WHITE))
                                .append(Component.text("║ /welcome migrate", NamedTextColor.YELLOW))
                                .append(Component.text(" — Обновить конфиг\n", NamedTextColor.WHITE))
                                .append(Component.text("║ /welcome gui", NamedTextColor.YELLOW))
                                .append(Component.text(" — Меню настроек\n", NamedTextColor.WHITE))
                                .append(Component.text("║ /welcome language <lang>", NamedTextColor.YELLOW))
                                .append(Component.text(" — Сменить язык\n", NamedTextColor.WHITE));
                    }

                    builder = builder.append(Component.text(
                            "╚══════════════════════════════════════╝", NamedTextColor.GOLD));

                    sender.sendMessage(builder.build());
                });
    }
}