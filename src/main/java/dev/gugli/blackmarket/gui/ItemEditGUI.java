package dev.gugli.blackmarket.gui;

import dev.gugli.blackmarket.BlackMarket;
import dev.gugli.blackmarket.model.MarketItem;
import dev.gugli.blackmarket.util.GuiUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemEditGUI {

    private final BlackMarket plugin;

    public ItemEditGUI(BlackMarket plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, MarketItem item) {
        Inventory inv = GuiUtil.createInventory(45, "&0&l✎ &4&lEdit Item");

        ItemStack filler = GuiUtil.makeFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 45; i++) inv.setItem(i, filler);

        String symbol = plugin.getConfig().getString("market.currency-symbol", "$");

        // Preview of the item in center
        inv.setItem(4, item.getItemStack().clone());

        // Buy Price controls
        inv.setItem(10, GuiUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&a&l+ Buy Price", "&7+100 per click", "&7Shift: +1000"));
        inv.setItem(11, GuiUtil.makeItem(Material.GOLD_NUGGET, "&e&lBuy Price",
                "&7Current: &a" + symbol + GuiUtil.formatNumber(item.getBuyPrice()),
                "", "&7Price players pay to buy this item."));
        inv.setItem(12, GuiUtil.makeItem(Material.RED_STAINED_GLASS_PANE, "&c&l- Buy Price", "&7-100 per click", "&7Shift: -1000"));

        // Sell Price controls
        inv.setItem(19, GuiUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&a&l+ Sell Price", "&7+100 per click", "&7Shift: +1000"));
        inv.setItem(20, GuiUtil.makeItem(Material.GOLD_INGOT, "&6&lSell Price",
                "&7Current: &e" + symbol + GuiUtil.formatNumber(item.getSellPrice()),
                "", "&7Price players get when selling."));
        inv.setItem(21, GuiUtil.makeItem(Material.RED_STAINED_GLASS_PANE, "&c&l- Sell Price", "&7-100 per click", "&7Shift: -1000"));

        // Stock controls
        inv.setItem(28, GuiUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&a&l+ Max Stock", "&7+1 per click", "&7Shift: +10"));
        inv.setItem(29, GuiUtil.makeItem(Material.BARREL, "&b&lMax Stock",
                "&7Current: &f" + item.getMaxStock(),
                "", "&7Amount available per market cycle."));
        inv.setItem(30, GuiUtil.makeItem(Material.RED_STAINED_GLASS_PANE, "&c&l- Max Stock", "&7-1 per click", "&7Shift: -10"));

        // Sellable toggle
        Material sellMat = item.isSellable() ? Material.LIME_DYE : Material.GRAY_DYE;
        String sellStatus = item.isSellable() ? "&aEnabled" : "&cDisabled";
        inv.setItem(24, GuiUtil.makeItem(sellMat, "&f&lSellable: " + sellStatus,
                "&7Toggle whether players can",
                "&7sell this item back.",
                "", "&eClick to toggle"));

        // Save & Back
        inv.setItem(38, GuiUtil.makeItem(Material.LIME_WOOL, "&a&lSave Changes", "&7Save and return to item list"));
        inv.setItem(40, GuiUtil.makeItem(Material.ARROW, "&7← Back", "&7Return without saving"));
        inv.setItem(42, GuiUtil.makeItem(Material.BARRIER, "&c&lRemove Item", "&7Permanently remove this item"));

        player.openInventory(inv);
    }
}
