package dev.gugli.blackmarket.listener;

import dev.gugli.blackmarket.BlackMarket;
import dev.gugli.blackmarket.gui.*;
import dev.gugli.blackmarket.model.MarketItem;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final BlackMarket plugin;
    private final MarketGUI marketGUI;
    private final AdminGUI adminGUI;
    private final ItemListGUI itemListGUI;
    private final ItemEditGUI itemEditGUI;
    private final SettingsGUI settingsGUI;
    private final AddItemGUI addItemGUI;

    // Pending states per player
    private final Map<UUID, MarketItem> editingItem = new HashMap<>();
    private final Map<UUID, double[]> pendingPrices = new HashMap<>(); // [buyPrice, sellPrice, stock, maxStock, sellable]
    private final Map<UUID, ItemStack> pendingAddItem = new HashMap<>();
    private final Map<UUID, Integer> pendingAddSlot = new HashMap<>();
    private final Map<UUID, double[]> pendingAddPrices = new HashMap<>(); // [buy, sell, stock, sellable]
    private final Map<UUID, String> awaitingInput = new HashMap<>();

    public PlayerListener(BlackMarket plugin) {
        this.plugin = plugin;
        this.marketGUI = new MarketGUI(plugin);
        this.adminGUI = new AdminGUI(plugin);
        this.itemListGUI = new ItemListGUI(plugin);
        this.itemEditGUI = new ItemEditGUI(plugin);
        this.settingsGUI = new SettingsGUI(plugin);
        this.addItemGUI = new AddItemGUI(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        String title = getTitle(event);

        if (title.contains("BLACK MARKET") && !title.contains("Admin") && !title.contains("Item Manager")) {
            handleMarketClick(event, player, title);
        } else if (title.contains("Admin Panel")) {
            handleAdminClick(event, player);
        } else if (title.contains("Item Manager")) {
            handleItemListClick(event, player);
        } else if (title.contains("Edit Item")) {
            handleItemEditClick(event, player);
        } else if (title.contains("Market Settings")) {
            handleSettingsClick(event, player);
        } else if (title.contains("Add Item to Market")) {
            handleAddItemClick(event, player);
        }
    }

    private void handleMarketClick(InventoryClickEvent event, Player player, String title) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        int slot = event.getRawSlot();

        // Admin panel button
        if (slot == 53 && player.hasPermission("blackmarket.admin")) {
            adminGUI.open(player);
            return;
        }

        // Check if it's a market item slot
        if (slot < 10 || slot > 43) return;
        MarketItem mi = getItemBySlot(slot);
        if (mi == null) return;

        if (!plugin.getMarketManager().isOpen()) {
            player.sendMessage(plugin.getMarketManager().colorize(plugin.getConfig().getString("messages.prefix", "") + plugin.getConfig().getString("messages.market-closed", "")));
            return;
        }

        if (mi.isOutOfStock()) {
            player.sendMessage(plugin.getMarketManager().colorize(plugin.getConfig().getString("messages.prefix", "") + plugin.getConfig().getString("messages.out-of-stock", "")));
            return;
        }

        ClickType click = event.getClick();
        if (click == ClickType.LEFT) {
            plugin.getMarketManager().purchase(player, mi, 1);
            marketGUI.open(player);
        } else if (click == ClickType.RIGHT) {
            int qty = Math.min(16, mi.getStock());
            plugin.getMarketManager().purchase(player, mi, qty);
            marketGUI.open(player);
        } else if (click == ClickType.SHIFT_LEFT && mi.isSellable()) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.AIR) {
                player.sendMessage(plugin.getMarketManager().colorize("&cHold an item in your hand to sell."));
                return;
            }
            plugin.getMarketManager().sell(player, hand);
            marketGUI.open(player);
        }
    }

    private void handleAdminClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        int slot = event.getRawSlot();

        switch (slot) {
            case 4 -> { // toggle open/close
                if (plugin.getMarketManager().isOpen()) plugin.getSchedulerManager().forceClose();
                else plugin.getSchedulerManager().forceOpen();
                adminGUI.open(player);
            }
            case 19 -> itemListGUI.open(player);  // Manage Items
            case 21 -> settingsGUI.open(player);  // Settings
            case 23 -> { // Set location
                plugin.getMarketManager().setLocation(player.getLocation());
                player.sendMessage(plugin.getMarketManager().colorize(plugin.getConfig().getString("messages.prefix", "") + plugin.getConfig().getString("messages.location-set", "")));
                adminGUI.open(player);
            }
            case 25 -> { // Restock
                plugin.getDataManager().restockAll();
                plugin.getMarketManager().reload();
                player.sendMessage(plugin.getMarketManager().colorize("&aAll items restocked!"));
                adminGUI.open(player);
            }
            case 31 -> { // Announce
                String msg = plugin.getMarketManager().colorize(plugin.getConfig().getString("messages.prefix", "") + plugin.getConfig().getString("messages.appear", ""));
                for (Player p : plugin.getServer().getOnlinePlayers()) p.sendMessage(msg);
                player.sendMessage(plugin.getMarketManager().colorize("&aAnnouncement sent!"));
            }
            case 40 -> marketGUI.open(player);  // Back
        }
    }

    private void handleItemListClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();

        // Allow taking items from player inventory when adding
        if (slot >= 54) return; // player's inv, let it pass for drag-drop? Actually cancel all
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        Material mat = event.getCurrentItem().getType();

        // Back button
        if (slot == 45) { adminGUI.open(player); return; }

        // Empty / Add Item slot
        if (mat == Material.LIME_STAINED_GLASS_PANE && slot >= 10 && slot <= 43) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.AIR) {
                player.sendMessage(plugin.getMarketManager().colorize("&cHold an item to add it to the market."));
                return;
            }
            pendingAddItem.put(player.getUniqueId(), hand.clone());
            pendingAddSlot.put(player.getUniqueId(), slot);
            pendingAddPrices.put(player.getUniqueId(), new double[]{100, 50, 5, 0}); // buy, sell, stock, sellable
            addItemGUI.open(player, hand, slot);
            return;
        }

        // Existing item slot - edit or remove
        if (slot >= 10 && slot <= 43) {
            MarketItem mi = getItemBySlot(slot);
            if (mi == null) return;
            if (event.getClick() == ClickType.LEFT) {
                editingItem.put(player.getUniqueId(), mi);
                pendingPrices.put(player.getUniqueId(), new double[]{mi.getBuyPrice(), mi.getSellPrice(), mi.getStock(), mi.getMaxStock(), mi.isSellable() ? 1 : 0});
                itemEditGUI.open(player, mi);
            } else if (event.getClick() == ClickType.RIGHT) {
                plugin.getMarketManager().removeItem(mi);
                player.sendMessage(plugin.getMarketManager().colorize("&aItem removed from market."));
                itemListGUI.open(player);
            }
        }
    }

    private void handleItemEditClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        int slot = event.getRawSlot();
        MarketItem mi = editingItem.get(player.getUniqueId());
        if (mi == null) return;
        double[] prices = pendingPrices.getOrDefault(player.getUniqueId(), new double[]{mi.getBuyPrice(), mi.getSellPrice(), mi.getStock(), mi.getMaxStock(), mi.isSellable() ? 1 : 0});
        boolean shift = event.isShiftClick();

        switch (slot) {
            case 10 -> prices[0] += shift ? 1000 : 100;  // +buy
            case 12 -> prices[0] = Math.max(0, prices[0] - (shift ? 1000 : 100)); // -buy
            case 19 -> prices[1] += shift ? 1000 : 100;  // +sell
            case 21 -> prices[1] = Math.max(0, prices[1] - (shift ? 1000 : 100)); // -sell
            case 28 -> prices[3] += shift ? 10 : 1;  // +stock
            case 30 -> prices[3] = Math.max(1, prices[3] - (shift ? 10 : 1)); // -stock
            case 24 -> prices[4] = prices[4] == 1 ? 0 : 1; // toggle sellable
            case 38 -> { // Save
                mi.setBuyPrice(prices[0]);
                mi.setSellPrice(prices[1]);
                mi.setMaxStock((int) prices[3]);
                mi.setStock(Math.min(mi.getStock(), (int) prices[3]));
                mi.setSellable(prices[4] == 1);
                plugin.getDataManager().updateItem(mi);
                editingItem.remove(player.getUniqueId());
                pendingPrices.remove(player.getUniqueId());
                player.sendMessage(plugin.getMarketManager().colorize("&aItem updated!"));
                itemListGUI.open(player);
                return;
            }
            case 40 -> { // Back without save
                editingItem.remove(player.getUniqueId());
                pendingPrices.remove(player.getUniqueId());
                itemListGUI.open(player);
                return;
            }
            case 42 -> { // Remove
                plugin.getMarketManager().removeItem(mi);
                editingItem.remove(player.getUniqueId());
                pendingPrices.remove(player.getUniqueId());
                player.sendMessage(plugin.getMarketManager().colorize("&aItem removed!"));
                itemListGUI.open(player);
                return;
            }
            default -> { return; }
        }

        pendingPrices.put(player.getUniqueId(), prices);
        // Re-open with updated values applied to mi temporarily
        MarketItem tempMi = new MarketItem(mi.getId(), mi.getItemStack(), prices[0], prices[1], (int) prices[2], (int) prices[3], prices[4] == 1, mi.getSlot());
        itemEditGUI.open(player, tempMi);
        editingItem.put(player.getUniqueId(), mi); // keep original
    }

    private void handleSettingsClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        int slot = event.getRawSlot();
        boolean shift = event.isShiftClick();

        int interval = plugin.getConfig().getInt("market.interval-minutes", 60);
        int duration = plugin.getConfig().getInt("market.duration-minutes", 10);
        int maxItems = plugin.getConfig().getInt("market.max-items", 9);

        switch (slot) {
            case 10 -> plugin.getConfig().set("market.interval-minutes", interval + (shift ? 30 : 5));
            case 12 -> plugin.getConfig().set("market.interval-minutes", Math.max(1, interval - (shift ? 30 : 5)));
            case 19 -> plugin.getConfig().set("market.duration-minutes", duration + (shift ? 10 : 1));
            case 21 -> plugin.getConfig().set("market.duration-minutes", Math.max(1, duration - (shift ? 10 : 1)));
            case 28 -> plugin.getConfig().set("market.max-items", maxItems + 1);
            case 30 -> plugin.getConfig().set("market.max-items", Math.max(1, maxItems - 1));
            case 24 -> {
                player.closeInventory();
                player.sendMessage(plugin.getMarketManager().colorize("&eType the new currency symbol in chat:"));
                awaitingInput.put(player.getUniqueId(), "currency");
                return;
            }
            case 40 -> { adminGUI.open(player); return; }
            default -> { return; }
        }

        plugin.saveConfig();
        settingsGUI.open(player);
    }

    private void handleAddItemClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        int slot = event.getRawSlot();
        boolean shift = event.isShiftClick();

        double[] prices = pendingAddPrices.getOrDefault(player.getUniqueId(), new double[]{100, 50, 5, 0});
        ItemStack pending = pendingAddItem.get(player.getUniqueId());
        int pendingSlot = pendingAddSlot.getOrDefault(player.getUniqueId(), 10);

        switch (slot) {
            case 10 -> prices[0] += shift ? 1000 : 100;
            case 12 -> prices[0] = Math.max(0, prices[0] - (shift ? 1000 : 100));
            case 19 -> prices[1] += shift ? 1000 : 100;
            case 21 -> prices[1] = Math.max(0, prices[1] - (shift ? 1000 : 100));
            case 28 -> prices[2] += shift ? 10 : 1;
            case 30 -> prices[2] = Math.max(1, prices[2] - (shift ? 10 : 1));
            case 24 -> prices[3] = prices[3] == 1 ? 0 : 1;
            case 38 -> { // Confirm add
                if (pending == null) { itemListGUI.open(player); return; }
                ItemStack toSave = pending.clone();
                toSave.setAmount(1);
                int id = plugin.getDataManager().saveItem(toSave, prices[0], prices[1], (int) prices[2], (int) prices[2], prices[3] == 1, pendingSlot);
                if (id > 0) {
                    MarketItem newMi = new MarketItem(id, toSave, prices[0], prices[1], (int) prices[2], (int) prices[2], prices[3] == 1, pendingSlot);
                    plugin.getMarketManager().addItem(newMi);
                    player.sendMessage(plugin.getMarketManager().colorize("&aItem added to the market!"));
                }
                pendingAddItem.remove(player.getUniqueId());
                pendingAddSlot.remove(player.getUniqueId());
                pendingAddPrices.remove(player.getUniqueId());
                itemListGUI.open(player);
                return;
            }
            case 40 -> { // Cancel
                pendingAddItem.remove(player.getUniqueId());
                pendingAddSlot.remove(player.getUniqueId());
                pendingAddPrices.remove(player.getUniqueId());
                itemListGUI.open(player);
                return;
            }
            default -> { return; }
        }

        pendingAddPrices.put(player.getUniqueId(), prices);
        addItemGUI.open(player, pending, pendingSlot);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        // Cleanup if they close mid-edit
        UUID uid = player.getUniqueId();
        // We intentionally leave pending states so reopening works
    }

    // Chat listener for currency symbol input
    @EventHandler
    public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String input = awaitingInput.get(player.getUniqueId());
        if (input == null) return;
        event.setCancelled(true);
        awaitingInput.remove(player.getUniqueId());
        if (input.equals("currency")) {
            String symbol = event.getMessage().trim();
            plugin.getConfig().set("market.currency-symbol", symbol);
            plugin.saveConfig();
            player.sendMessage(plugin.getMarketManager().colorize("&aCurrency symbol set to: &f" + symbol));
            plugin.getServer().getScheduler().runTask(plugin, () -> settingsGUI.open(player));
        }
    }

    private MarketItem getItemBySlot(int slot) {
        for (MarketItem mi : plugin.getMarketManager().getItems()) {
            if (mi.getSlot() == slot) return mi;
        }
        return null;
    }

    private String getTitle(InventoryClickEvent event) {
        try {
            return LegacyComponentSerializer.legacyAmpersand()
                    .serialize(event.getView().title());
        } catch (Exception e) {
            return "";
        }
    }
}
