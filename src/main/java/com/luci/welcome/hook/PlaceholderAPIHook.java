package com.luci.welcome.hook;

import com.luci.welcome.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final Main plugin;

    public PlaceholderAPIHook(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "welcome";
    }

    @Override
    public @NotNull String getAuthor() {
        return "luci";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        switch (identifier.toLowerCase()) {
            case "player_name":
                return player.getName();
            case "player_displayname":
                return player.getDisplayName();
            case "player_world":
                return player.getWorld().getName();
            case "online_players":
                return String.valueOf(plugin.getServer().getOnlinePlayers().size());
            case "max_players":
                return String.valueOf(plugin.getServer().getMaxPlayers());
            case "plugin_version":
                return plugin.getDescription().getVersion();
            default:
                return null;
        }
    }

    public String parse(Player player, String text) {
        if (text == null || text.isEmpty()) return "";
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
    }

    private WelcomePlaceholderExpansion expansion;

    public WelcomePlaceholderExpansion getExpansion() {
        return expansion;
    }
}