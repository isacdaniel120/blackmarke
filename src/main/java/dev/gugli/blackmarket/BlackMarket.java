package dev.gugli.blackmarket;

import dev.gugli.blackmarket.commands.BlackMarketCommand;
import dev.gugli.blackmarket.commands.BlackMarketAdminCommand;
import dev.gugli.blackmarket.hooks.VaultHook;
import dev.gugli.blackmarket.listener.PlayerListener;
import dev.gugli.blackmarket.manager.DataManager;
import dev.gugli.blackmarket.manager.MarketManager;
import dev.gugli.blackmarket.manager.SchedulerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BlackMarket extends JavaPlugin {

    private static BlackMarket instance;
    private VaultHook vaultHook;
    private DataManager dataManager;
    private MarketManager marketManager;
    private SchedulerManager schedulerManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        vaultHook = new VaultHook(this);
        if (!vaultHook.setup()) {
            getLogger().warning("Vault not found! Economy features disabled.");
        }

        dataManager = new DataManager(this);
        dataManager.init();

        marketManager = new MarketManager(this);
        schedulerManager = new SchedulerManager(this);

        registerCommands();
        registerListeners();

        schedulerManager.startScheduler();

        getLogger().info("BlackMarket enabled. Good business.");
    }

    @Override
    public void onDisable() {
        if (schedulerManager != null) schedulerManager.stopScheduler();
        if (dataManager != null) dataManager.close();
        getLogger().info("BlackMarket disabled.");
    }

    private void registerCommands() {
        getCommand("blackmarket").setExecutor(new BlackMarketCommand(this));
        getCommand("blackmarket").setTabCompleter(new BlackMarketCommand(this));
        getCommand("bmadmin").setExecutor(new BlackMarketAdminCommand(this));
        getCommand("bmadmin").setTabCompleter(new BlackMarketAdminCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public static BlackMarket getInstance() { return instance; }
    public VaultHook getVaultHook() { return vaultHook; }
    public DataManager getDataManager() { return dataManager; }
    public MarketManager getMarketManager() { return marketManager; }
    public SchedulerManager getSchedulerManager() { return schedulerManager; }
}
