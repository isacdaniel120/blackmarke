package dev.gugli.blackmarket.gui;

import dev.gugli.blackmarket.BlackMarket;
import dev.gugli.blackmarket.util.GuiUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AddItemGUI {

    private final BlackMarket plugin;

    public AddItemGUI(BlackMarket plugin) {
        this.plugin = plugin;
    }

    // pendingItem = the item the admin is adding, pendingSlot = the GUI slot
    public void open(Player player, ItemStack pendingItem, int pendingSlot) {
        Inventory inv = GuiUtil.createInventory(45, "&0&l+ &4&lAdd Item to Market");

        ItemStack filler = GuiUtil.makeFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 45; i++) inv.setItem(i, filler);

        String symbol = plugin.getConfig().getString("market.currency-symbol", "$");

        // Show the item being added
        inv.setItem(4, pendingItem.clone());

        // Default values shown (admin can click to adjust)
        int defaultBuy = 100;
        int defaultSell = 50;
        int defaultStock = 5;

        // Buy Price controls
        inv.setItem(10, GuiUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&a&l+ Buy Price", "&7+100 per click", "&7Shift: +1000"));
        inv.setItem(11, GuiUtil.makeItem(Material.GOLD_NUGGET, "&e&lBuy Price",
                "&7Current: &a" + symbol + defaultBuy,
                "", "&7Price players pay to buy."));
        inv.setItem(12, GuiUtil.makeItem(Material.RED_STAINED_GLASS_PANE, "&c&l- Buy Price", "&7-100 per click", "&7Shift: -1000"));

        // Sell Price controls
        inv.setItem(19, GuiUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&a&l+ Sell Price", "&7+100 per click", "&7Shift: +1000"));
        inv.setItem(20, GuiUtil.makeItem(Material.GOLD_INGOT, "&6&lSell Price",
                "&7Current: &e" + symbol + defaultSell,
                "", "&7Price players get when selling."));
        inv.setItem(21, GuiUtil.makeItem(Material.RED_STAINED_GLASS_PANE, "&c&l- Sell Price", "&7-100 per click", "&7Shift: -1000"));

        // Stock
        inv.setItem(28, GuiUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&a&l+ Stock", "&7+1 per click", "&7Shift: +10"));
        inv.setItem(29, GuiUtil.makeItem(Material.BARREL, "&b&lMax Stock",
                "&7Current: &f" + defaultStock,
                "", "&7Available per market cycle."));
        inv.setItem(30, GuiUtil.makeItem(Material.RED_STAINED_GLASS_PANE, "&c&l- Stock", "&7-1 per click", "&7Shift: -10"));

        // Sellable toggle
        inv.setItem(24, GuiUtil.makeItem(Material.GRAY_DYE, "&f&lSellable: &cDisabled",
                "&7Toggle to allow players to",
                "&7sell this item back.",
                "", "&eClick to toggle"));

        // Confirm & Cancel
        inv.setItem(38, GuiUtil.makeItem(Material.LIME_WOOL, "&a&lAdd to Market", "&7Confirm and add this item."));
        inv.setItem(40, GuiUtil.makeItem(Material.ARROW, "&7← Cancel", "&7Return to item list without saving"));

        player.openInventory(inv);
    }
}
