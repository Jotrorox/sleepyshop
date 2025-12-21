package de.relaxogames.shop.gui;

import de.relaxogames.shop.model.Shop;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ShopInventoryHolder implements InventoryHolder {
    private final Shop shop;
    private final String title;

    public ShopInventoryHolder(Shop shop, String title) {
        this.shop = shop;
        this.title = title;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

    public Shop getShop() {
        return shop;
    }

    public String getTitle() {
        return title;
    }
}
