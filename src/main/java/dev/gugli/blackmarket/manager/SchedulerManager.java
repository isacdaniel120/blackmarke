package dev.gugli.blackmarket.manager;

import dev.gugli.blackmarket.BlackMarket;
import org.bukkit.scheduler.BukkitTask;

public class SchedulerManager {

    private final BlackMarket plugin;
    private BukkitTask marketTask;
    private BukkitTask countdownTask;
    private int ticksUntilOpen;

    public SchedulerManager(BlackMarket plugin) {
        this.plugin = plugin;
    }

    public void startScheduler() {
        resetInterval();
        startCountdownTask();
    }

    public void stopScheduler() {
        if (marketTask != null) marketTask.cancel();
        if (countdownTask != null) countdownTask.cancel();
    }

    private void resetInterval() {
        int intervalTicks = plugin.getConfig().getInt("market.interval-minutes", 60) * 60 * 20;
        ticksUntilOpen = intervalTicks;
        if (marketTask != null) marketTask.cancel();
        marketTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            ticksUntilOpen -= 20;
            if (ticksUntilOpen <= 0) {
                if (!plugin.getMarketManager().isOpen()) {
                    plugin.getMarketManager().openMarket();
                    scheduleClose();
                }
                resetInterval();
            }
        }, 20L, 20L);
    }

    private void scheduleClose() {
        int durationTicks = plugin.getConfig().getInt("market.duration-minutes", 10) * 60 * 20;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getMarketManager().isOpen()) {
                plugin.getMarketManager().closeMarket();
            }
        }, durationTicks);
    }

    private void startCountdownTask() {
        countdownTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (plugin.getMarketManager().isOpen()) {
                plugin.getMarketManager().tickDown();
            }
        }, 20L, 20L);
    }

    public void forceOpen() {
        if (!plugin.getMarketManager().isOpen()) {
            plugin.getMarketManager().openMarket();
            scheduleClose();
        }
    }

    public void forceClose() {
        if (plugin.getMarketManager().isOpen()) {
            plugin.getMarketManager().closeMarket();
        }
    }

    public int getTicksUntilOpen() { return ticksUntilOpen; }
}
