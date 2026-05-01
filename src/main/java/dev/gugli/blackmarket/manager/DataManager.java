package dev.gugli.blackmarket.manager;

import dev.gugli.blackmarket.BlackMarket;
import dev.gugli.blackmarket.model.MarketItem;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DataManager {

    private final BlackMarket plugin;
    private Connection connection;

    public DataManager(BlackMarket plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "blackmarket.db");
            if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to SQLite: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS market_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_data TEXT NOT NULL,
                    buy_price REAL NOT NULL DEFAULT 0,
                    sell_price REAL NOT NULL DEFAULT 0,
                    stock INTEGER NOT NULL DEFAULT 1,
                    max_stock INTEGER NOT NULL DEFAULT 1,
                    sellable INTEGER NOT NULL DEFAULT 0,
                    slot INTEGER NOT NULL DEFAULT 0
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS market_settings (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
            """);
        }
    }

    public List<MarketItem> loadItems() {
        List<MarketItem> items = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM market_items ORDER BY slot ASC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                ItemStack item = deserializeItem(rs.getString("item_data"));
                if (item == null) continue;
                double buy = rs.getDouble("buy_price");
                double sell = rs.getDouble("sell_price");
                int stock = rs.getInt("stock");
                int maxStock = rs.getInt("max_stock");
                boolean sellable = rs.getInt("sellable") == 1;
                int slot = rs.getInt("slot");
                items.add(new MarketItem(id, item, buy, sell, stock, maxStock, sellable, slot));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load items: " + e.getMessage());
        }
        return items;
    }

    public int saveItem(ItemStack item, double buyPrice, double sellPrice, int stock, int maxStock, boolean sellable, int slot) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO market_items (item_data, buy_price, sell_price, stock, max_stock, sellable, slot) VALUES (?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, serializeItem(item));
            ps.setDouble(2, buyPrice);
            ps.setDouble(3, sellPrice);
            ps.setInt(4, stock);
            ps.setInt(5, maxStock);
            ps.setInt(6, sellable ? 1 : 0);
            ps.setInt(7, slot);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save item: " + e.getMessage());
        }
        return -1;
    }

    public void updateItem(MarketItem marketItem) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE market_items SET item_data=?, buy_price=?, sell_price=?, stock=?, max_stock=?, sellable=?, slot=? WHERE id=?")) {
            ps.setString(1, serializeItem(marketItem.getItemStack()));
            ps.setDouble(2, marketItem.getBuyPrice());
            ps.setDouble(3, marketItem.getSellPrice());
            ps.setInt(4, marketItem.getStock());
            ps.setInt(5, marketItem.getMaxStock());
            ps.setInt(6, marketItem.isSellable() ? 1 : 0);
            ps.setInt(7, marketItem.getSlot());
            ps.setInt(8, marketItem.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update item: " + e.getMessage());
        }
    }

    public void deleteItem(int id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM market_items WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete item: " + e.getMessage());
        }
    }

    public void restockAll() {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE market_items SET stock = max_stock")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to restock: " + e.getMessage());
        }
    }

    public void saveSetting(String key, String value) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO market_settings (key, value) VALUES (?,?)")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save setting: " + e.getMessage());
        }
    }

    public String getSetting(String key) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT value FROM market_settings WHERE key=?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("value");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get setting: " + e.getMessage());
        }
        return null;
    }

    public void saveLocation(Location location) {
        if (location == null || location.getWorld() == null) return;
        String data = location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
        saveSetting("location", data);
    }

    public Location loadLocation() {
        String data = getSetting("location");
        if (data == null) return null;
        try {
            String[] parts = data.split(",");
            var world = plugin.getServer().getWorld(parts[0]);
            if (world == null) return null;
            return new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
        } catch (Exception e) {
            return null;
        }
    }

    private String serializeItem(ItemStack item) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeObject(item);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    private ItemStack deserializeItem(String data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            return (ItemStack) bois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close DB: " + e.getMessage());
        }
    }
}
