package de.relaxogames.shop.model;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Shop {
    private final Location signLocation;
    private final Location chestLocation;
    private final UUID owner;
    private ItemStack item;
    private double price;
    private int amount;

    public Shop(Location signLocation, Location chestLocation, UUID owner) {
        this.signLocation = signLocation;
        this.chestLocation = chestLocation;
        this.owner = owner;
        this.price = 0;
        this.amount = 1;
    }

    public Location getSignLocation() {
        return signLocation;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    public UUID getOwner() {
        return owner;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
