package luci.welcome;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {
    private final Main plugin;

    public CommandManager(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("wconfig").setExecutor(this);
        plugin.getCommand("welcome").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("wconfig")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                sender.sendMessage("§aКонфигурация успешно перезагружена!");
            } else {
                sender.sendMessage("§cИспользование: /wconfig reload");
            }
            return true;
        }

        if (label.equalsIgnoreCase("welcome")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("test")) {
                if (sender instanceof Player player) {
                    player.sendMessage("§bТест эффектов приветствия...");
                    new EventListener(plugin).playEffects(player);
                } else {
                    sender.sendMessage("§cТолько игрок может использовать эту команду!");
                }
            } else {
                sender.sendMessage("§cИспользование: /welcome test");
            }
            return true;
        }

        return false;
    }
}
