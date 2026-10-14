package ru.terracraft.welcome.core.integration;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.terracraft.welcome.WelcomePlugin;

/**
 * Подключение к Vault Economy + обёртка над Economy.
 * Всё в одном классе — без отдельного EconomyService.
 */
public class VaultIntegration {

    private final WelcomePlugin plugin;
    private Economy economy;

    public VaultIntegration(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    // ═══════════════ SETUP ═══════════════

    /**
     * Настраивает Vault.
     * @return true, если успешно
     */
    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        try {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer()
                    .getServicesManager()
                    .getRegistration(Economy.class);

            if (rsp == null) {
                plugin.getLogger().warning("Vault найден, но экономика не зарегистрирована");
                return false;
            }

            this.economy = rsp.getProvider();
            return economy != null;
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка Vault: " + e.getMessage());
            return false;
        }
    }

    // ═══════════════ STATUS ═══════════════

    public boolean isEnabled() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    // ═══════════════ ECONOMY API ═══════════════

    /**
     * Баланс игрока.
     */
    public double getBalance(Player player) {
        if (economy == null || player == null) return 0.0;
        try {
            return economy.getBalance(player);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Достаточно ли денег.
     */
    public boolean hasEnough(Player player, double amount) {
        if (economy == null || player == null) return false;
        try {
            return economy.has(player, amount);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Снять деньги.
     */
    public boolean withdraw(Player player, double amount) {
        if (economy == null || player == null) return false;
        try {
            return economy.withdrawPlayer(player, amount).transactionSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Начислить деньги.
     */
    public boolean deposit(Player player, double amount) {
        if (economy == null || player == null) return false;
        try {
            return economy.depositPlayer(player, amount).transactionSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Форматировать сумму (например, "$100.00").
     */
    public String format(double amount) {
        if (economy == null) return String.format("%.2f", amount);
        try {
            return economy.format(amount);
        } catch (Exception e) {
            return String.format("%.2f", amount);
        }
    }
}