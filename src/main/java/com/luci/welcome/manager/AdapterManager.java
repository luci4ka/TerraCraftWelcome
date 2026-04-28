package com.luci.welcome.manager;

import com.luci.welcome.Main;
import com.luci.welcome.utils.LoggerUtil;
import org.bukkit.Bukkit;

public class AdapterManager {

    private final Main plugin;
    private final LoggerUtil logger;
    private final String serverVersion;

    public AdapterManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogUtil();
        this.serverVersion = Bukkit.getMinecraftVersion();

        logger.debug("Адаптер инициализирован для версии: " + serverVersion);
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public boolean isPaper() {
        try {
            Class.forName("io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public boolean isVersionAtLeast(int major, int minor) {
        String[] parts = serverVersion.split("\\.");
        if (parts.length < 2) return false;

        try {
            int serverMajor = Integer.parseInt(parts[0]);
            int serverMinor = Integer.parseInt(parts[1]);

            return serverMajor > major || (serverMajor == major && serverMinor >= minor);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getNMSVersion() {
        try {
            // Для Paper 1.21.4 использует Mojang mappings
            return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (Exception e) {
            return "unknown";
        }
    }
}