package ru.terracraft.welcome.core.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;
import ru.terracraft.welcome.WelcomePlugin;

/**
 * Обёртка над LuckPerms API.
 * Позволяет получить группу игрока и его primary group.
 */
public class LuckPermsIntegration {

    private final WelcomePlugin plugin;
    private LuckPerms luckPerms;

    public LuckPermsIntegration(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Подключение к LuckPerms.
     * @return true, если успешно
     */
    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            return false;
        }
        try {
            this.luckPerms = LuckPermsProvider.get();
            return luckPerms != null;
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка LuckPerms: " + e.getMessage());
            return false;
        }
    }

    public boolean isEnabled() {
        return luckPerms != null;
    }

    /**
     * Получить primary group игрока.
     */
    public String getPrimaryGroup(Player player) {
        if (luckPerms == null || player == null) return "";
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return "";
            String groupName = user.getPrimaryGroup();
            Group group = luckPerms.getGroupManager().getGroup(groupName);
            return group != null ? group.getName() : groupName;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Проверить, есть ли у игрока право.
     */
    public boolean hasPermission(Player player, String permission) {
        if (luckPerms == null || player == null) {
            return player != null && player.hasPermission(permission);
        }
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return player.hasPermission(permission);
            return user.getCachedData().getPermissionData()
                    .checkPermission(permission).asBoolean();
        } catch (Exception e) {
            return player.hasPermission(permission);
        }
    }
}