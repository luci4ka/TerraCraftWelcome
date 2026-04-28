package com.luci.welcome.hook;

import com.luci.welcome.Main;
import com.luci.welcome.utils.LoggerUtil;

public class HookManager {

    private final Main plugin;
    private final LoggerUtil logger;
    private PlaceholderAPIHook papiHook;
    private VaultHook vaultHook;

    public HookManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogUtil();
    }

    public void loadHooks() {
        // PlaceholderAPI
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiHook = new PlaceholderAPIHook(plugin);
            papiHook.register();
            logger.info("✓ PlaceholderAPI хук загружен");
        } else {
            logger.warn("✗ PlaceholderAPI не найден");
        }

        // Vault
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            vaultHook = new VaultHook(plugin);
            vaultHook.setupEconomy();
            logger.info("✓ Vault хук загружен");
        } else {
            logger.warn("✗ Vault не найден");
        }
    }

    public void unloadHooks() {
        if (papiHook != null) papiHook.unregister();
        logger.debug("Все хуки выгружены");
    }

    public boolean isPapiEnabled() { return papiHook != null; }
    public boolean isVaultEnabled() { return vaultHook != null; }
    public PlaceholderAPIHook getPapiHook() { return papiHook; }
    public VaultHook getVaultHook() { return vaultHook; }
}