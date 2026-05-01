package dev.gugli.blackmarket.gui;

import dev.gugli.blackmarket.BlackMarket;
import dev.gugli.blackmarket.model.MarketItem;
import dev.gugli.blackmarket.util.GuiUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemListGUI {

    private final BlackMarket plugin;

    public ItemListGUI(BlackMarket plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = GuiUtil.createInventory(54, "&0&l☠ &4&lItem Manager &0&l☠");

        ItemStack filler = GuiUtil.makeFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, filler);
        for (int i = 45; i < 54; i++) inv.setItem(i, filler);
        inv.setItem(9, filler); inv.setItem(17, filler);
        inv.setItem(36, filler); inv.setItem(44, filler);

        String symbol = plugin.getConfig().getString("market.currency-symbol", "$");
        List<MarketItem> items = plugin.getMarketManager().getItems();

        for (MarketItem mi : items) {
            int slot = mi.getSlot();
            if (slot < 10 || slot > 43) continue;

            ItemStack display = mi.getItemStack().clone();
            List<String> lore = new ArrayList<>();
            lore.add("&8━━━━━━━━━━━━━━━━━━━━━━");
            lore.add("&7 Buy Price &a" + symbol + GuiUtil.formatNumber(mi.getBuyPrice()));
            lore.add("&7 Sell Price &e" + symbol + GuiUtil.formatNumber(mi.getSellPrice()));
            lore.add("&7 Stock &f" + mi.getStock() + " &8/ &7" + mi.getMaxStock());
            lore.add("&7 Sellable &f" + (mi.isSellable() ? "&aYes" : "&cNo"));
            lore.add("&8━━━━━━━━━━━━━━━━━━━━━━");
            lore.add("&aLeft-click &7to edit");
            lore.add("&cRight-click &7to remove");

            ItemStack rendered = GuiUtil.makeItemWithLore(display.getType(), getItemName(display), lore);
            inv.setItem(slot, rendered);
        }

        // Add new item button — fill empty slots in grid
        for (int row = 1; row <= 3; row++) {
            for (int col = 1; col <= 7; col++) {
                int slot = row * 9 + col;
                if (inv.getItem(slot) == null || inv.getItem(slot).getType() == Material.AIR) {
                    if (slot >= 10 && slot <= 43) {
                        ItemStack addItem = GuiUtil.makeItem(Material.LIME_STAINED_GLASS_PANE,
                                "&a&l+ Add Item",
                                "&7Place an item in this slot.",
                                "&7Left-click with item in hand."
                        );
                        inv.setItem(slot, addItem);
                    }
                }
            }
        }

        // Back button
        inv.setItem(45, GuiUtil.makeItem(Material.ARROW, "&7← Back", "&7Return to Admin Panel"));
        inv.setItem(49, GuiUtil.makeItem(Material.BOOK, "&e&lSlot Help",
                "&7Left-click a &a+ slot &7while",
                "&7holding an item to add it.",
                "&7Each slot in the grid",
                "&7maps to the market GUI."));

        player.openInventory(inv);
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().serialize(item.getItemMeta().displayName());
        }
        String[] words = item.getType().name().split("_");
        StringBuilder sb = new StringBuilder("&f");
        for (String word : words) sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
        return sb.toString().trim();
    }
}
