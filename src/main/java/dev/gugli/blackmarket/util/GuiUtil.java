package dev.gugli.blackmarket.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiUtil {

    public static ItemStack makeItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.displayName(color(name));
        if (lore.length > 0) {
            meta.lore(Arrays.stream(lore).map(GuiUtil::color).collect(Collectors.toList()));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeItemWithLore(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.displayName(color(name));
        meta.lore(lore.stream().map(GuiUtil::color).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeFiller(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    public static Component color(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    public static Inventory createInventory(int size, String title) {
        return Bukkit.createInventory(null, size, LegacyComponentSerializer.legacyAmpersand().deserialize(title));
    }

    public static String strip(String s) {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', s));
    }

    public static String formatTime(int seconds) {
        if (seconds <= 0) return "00:00";
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public static String formatNumber(double value) {
        if (value == Math.floor(value)) return String.valueOf((int) value);
        return String.format("%.2f", value);
    }
}
