package com.welcome.command;

import com.welcome.config.ConfigHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.List;

public class CommandManager {

    private final ConfigHandler config;

    public CommandManager(ConfigHandler config) {
        this.config = config;

        registerReloadCommand();
    }

    private void registerReloadCommand() {
        config.getPlugin().getServer().getCommandMap().register("welcomeplugin",
                new BukkitCommand("wconfig", "Перезагрузка конфигурации Welcome", null, List.of()) {
                    @Override
                    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                            config.reload();
                            sender.sendMessage("§aКонфигурация успешно перезагружена!");
                        } else {
                            sender.sendMessage("§cИспользование: /wconfig reload");
                        }
                        return true;
                    }
                });
    }
}
