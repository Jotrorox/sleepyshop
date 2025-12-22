package com.jotrorox.shop.gui;

import com.jotrorox.shop.model.Shop;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NonNull;

public record ShopInventoryHolder(Shop shop, String title) implements InventoryHolder {

    @Override
    public @NonNull Inventory getInventory() {
        return null;
    }
}
