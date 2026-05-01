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

public class MarketGUI {

    private final BlackMarket plugin;

    public MarketGUI(BlackMarket plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        int timeLeft = plugin.getMarketManager().getTimeRemainingSeconds();
        String timeStr = GuiUtil.formatTime(timeLeft);
        String symbol = plugin.getConfig().getString("market.currency-symbol", "$");

        Inventory inv = GuiUtil.createInventory(54, "&0&l☠ &4&lBLACK MARKET &0&l☠ &8| &c" + timeStr);

        // Border decoration
        ItemStack borderItem = GuiUtil.makeFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, borderItem);
        for (int i = 45; i < 54; i++) inv.setItem(i, borderItem);
        inv.setItem(9, borderItem); inv.setItem(17, borderItem);
        inv.setItem(36, borderItem); inv.setItem(44, borderItem);

        List<MarketItem> items = plugin.getMarketManager().getItems();
        for (MarketItem mi : items) {
            int slot = mi.getSlot();
            if (slot < 10 || slot > 43) continue;

            ItemStack display = mi.getItemStack().clone();
            display.setAmount(1);

            List<String> lore = new ArrayList<>();
            lore.add("&8━━━━━━━━━━━━━━━━━━━━━━");
            if (!mi.isOutOfStock()) {
                lore.add("&7  Buy  &a" + symbol + GuiUtil.formatNumber(mi.getBuyPrice()));
                if (mi.isSellable()) {
                    lore.add("&7  Sell &e" + symbol + GuiUtil.formatNumber(mi.getSellPrice()));
                }
                lore.add("&8━━━━━━━━━━━━━━━━━━━━━━");
                lore.add("&7  Stock &f" + mi.getStock() + " &8/ &7" + mi.getMaxStock());
                lore.add("&8━━━━━━━━━━━━━━━━━━━━━━");
                lore.add("&7  &aLeft-click &7to buy &fx1");
                lore.add("&7  &eRight-click &7to buy &fx" + Math.min(16, mi.getStock()));
                if (mi.isSellable()) {
                    lore.add("&7  &6Shift-click &7to sell hand");
                }
            } else {
                lore.add("&8━━━━━━━━━━━━━━━━━━━━━━");
                lore.add("&4  OUT OF STOCK");
                lore.add("&8━━━━━━━━━━━━━━━━━━━━━━");
            }

            ItemStack rendered = GuiUtil.makeItemWithLore(display.getType(), getItemName(display), lore);
            copyMeta(display, rendered);
            inv.setItem(slot, rendered);
        }

        // Info item bottom bar
        ItemStack infoItem = GuiUtil.makeItem(Material.REDSTONE_TORCH,
                "&4&l⏳ Time Remaining",
                "&7The market closes in &c" + timeStr,
                "",
                "&8Acquire rare goods before time runs out."
        );
        inv.setItem(49, infoItem);

        if (player.hasPermission("blackmarket.admin")) {
            ItemStack adminItem = GuiUtil.makeItem(Material.COMMAND_BLOCK,
                    "&c&lAdmin Panel",
                    "&7Click to manage the market"
            );
            inv.setItem(53, adminItem);
        }

        String sound = plugin.getConfig().getString("sounds.open-gui", "BLOCK_CHEST_OPEN");
        try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(sound), 1f, 0.7f); } catch (Exception ignored) {}

        player.openInventory(inv);
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().serialize(item.getItemMeta().displayName());
        }
        return formatMaterialName(item.getType().name());
    }

    private String formatMaterialName(String name) {
        String[] words = name.split("_");
        StringBuilder sb = new StringBuilder("&f");
        for (String word : words) {
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }

    private void copyMeta(ItemStack from, ItemStack to) {
        if (!from.hasItemMeta()) return;
        var fromMeta = from.getItemMeta();
        var toMeta = to.getItemMeta();
        if (fromMeta == null || toMeta == null) return;
        if (fromMeta.hasEnchants()) {
            fromMeta.getEnchants().forEach((ench, lvl) -> toMeta.addEnchant(ench, lvl, true));
        }
        toMeta.setCustomModelData(fromMeta.hasCustomModelData() ? fromMeta.getCustomModelData() : 0);
        to.setItemMeta(toMeta);
    }
}
