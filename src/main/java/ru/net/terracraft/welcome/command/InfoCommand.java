package ru.net.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import ru.net.terracraft.welcome.WelcomePlugin;

/**
 * /welcome version — информация о плагине.
 */
public class InfoCommand {

    private final WelcomePlugin plugin;

    public InfoCommand(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand build() {
        return new CommandAPICommand("info")
                .executes((sender, args) -> {
                    var meta = plugin.getPluginMeta();
                    boolean papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
                    boolean vault = plugin.getServer().getPluginManager().getPlugin("Vault") != null;
                    boolean luckPerms = plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null;

                    Component message = Component.text()
                            .append(Component.text("╔══════════════════════════════════════╗\n", NamedTextColor.GOLD))
                            .append(Component.text("║     ", NamedTextColor.GOLD))
                            .append(Component.text("Welcome Plugin", NamedTextColor.YELLOW, TextDecoration.BOLD))
                            .append(Component.text("        ║\n", NamedTextColor.GOLD))
                            .append(Component.text("║   v" + meta.getVersion() + " by luci", NamedTextColor.GOLD))
                            .append(Component.text("       ║\n", NamedTextColor.GOLD))
                            .append(Component.text("╠══════════════════════════════════════╣\n", NamedTextColor.GOLD))
                            .append(Component.text("║   PlaceholderAPI: " + (papi ? "✔" : "✘") + "\n", NamedTextColor.GOLD))
                            .append(Component.text("║   Vault: " + (vault ? "✔" : "✘") + "\n", NamedTextColor.GOLD))
                            .append(Component.text("║   LuckPerms: " + (luckPerms ? "✔" : "✘") + "\n", NamedTextColor.GOLD))
                            .append(Component.text("╚══════════════════════════════════════╝", NamedTextColor.GOLD))
                            .build();

                    sender.sendMessage(message);
                });
    }
}