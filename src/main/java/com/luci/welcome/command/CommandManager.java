package com.luci.welcome.command;

import com.luci.welcome.config.ConfigHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.slf4j.Logger;

import java.util.List;

public class CommandManager {

    private final ConfigHandler config;
    private final Logger logger;

    public CommandManager(ConfigHandler config) {
        this.config = config;
        this.logger = config.getPlugin().getSLF4JLogger();

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
                            logger.info("Конфигурация плагина успешно перезагружена пользователем {}", sender.getName());
                        } else {
                            sender.sendMessage("§cИспользование: /wconfig reload");
                        }
                        return true;
                    }
                });
    }
}
