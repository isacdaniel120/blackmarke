package dev.gugli.blackmarket.model;

import org.bukkit.inventory.ItemStack;

public class MarketItem {

    private final int id;
    private ItemStack itemStack;
    private double buyPrice;
    private double sellPrice;
    private int stock;
    private int maxStock;
    private boolean sellable;
    private int slot;

    public MarketItem(int id, ItemStack itemStack, double buyPrice, double sellPrice, int stock, int maxStock, boolean sellable, int slot) {
        this.id = id;
        this.itemStack = itemStack;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stock = stock;
        this.maxStock = maxStock;
        this.sellable = sellable;
        this.slot = slot;
    }

    public int getId() { return id; }
    public ItemStack getItemStack() { return itemStack; }
    public void setItemStack(ItemStack itemStack) { this.itemStack = itemStack; }
    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }
    public double getSellPrice() { return sellPrice; }
    public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getMaxStock() { return maxStock; }
    public void setMaxStock(int maxStock) { this.maxStock = maxStock; }
    public boolean isSellable() { return sellable; }
    public void setSellable(boolean sellable) { this.sellable = sellable; }
    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }

    public void decreaseStock(int amount) {
        this.stock = Math.max(0, this.stock - amount);
    }

    public boolean isOutOfStock() {
        return stock <= 0;
    }
}
