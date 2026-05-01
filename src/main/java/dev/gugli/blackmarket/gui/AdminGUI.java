package dev.gugli.blackmarket.gui;

import dev.gugli.blackmarket.BlackMarket;
import dev.gugli.blackmarket.util.GuiUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AdminGUI {

    private final BlackMarket plugin;

    public AdminGUI(BlackMarket plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = GuiUtil.createInventory(45, "&0&l⚙ &4&lAdmin Panel &0&l⚙");

        // Fill background
        ItemStack filler = GuiUtil.makeFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 45; i++) inv.setItem(i, filler);

        boolean isOpen = plugin.getMarketManager().isOpen();
        String timeLeft = GuiUtil.formatTime(plugin.getMarketManager().getTimeRemainingSeconds());

        // Status indicator
        ItemStack statusItem = isOpen
                ? GuiUtil.makeItem(Material.GREEN_WOOL, "&a&lMARKET OPEN", "&7Time remaining: &c" + timeLeft, "", "&7Click to &cforce close")
                : GuiUtil.makeItem(Material.RED_WOOL, "&c&lMARKET CLOSED", "&7The market is currently closed.", "", "&7Click to &aforce open");
        inv.setItem(4, statusItem);

        // Manage Items
        ItemStack manageItems = GuiUtil.makeItem(Material.CHEST,
                "&e&lManage Items",
                "&7Add, edit or remove items",
                "&7from the market.",
                "",
                "&aClick to open"
        );
        inv.setItem(19, manageItems);

        // Market Settings
        ItemStack settingsItem = GuiUtil.makeItem(Material.COMPARATOR,
                "&b&lMarket Settings",
                "&7Configure timers, interval,",
                "&7max items and more.",
                "",
                "&aClick to open"
        );
        inv.setItem(21, settingsItem);

        // Set Location
        ItemStack locationItem = GuiUtil.makeItem(Material.ENDER_EYE,
                "&d&lSet Location",
                "&7Teleport command location.",
                "",
                plugin.getMarketManager().getMarketLocation() != null
                        ? "&7Current: &f" + formatLoc(plugin.getMarketManager().getMarketLocation())
                        : "&cNot set",
                "",
                "&aClick to set to your position"
        );
        inv.setItem(23, locationItem);

        // Restock
        ItemStack restockItem = GuiUtil.makeItem(Material.HOPPER,
                "&6&lForce Restock",
                "&7Restock all items to max stock.",
                "",
                "&aClick to restock"
        );
        inv.setItem(25, restockItem);

        // Announce
        ItemStack announceItem = GuiUtil.makeItem(Material.BELL,
                "&c&lAnnounce Market",
                "&7Broadcast the appearance message",
                "&7to all online players.",
                "",
                "&aClick to announce"
        );
        inv.setItem(31, announceItem);

        // Back button
        ItemStack back = GuiUtil.makeItem(Material.ARROW, "&7← Back to Market", "&7Return to the player view");
        inv.setItem(40, back);

        player.openInventory(inv);
    }

    private String formatLoc(org.bukkit.Location loc) {
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }
}
