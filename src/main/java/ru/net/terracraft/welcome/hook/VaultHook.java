package ru.net.terracraft.welcome.hook;

import ru.net.terracraft.welcome.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private final Main plugin;
    private Economy economy;

    public VaultHook(Main plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer()
                .getServicesManager()
                .getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isEnabled() {
        return economy != null;
    }

    public double getBalance(org.bukkit.entity.Player player) {
        if (economy != null && player != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }

    public boolean hasEnough(org.bukkit.entity.Player player, double amount) {
        if (economy != null && player != null) {
            return economy.has(player, amount);
        }
        return false;
    }

    public boolean withdraw(org.bukkit.entity.Player player, double amount) {
        if (economy != null && player != null) {
            return economy.withdrawPlayer(player, amount).transactionSuccess();
        }
        return false;
    }

    public boolean deposit(org.bukkit.entity.Player player, double amount) {
        if (economy != null && player != null) {
            return economy.depositPlayer(player, amount).transactionSuccess();
        }
        return false;
    }
}