package dev.gugli.blackmarket.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultHook {

    private final JavaPlugin plugin;
    private Economy economy;
    private boolean enabled = false;

    public VaultHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        enabled = economy != null;
        return enabled;
    }

    public boolean has(Player player, double amount) {
        return enabled && economy.has(player, amount);
    }

    public void withdraw(Player player, double amount) {
        if (enabled) economy.withdrawPlayer(player, amount);
    }

    public void deposit(Player player, double amount) {
        if (enabled) economy.depositPlayer(player, amount);
    }

    public double getBalance(Player player) {
        return enabled ? economy.getBalance(player) : 0;
    }

    public boolean isEnabled() { return enabled; }
}
