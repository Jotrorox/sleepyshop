package de.relaxogames.sleepyshop.gui;

import de.relaxogames.sleepyshop.model.Shop;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ShopInventoryHolder implements InventoryHolder {

    private final Shop shop;
    private final String title;
    private int transactionCount = 1;

    public ShopInventoryHolder(Shop shop, String title) {
        this.shop = shop;
        this.title = title;
    }

    public Shop shop() {
        return shop;
    }

    public String title() {
        return title;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
