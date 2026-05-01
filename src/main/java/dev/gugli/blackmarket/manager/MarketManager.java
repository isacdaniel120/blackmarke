package dev.gugli.blackmarket.manager;

import dev.gugli.blackmarket.BlackMarket;
import dev.gugli.blackmarket.model.MarketItem;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MarketManager {

    private final BlackMarket plugin;
    private final List<MarketItem> items = new ArrayList<>();
    private Location marketLocation;
    private boolean open = false;
    private int timeRemainingSeconds = 0;

    public MarketManager(BlackMarket plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        items.clear();
        items.addAll(plugin.getDataManager().loadItems());
        marketLocation = plugin.getDataManager().loadLocation();
    }

    public void openMarket() {
        plugin.getDataManager().restockAll();
        reload();
        open = true;
        timeRemainingSeconds = plugin.getConfig().getInt("market.duration-minutes", 10) * 60;
        broadcastAppear();
    }

    public void closeMarket() {
        open = false;
        timeRemainingSeconds = 0;
        broadcastDisappear();
    }

    private void broadcastAppear() {
        String msg = colorize(plugin.getConfig().getString("messages.prefix", "") + plugin.getConfig().getString("messages.appear", ""));
        String sound = plugin.getConfig().getString("sounds.appear", "ENTITY_ENDER_DRAGON_GROWL");
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(msg);
            try { player.playSound(player.getLocation(), Sound.valueOf(sound), 1f, 1f); } catch (Exception ignored) {}
        }
    }

    private void broadcastDisappear() {
        String msg = colorize(plugin.getConfig().getString("messages.prefix", "") + plugin.getConfig().getString("messages.disappear", ""));
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(msg);
        }
    }

    public boolean purchase(Player player, MarketItem marketItem, int quantity) {
        if (!plugin.getVaultHook().isEnabled()) {
            player.sendMessage(colorize("&cEconomy not available."));
            return false;
        }
        if (marketItem.isOutOfStock()) {
            player.sendMessage(colorize(msg("messages.out-of-stock")));
            return false;
        }
        double total = marketItem.getBuyPrice() * quantity;
        if (!plugin.getVaultHook().has(player, total)) {
            player.sendMessage(colorize(msg("messages.not-enough-money")));
            return false;
        }
        int actualQty = Math.min(quantity, marketItem.getStock());
        total = marketItem.getBuyPrice() * actualQty;
        plugin.getVaultHook().withdraw(player, total);
        marketItem.decreaseStock(actualQty);
        plugin.getDataManager().updateItem(marketItem);

        org.bukkit.inventory.ItemStack toGive = marketItem.getItemStack().clone();
        toGive.setAmount(actualQty);
        player.getInventory().addItem(toGive);

        String successMsg = colorize(msg("messages.purchase-success")
                .replace("{price}", plugin.getConfig().getString("market.currency-symbol", "$") + String.format("%.2f", total)));
        player.sendMessage(successMsg);

        String sound = plugin.getConfig().getString("sounds.purchase", "ENTITY_EXPERIENCE_ORB_PICKUP");
        try { player.playSound(player.getLocation(), Sound.valueOf(sound), 1f, 1f); } catch (Exception ignored) {}
        return true;
    }

    public boolean sell(Player player, org.bukkit.inventory.ItemStack item) {
        for (MarketItem mi : items) {
            if (mi.isSellable() && mi.getItemStack().getType() == item.getType()) {
                double price = mi.getSellPrice() * item.getAmount();
                plugin.getVaultHook().deposit(player, price);
                player.getInventory().removeItem(item);
                String msg = colorize(msg("messages.sell-success")
                        .replace("{price}", plugin.getConfig().getString("market.currency-symbol", "$") + String.format("%.2f", price)));
                player.sendMessage(msg);
                return true;
            }
        }
        player.sendMessage(colorize(msg("messages.cannot-sell")));
        return false;
    }

    public void addItem(MarketItem item) {
        items.add(item);
    }

    public void removeItem(MarketItem item) {
        items.remove(item);
        plugin.getDataManager().deleteItem(item.getId());
    }

    public void setLocation(Location location) {
        this.marketLocation = location;
        plugin.getDataManager().saveLocation(location);
    }

    public void tickDown() {
        if (timeRemainingSeconds > 0) timeRemainingSeconds--;
    }

    private String msg(String path) {
        return plugin.getConfig().getString(path, "");
    }

    public String colorize(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }

    public List<MarketItem> getItems() { return items; }
    public Location getMarketLocation() { return marketLocation; }
    public boolean isOpen() { return open; }
    public int getTimeRemainingSeconds() { return timeRemainingSeconds; }
}
