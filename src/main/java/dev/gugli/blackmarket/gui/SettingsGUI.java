package dev.gugli.blackmarket.gui;

import dev.gugli.blackmarket.BlackMarket;
import dev.gugli.blackmarket.util.GuiUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SettingsGUI {

    private final BlackMarket plugin;

    public SettingsGUI(BlackMarket plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = GuiUtil.createInventory(45, "&0&l⚙ &4&lMarket Settings");

        ItemStack filler = GuiUtil.makeFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 45; i++) inv.setItem(i, filler);

        int interval = plugin.getConfig().getInt("market.interval-minutes", 60);
        int duration = plugin.getConfig().getInt("market.duration-minutes", 10);
        int maxItems = plugin.getConfig().getInt("market.max-items", 9);
        String symbol = plugin.getConfig().getString("market.currency-symbol", "$");

        // Interval
        inv.setItem(10, GuiUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&a&l+ Interval", "&7+5 min per click", "&7Shift: +30 min"));
        inv.setItem(11, GuiUtil.makeItem(Material.CLOCK, "&e&lSpawn Interval",
                "&7Current: &f" + interval + " min",
                "", "&7How often the market appears."));
        inv.setItem(12, GuiUtil.makeItem(Material.RED_STAINED_GLASS_PANE, "&c&l- Interval", "&7-5 min per click", "&7Shift: -30 min"));

        // Duration
        inv.setItem(19, GuiUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&a&l+ Duration", "&7+1 min per click", "&7Shift: +10 min"));
        inv.setItem(20, GuiUtil.makeItem(Material.SAND, "&6&lOpen Duration",
                "&7Current: &f" + duration + " min",
                "", "&7How long the market stays open."));
        inv.setItem(21, GuiUtil.makeItem(Material.RED_STAINED_GLASS_PANE, "&c&l- Duration", "&7-1 min per click", "&7Shift: -10 min"));

        // Max Items
        inv.setItem(28, GuiUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&a&l+ Max Items", "&7+1 per click"));
        inv.setItem(29, GuiUtil.makeItem(Material.CHEST, "&b&lMax Items",
                "&7Current: &f" + maxItems,
                "", "&7Max items shown in market GUI."));
        inv.setItem(30, GuiUtil.makeItem(Material.RED_STAINED_GLASS_PANE, "&c&l- Max Items", "&7-1 per click"));

        // Currency Symbol
        inv.setItem(24, GuiUtil.makeItem(Material.SUNFLOWER, "&6&lCurrency Symbol",
                "&7Current: &f" + symbol,
                "", "&eClick to change (type in chat)"));

        // Back button
        inv.setItem(40, GuiUtil.makeItem(Material.ARROW, "&7← Back", "&7Return to Admin Panel"));

        player.openInventory(inv);
    }
}
