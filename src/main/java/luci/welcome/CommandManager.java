package luci.welcome;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CommandManager implements CommandExecutor {
    private final Main plugin;

    public CommandManager(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("wconfig")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("welcome")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String @NotNull [] args) {
        if (label.equalsIgnoreCase("wconfig")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                sender.sendMessage("§aКонфигурация успешно перезагружена!");
            } else {
                sender.sendMessage("§cИспользование: /wconfig reload");
            }
            return true;
        }

        if (label.equalsIgnoreCase("welcome") && args.length == 1) {
            if (args[0].equalsIgnoreCase("toggle_sound")) {
                boolean soundEnabled = plugin.getConfig().getBoolean("sound.enable_sound");
                plugin.getConfig().set("sound.enable_sound", !soundEnabled);
                plugin.saveConfig();
                sender.sendMessage("§aЗвук " + (soundEnabled ? "выключен" : "включён"));
            } else if (args[0].equalsIgnoreCase("toggle_particles")) {
                boolean particlesEnabled = plugin.getConfig().getBoolean("particle_effect.enable_particles");
                plugin.getConfig().set("particle_effect.enable_particles", !particlesEnabled);
                plugin.saveConfig();
                sender.sendMessage("§aЭффекты частиц " + (particlesEnabled ? "выключены" : "включены"));
            } else {
                sender.sendMessage("§cИспользование: /welcome toggle_sound или /welcome toggle_particles");
            }
            return true;
        }

        return false;
    }
}
