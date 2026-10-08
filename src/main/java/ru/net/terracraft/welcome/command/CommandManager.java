package ru.net.terracraft.welcome.command;

import ru.net.terracraft.welcome.Main;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandManager {

    private final Main plugin;

    public CommandManager(Main plugin, Commands commands) {
        this.plugin = plugin;
        register(commands);
    }

    private void register(Commands commands) {
        commands.register(
                Commands.literal("welcome")
                        .requires(source -> source.getSender().hasPermission("welcome.use"))
                        .executes(this::handleWelcome)
                        .then(Commands.literal("reload")
                                .requires(source -> source.getSender().hasPermission("welcome.admin"))
                                .executes(this::handleReload)
                        )
                        .then(Commands.literal("version")
                                .executes(this::handleVersion)
                        )
                        .then(Commands.literal("help")
                                .executes(this::handleHelp)
                        )
                        .then(Commands.literal("gui")
                                .requires(source -> source.getSender().hasPermission("welcome.admin"))
                                .executes(this::handleGui)
                        )
                        .then(Commands.literal("language")
                                .requires(source -> source.getSender().hasPermission("welcome.admin"))
                                .then(Commands.argument("lang", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            plugin.getLanguageManager().getAvailableLanguages()
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(this::handleLanguage)
                                )
                        )
                        .build(),
                "Основная команда Welcome плагина",
                List.of("welc", "wlc")
        );
    }

    /**
     * /welcome - Показать приветствие
     */
    private int handleWelcome(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getSender() instanceof Player player) {
            plugin.getAPI().sendWelcomeMessage(player);
        } else {
            source.getSender().sendMessage(
                    Component.text("✘ Эта команда только для игроков! Используйте консоль для администрирования.",
                            NamedTextColor.RED)
            );
        }
        return 1;
    }

    /**
     * /welcome reload - Перезагрузка конфигурации
     */
    private int handleReload(CommandContext<CommandSourceStack> context) {
        long start = System.currentTimeMillis();
        plugin.getConfigHandler().reloadConfig();
        long time = System.currentTimeMillis() - start;

        context.getSource().getSender().sendMessage(
                Component.text()
                        .append(Component.text("✔ ", NamedTextColor.GREEN))
                        .append(Component.text("Конфигурация перезагружена ", NamedTextColor.GREEN))
                        .append(Component.text("(за " + time + "мс)", NamedTextColor.GRAY))
                        .build()
        );
        return 1;
    }

    /**
     * /welcome version - Информация о плагине
     */
    private int handleVersion(CommandContext<CommandSourceStack> context) {
        String serverName = plugin.getServer().getName();
        String mcVersion = plugin.getServer().getMinecraftVersion();
        boolean papi = plugin.getHookManager().isPapiEnabled();
        boolean vault = plugin.getHookManager().isVaultEnabled();

        var builder = Component.text()
                .append(Component.text("  ╔════════════════════════════╗\n", NamedTextColor.GOLD))
                .append(Component.text("  ║  ", NamedTextColor.GOLD))
                .append(Component.text("Welcome Plugin", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text("          ║\n", NamedTextColor.GOLD))
                .append(Component.text("  ║  v" + plugin.getDescription().getVersion() + " by ", NamedTextColor.GOLD))
                .append(Component.text("luci", NamedTextColor.AQUA))
                .append(Component.text("            ║\n", NamedTextColor.GOLD))
                .append(Component.text("  ╠════════════════════════════╣\n", NamedTextColor.GOLD))
                .append(Component.text("  ║  Сервер: " + serverName + " ".repeat(Math.max(0, 19 - serverName.length())) + "║\n", NamedTextColor.GOLD))
                .append(Component.text("  ║  MC: " + mcVersion + " ".repeat(Math.max(0, 22 - mcVersion.length())) + "║\n", NamedTextColor.GOLD))
                .append(Component.text("  ║  PAPI: " + (papi ? "✔ Да" : "✘ Нет") + " ".repeat(Math.max(0, papi ? 14 : 13)) + "║\n", NamedTextColor.GOLD))
                .append(Component.text("  ║  Vault: " + (vault ? "✔ Да" : "✘ Нет") + " ".repeat(Math.max(0, vault ? 13 : 12)) + "║\n", NamedTextColor.GOLD))
                .append(Component.text("  ╚════════════════════════════╝", NamedTextColor.GOLD));

        context.getSource().getSender().sendMessage(builder.build());
        return 1;
    }

    /**
     * /welcome help - Справка по командам
     */
    private int handleHelp(CommandContext<CommandSourceStack> context) {
        boolean isAdmin = context.getSource().getSender().hasPermission("welcome.admin");

        // Используем TextComponent.Builder
        var builder = Component.text()
                .append(Component.text("  ╔══════════════════════════════════════╗\n", NamedTextColor.GOLD))
                .append(Component.text("  ║     ", NamedTextColor.GOLD))
                .append(Component.text("Welcome Plugin - Помощь", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text("     ║\n", NamedTextColor.GOLD))
                .append(Component.text("  ╠══════════════════════════════════════╣\n", NamedTextColor.GOLD))

                // Команды для всех
                .append(Component.text("  ║ ", NamedTextColor.GOLD))
                .append(Component.text("/welcome", NamedTextColor.YELLOW))
                .append(Component.text(" - Приветствие", NamedTextColor.WHITE))
                .append(Component.text(" ".repeat(18)))
                .append(Component.text("║\n", NamedTextColor.GOLD))

                .append(Component.text("  ║ ", NamedTextColor.GOLD))
                .append(Component.text("/welcome version", NamedTextColor.YELLOW))
                .append(Component.text(" - Версия плагина", NamedTextColor.WHITE))
                .append(Component.text(" ".repeat(13)))
                .append(Component.text("║\n", NamedTextColor.GOLD))

                .append(Component.text("  ║ ", NamedTextColor.GOLD))
                .append(Component.text("/welcome help", NamedTextColor.YELLOW))
                .append(Component.text(" - Эта справка", NamedTextColor.WHITE))
                .append(Component.text(" ".repeat(18)))
                .append(Component.text("║\n", NamedTextColor.GOLD));

        // Админские команды
        if (isAdmin) {
            builder = builder
                    .append(Component.text("  ╠══════════════════════════════════════╣\n", NamedTextColor.GOLD))
                    .append(Component.text("  ║  ", NamedTextColor.GOLD))
                    .append(Component.text("Администратор:", NamedTextColor.RED, TextDecoration.BOLD))
                    .append(Component.text(" ".repeat(24)))
                    .append(Component.text("║\n", NamedTextColor.GOLD))

                    .append(Component.text("  ║ ", NamedTextColor.GOLD))
                    .append(Component.text("/welcome reload", NamedTextColor.YELLOW))
                    .append(Component.text(" - Перезагрузка", NamedTextColor.WHITE))
                    .append(Component.text(" ".repeat(16)))
                    .append(Component.text("║\n", NamedTextColor.GOLD))

                    .append(Component.text("  ║ ", NamedTextColor.GOLD))
                    .append(Component.text("/welcome gui", NamedTextColor.YELLOW))
                    .append(Component.text(" - Меню настроек", NamedTextColor.WHITE))
                    .append(Component.text(" ".repeat(15)))
                    .append(Component.text("║\n", NamedTextColor.GOLD))

                    .append(Component.text("  ║ ", NamedTextColor.GOLD))
                    .append(Component.text("/welcome language <ru/en>", NamedTextColor.YELLOW))
                    .append(Component.text(" - Язык", NamedTextColor.WHITE))
                    .append(Component.text(" ".repeat(8)))
                    .append(Component.text("║\n", NamedTextColor.GOLD));
        }

        builder = builder
                .append(Component.text("  ╚══════════════════════════════════════╝", NamedTextColor.GOLD));

        // build() на билдере, получаем Component
        Component help = builder.build();
        context.getSource().getSender().sendMessage(help);
        return 1;
    }

    /**
     * /welcome gui - Открыть меню настроек
     */
    private int handleGui(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getSender() instanceof Player player) {
            new GuiCommand(plugin).open(player);
        } else {
            context.getSource().getSender().sendMessage(
                    Component.text("✘ GUI доступен только для игроков!", NamedTextColor.RED)
            );
        }
        return 1;
    }

    /**
     * /welcome language <lang> - Сменить язык
     */
    private int handleLanguage(CommandContext<CommandSourceStack> context) {
        String lang = context.getArgument("lang", String.class);

        if (!plugin.getLanguageManager().getAvailableLanguages().contains(lang)) {
            context.getSource().getSender().sendMessage(
                    Component.text("✘ Язык '" + lang + "' не найден! Доступные: " +
                                    String.join(", ", plugin.getLanguageManager().getAvailableLanguages()),
                            NamedTextColor.RED)
            );
            return 0;
        }

        plugin.getLanguageManager().setLanguage(lang);

        context.getSource().getSender().sendMessage(
                Component.text()
                        .append(Component.text("✔ ", NamedTextColor.GREEN))
                        .append(Component.text("Язык изменён на: ", NamedTextColor.GREEN))
                        .append(Component.text(lang.toUpperCase(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                        .build()
        );
        return 1;
    }
}