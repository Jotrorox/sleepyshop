package com.jotrorox.shop.gui;

import com.jotrorox.shop.model.Shop;
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

    private static final Component PREFIX = Component.text("[SleepyShop] ", NamedTextColor.BLUE);

    public void openOwnerGui(Player player, Shop shop) {
        Inventory inv = createBaseGui(shop, 27, OWNER_GUI_TITLE);

        inv.setItem(11, createGuiItem(Material.GOLD_INGOT, "Price Settings", NamedTextColor.GOLD, "Set output and take amounts"));
        inv.setItem(13, createGuiItem(Material.CHEST, "Item Settings", NamedTextColor.AQUA, "Set sell and payment items"));
        inv.setItem(15, createGuiItem(Material.REPEATER, "Other Settings", NamedTextColor.GRAY, "Under construction..."));

        inv.setItem(26, createGuiItem(Material.BARRIER, "Disband Shop", NamedTextColor.RED));

        player.openInventory(inv);
    }

    public void openPriceGui(Player player, Shop shop) {
        Inventory inv = createBaseGui(shop, 36, PRICE_GUI_TITLE);

        // Take Amount (Price) - Row 1
        inv.setItem(10, createGuiItem(Material.RED_STAINED_GLASS_PANE, "-10 Price", NamedTextColor.RED));
        inv.setItem(11, createGuiItem(Material.PINK_STAINED_GLASS_PANE, "-1 Price", NamedTextColor.LIGHT_PURPLE));
        
        ItemStack priceStatus = createGuiItem(Material.GOLD_INGOT, "Current Price: " + shop.getTakeAmount(), NamedTextColor.GOLD);
        ItemMeta priceMeta = priceStatus.getItemMeta();
        priceMeta.lore(List.of(Component.text("This is what buyers pay per purchase", NamedTextColor.GRAY)));
        priceStatus.setItemMeta(priceMeta);
        inv.setItem(13, priceStatus);
        
        inv.setItem(15, createGuiItem(Material.LIME_STAINED_GLASS_PANE, "+1 Price", NamedTextColor.GREEN));
        inv.setItem(16, createGuiItem(Material.GREEN_STAINED_GLASS_PANE, "+10 Price", NamedTextColor.DARK_GREEN));

        // Output Amount - Row 2
        inv.setItem(19, createGuiItem(Material.RED_STAINED_GLASS_PANE, "-10 Amount", NamedTextColor.RED));
        inv.setItem(20, createGuiItem(Material.PINK_STAINED_GLASS_PANE, "-1 Amount", NamedTextColor.LIGHT_PURPLE));
        
        ItemStack amountStatus = createGuiItem(Material.CHEST, "Current Amount: " + shop.getOutputAmount(), NamedTextColor.AQUA);
        ItemMeta amountMeta = amountStatus.getItemMeta();
        amountMeta.lore(List.of(Component.text("This is what buyers receive per purchase", NamedTextColor.GRAY)));
        amountStatus.setItemMeta(amountMeta);
        inv.setItem(22, amountStatus);
        
        inv.setItem(24, createGuiItem(Material.LIME_STAINED_GLASS_PANE, "+1 Amount", NamedTextColor.GREEN));
        inv.setItem(25, createGuiItem(Material.GREEN_STAINED_GLASS_PANE, "+10 Amount", NamedTextColor.DARK_GREEN));

        inv.setItem(31, createBackItem());
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
        
        // Shop Name
        String currentName = shop.getShopName() != null ? shop.getShopName() : "None (Default)";
        inv.setItem(13, createGuiItem(Material.NAME_TAG, "Set Shop Name", NamedTextColor.GOLD, 
                "Current: " + currentName, "Click to change name in chat"));

        // Toggle Hologram
        Material holoMaterial = shop.isShowDisplay() ? Material.LIME_DYE : Material.GRAY_DYE;
        String holoStatus = shop.isShowDisplay() ? "Enabled" : "Disabled";
        inv.setItem(11, createGuiItem(holoMaterial, "Floating Text", NamedTextColor.YELLOW,
                "Status: " + holoStatus, "Click to toggle shop display"));

        // Toggle Stock Warning
        Material stockMaterial = shop.isShowStockMessage() ? Material.PAPER : Material.BARRIER;
        String stockStatus = shop.isShowStockMessage() ? "Enabled" : "Disabled";
        inv.setItem(15, createGuiItem(stockMaterial, "Out of Stock Warning", NamedTextColor.YELLOW,
                "Status: " + stockStatus, "Click to toggle 'OUT OF STOCK' message"));
        
        inv.setItem(18, createBackItem());
        player.openInventory(inv);
    }

    public void openBuyerGui(Player player, Shop shop) {
        Inventory inv = createBaseGui(shop, 9, BUYER_GUI_TITLE);

        if (shop.getSellItem() == null) {
            player.sendMessage(PREFIX.append(Component.text("This shop is not configured yet!", NamedTextColor.RED)));
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
