package de.relaxogames.shop.gui;

import de.relaxogames.shop.model.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGuiProvider {

    public static final String OWNER_GUI_TITLE = "SleepyShop Configuration";
    public static final String BUYER_GUI_TITLE = "Purchase Item";

    public void openOwnerGui(Player player, Shop shop) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text(OWNER_GUI_TITLE));

        // Item to sell
        ItemStack itemSlot = shop.getItem() != null ? shop.getItem().clone() : new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = itemSlot.getItemMeta();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Click with an item in your cursor to set", NamedTextColor.GRAY));
        itemMeta.lore(lore);
        if (shop.getItem() == null) {
            itemMeta.displayName(Component.text("No Item Set", NamedTextColor.RED));
        }
        itemSlot.setItemMeta(itemMeta);
        inv.setItem(13, itemSlot);

        // Price setting (simple implementation: +1, +10, -1, -10)
        inv.setItem(10, createGuiItem(Material.RED_TERRACOTTA, "-10 Price"));
        inv.setItem(11, createGuiItem(Material.PINK_TERRACOTTA, "-1 Price"));
        
        ItemStack priceItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta priceMeta = priceItem.getItemMeta();
        priceMeta.displayName(Component.text("Price: " + shop.getPrice(), NamedTextColor.GOLD));
        priceItem.setItemMeta(priceMeta);
        inv.setItem(12, priceItem);

        inv.setItem(14, createGuiItem(Material.LIME_TERRACOTTA, "+1 Price"));
        inv.setItem(15, createGuiItem(Material.GREEN_TERRACOTTA, "+10 Price"));

        // Amount setting
        inv.setItem(21, createGuiItem(Material.PAPER, "Amount: " + shop.getAmount(), NamedTextColor.YELLOW));

        // Disband
        inv.setItem(26, createGuiItem(Material.BARRIER, "Disband Shop", NamedTextColor.RED));

        player.openInventory(inv);
    }

    public void openBuyerGui(Player player, Shop shop) {
        Inventory inv = Bukkit.createInventory(null, 9, Component.text(BUYER_GUI_TITLE));

        if (shop.getItem() == null) {
            player.sendMessage(Component.text("This shop is not configured yet!", NamedTextColor.RED));
            return;
        }

        ItemStack displayItem = shop.getItem().clone();
        displayItem.setAmount(shop.getAmount());
        ItemMeta meta = displayItem.getItemMeta();
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Price: " + shop.getPrice() + " Diamonds", NamedTextColor.GOLD));
        meta.lore(lore);
        displayItem.setItemMeta(meta);

        inv.setItem(4, displayItem);
        
        // Confirm Buy Button
        inv.setItem(8, createGuiItem(Material.EMERALD_BLOCK, "Confirm Purchase", NamedTextColor.GREEN));

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material material, String name) {
        return createGuiItem(material, name, NamedTextColor.WHITE);
    }

    private ItemStack createGuiItem(Material material, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color));
        item.setItemMeta(meta);
        return item;
    }
}
