package de.relaxogames.shop.gui;

import de.relaxogames.shop.model.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
    public static final String PRICE_GUI_TITLE = "Shop Price Settings";
    public static final String ITEMS_GUI_TITLE = "Shop Item Settings";
    public static final String OTHER_GUI_TITLE = "Other Settings";
    public static final String BUYER_GUI_TITLE = "Purchase Item";

    public void openOwnerGui(Player player, Shop shop) {
        Inventory inv = createBaseGui(shop, 27, OWNER_GUI_TITLE);

        inv.setItem(11, createGuiItem(Material.GOLD_INGOT, "Price Settings", NamedTextColor.GOLD, "Set output and take amounts"));
        inv.setItem(13, createGuiItem(Material.CHEST, "Item Settings", NamedTextColor.AQUA, "Set sell and payment items"));
        inv.setItem(15, createGuiItem(Material.REPEATER, "Other Settings", NamedTextColor.GRAY, "Under construction..."));

        inv.setItem(26, createGuiItem(Material.BARRIER, "Disband Shop", NamedTextColor.RED));

        player.openInventory(inv);
    }

    public void openPriceGui(Player player, Shop shop) {
        Inventory inv = createBaseGui(shop, 27, PRICE_GUI_TITLE);

        // Take Amount (-10, -1, current, +1, +10)
        inv.setItem(10, createGuiItem(Material.RED_TERRACOTTA, "-10 Take Amount", NamedTextColor.RED));
        inv.setItem(11, createGuiItem(Material.PINK_TERRACOTTA, "-1 Take Amount", NamedTextColor.LIGHT_PURPLE));
        inv.setItem(12, createGuiItem(Material.HOPPER, "Takes: " + shop.getTakeAmount(), NamedTextColor.YELLOW));
        inv.setItem(13, createGuiItem(Material.LIME_TERRACOTTA, "+1 Take Amount", NamedTextColor.GREEN));
        inv.setItem(14, createGuiItem(Material.GREEN_TERRACOTTA, "+10 Take Amount", NamedTextColor.DARK_GREEN));

        // Output Amount (-10, -1, current, +1, +10)
        inv.setItem(19, createGuiItem(Material.RED_TERRACOTTA, "-10 Output Amount", NamedTextColor.RED));
        inv.setItem(20, createGuiItem(Material.PINK_TERRACOTTA, "-1 Output Amount", NamedTextColor.LIGHT_PURPLE));
        inv.setItem(21, createGuiItem(Material.DISPENSER, "Outputs: " + shop.getOutputAmount(), NamedTextColor.YELLOW));
        inv.setItem(22, createGuiItem(Material.LIME_TERRACOTTA, "+1 Output Amount", NamedTextColor.GREEN));
        inv.setItem(23, createGuiItem(Material.GREEN_TERRACOTTA, "+10 Output Amount", NamedTextColor.DARK_GREEN));

        inv.setItem(18, createBackItem());
        player.openInventory(inv);
    }

    public void openItemsGui(Player player, Shop shop) {
        Inventory inv = createBaseGui(shop, 27, ITEMS_GUI_TITLE);

        // Sell Item
        ItemStack sellItemSlot = shop.getSellItem() != null ? shop.getSellItem().clone() : new ItemStack(Material.BARRIER);
        ItemMeta sellMeta = sellItemSlot.getItemMeta();
        sellMeta.displayName(Component.text("Item to Sell", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        List<Component> sellLore = new ArrayList<>();
        if (shop.getSellItem() == null) {
            sellLore.add(Component.text("No item set!", NamedTextColor.RED));
        } else {
            sellLore.add(Component.text("Currently: " + shop.getSellItem().getType().name(), NamedTextColor.GRAY));
        }
        sellLore.add(Component.text("Click with an item in cursor to set", NamedTextColor.DARK_GRAY));
        sellMeta.lore(sellLore);
        sellItemSlot.setItemMeta(sellMeta);
        inv.setItem(11, sellItemSlot);

        // Payment Item
        ItemStack payItemSlot = shop.getPaymentItem() != null ? shop.getPaymentItem().clone() : new ItemStack(Material.BARRIER);
        ItemMeta payMeta = payItemSlot.getItemMeta();
        payMeta.displayName(Component.text("Item to Accept as Payment", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        List<Component> payLore = new ArrayList<>();
        if (shop.getPaymentItem() == null) {
            payLore.add(Component.text("No item set!", NamedTextColor.RED));
        } else {
            payLore.add(Component.text("Currently: " + shop.getPaymentItem().getType().name(), NamedTextColor.GRAY));
        }
        payLore.add(Component.text("Click with an item in cursor to set", NamedTextColor.DARK_GRAY));
        payMeta.lore(payLore);
        payItemSlot.setItemMeta(payMeta);
        inv.setItem(15, payItemSlot);

        inv.setItem(18, createBackItem());
        player.openInventory(inv);
    }

    public void openOtherGui(Player player, Shop shop) {
        Inventory inv = createBaseGui(shop, 27, OTHER_GUI_TITLE);
        
        String currentName = shop.getShopName() != null ? shop.getShopName() : "None (Default)";
        inv.setItem(13, createGuiItem(Material.NAME_TAG, "Set Shop Name", NamedTextColor.GOLD, 
                "Current: " + currentName, "Click to change name in chat"));
        
        inv.setItem(18, createBackItem());
        player.openInventory(inv);
    }

    public void openBuyerGui(Player player, Shop shop) {
        Inventory inv = createBaseGui(shop, 9, BUYER_GUI_TITLE);

        if (shop.getSellItem() == null) {
            player.sendMessage(Component.text("This shop is not configured yet!", NamedTextColor.RED));
            player.closeInventory();
            return;
        }

        ItemStack displayItem = shop.getSellItem().clone();
        displayItem.setAmount(shop.getOutputAmount());
        ItemMeta meta = displayItem.getItemMeta();
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Price: ", NamedTextColor.GRAY).append(Component.text(shop.getTakeAmount() + " x " + shop.getPaymentItem().getType().name(), NamedTextColor.GOLD)));
        meta.lore(lore);
        displayItem.setItemMeta(meta);

        inv.setItem(4, displayItem);
        inv.setItem(8, createGuiItem(Material.EMERALD_BLOCK, "Confirm Purchase", NamedTextColor.GREEN));

        player.openInventory(inv);
    }

    private Inventory createBaseGui(Shop shop, int size, String title) {
        Inventory inv = Bukkit.createInventory(new ShopInventoryHolder(shop, title), size, Component.text(title));
        ItemStack filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < size; i++) {
            inv.setItem(i, filler);
        }
        return inv;
    }

    private ItemStack createBackItem() {
        return createGuiItem(Material.ARROW, "Back", NamedTextColor.RED);
    }

    private ItemStack createGuiItem(Material material, String name) {
        return createGuiItem(material, name, NamedTextColor.WHITE);
    }

    private ItemStack createGuiItem(Material material, String name, NamedTextColor color, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        if (loreLines.length > 0) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(Component.text(line, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
}
