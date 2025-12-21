package de.relaxogames.shop.listener;

import de.relaxogames.shop.gui.ShopGuiProvider;
import de.relaxogames.shop.manager.ShopManager;
import de.relaxogames.shop.model.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopListener implements Listener {
    private final ShopManager manager;
    private final ShopGuiProvider guiProvider;
    private final Map<UUID, Location> openShopEditors = new HashMap<>();

    public ShopListener(ShopManager manager) {
        this.manager = manager;
        this.guiProvider = new ShopGuiProvider();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (manager.isShopSign(loc)) {
            Shop shop = manager.getShop(loc);
            if (!event.getPlayer().getUniqueId().equals(shop.getOwner()) && !event.getPlayer().isOp()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("You cannot break this shop!", NamedTextColor.RED));
            } else {
                manager.removeShop(loc);
                event.getPlayer().sendMessage(Component.text("SleepyShop removed.", NamedTextColor.YELLOW));
            }
        } else if (isChest(event.getBlock())) {
            for (Shop shop : manager.getShops().values()) {
                if (isSameChest(shop.getChestLocation().getBlock(), event.getBlock())) {
                    if (!event.getPlayer().getUniqueId().equals(shop.getOwner()) && !event.getPlayer().isOp()) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(Component.text("This chest belongs to a shop!", NamedTextColor.RED));
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onChestAccess(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || !isChest(block)) return;

        for (Shop shop : manager.getShops().values()) {
            if (isSameChest(shop.getChestLocation().getBlock(), block)) {
                if (!event.getPlayer().getUniqueId().equals(shop.getOwner()) && !event.getPlayer().isOp()) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Component.text("This chest belongs to a shop!", NamedTextColor.RED));
                }
                return;
            }
        }
    }

    private boolean isChest(Block block) {
        return block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.BARREL;
    }

    private boolean isSameChest(Block shopChest, Block target) {
        if (shopChest.equals(target)) return true;
        if (shopChest.getState() instanceof Chest c1 && target.getState() instanceof Chest c2) {
            return c1.getInventory().equals(c2.getInventory());
        }
        return false;
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign)) return;

        Shop shop = manager.getShop(block.getLocation());
        if (shop != null) {
            event.setCancelled(true);
            if (event.getPlayer().getUniqueId().equals(shop.getOwner())) {
                openShopEditors.put(event.getPlayer().getUniqueId(), shop.getSignLocation());
                guiProvider.openOwnerGui(event.getPlayer(), shop);
            } else {
                openShopEditors.put(event.getPlayer().getUniqueId(), shop.getSignLocation());
                guiProvider.openBuyerGui(event.getPlayer(), shop);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        openShopEditors.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equals(ShopGuiProvider.OWNER_GUI_TITLE) && !title.equals(ShopGuiProvider.BUYER_GUI_TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        Location signLoc = openShopEditors.get(player.getUniqueId());
        if (signLoc == null) return;
        Shop shop = manager.getShop(signLoc);
        if (shop == null) return;

        if (title.equals(ShopGuiProvider.OWNER_GUI_TITLE)) {
            handleOwnerClick(event, player, shop);
        } else {
            handleBuyerClick(event, player, shop);
        }
    }

    private void handleOwnerClick(InventoryClickEvent event, Player player, Shop shop) {
        int slot = event.getRawSlot();
        if (slot == 13) { // Set item
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                ItemStack shopItem = cursor.clone();
                shopItem.setAmount(1);
                shop.setItem(shopItem);
                manager.saveShop(shop);
                player.sendMessage(Component.text("Item set to " + shopItem.getType().name(), NamedTextColor.GREEN));
                guiProvider.openOwnerGui(player, shop);
            }
        } else if (slot == 10) { shop.setPrice(Math.max(0, shop.getPrice() - 10)); manager.saveShop(shop); guiProvider.openOwnerGui(player, shop);
        } else if (slot == 11) { shop.setPrice(Math.max(0, shop.getPrice() - 1)); manager.saveShop(shop); guiProvider.openOwnerGui(player, shop);
        } else if (slot == 14) { shop.setPrice(shop.getPrice() + 1); manager.saveShop(shop); guiProvider.openOwnerGui(player, shop);
        } else if (slot == 15) { shop.setPrice(shop.getPrice() + 10); manager.saveShop(shop); guiProvider.openOwnerGui(player, shop);
        } else if (slot == 21) { 
            int newAmount = shop.getAmount() == 64 ? 1 : Math.min(64, shop.getAmount() + 1);
            if (event.isLeftClick()) {
                shop.setAmount(newAmount);
            } else {
                shop.setAmount(Math.max(1, shop.getAmount() - 1));
            }
            manager.saveShop(shop);
            guiProvider.openOwnerGui(player, shop);
        } else if (slot == 26) { // Disband
            manager.removeShop(shop.getSignLocation());
            shop.getSignLocation().getBlock().setType(Material.AIR);
            player.closeInventory();
            player.sendMessage(Component.text("SleepyShop disbanded.", NamedTextColor.YELLOW));
        }
    }

    private void handleBuyerClick(InventoryClickEvent event, Player player, Shop shop) {
        if (event.getRawSlot() == 8) { // Confirm Purchase
            performTransaction(player, shop);
        }
    }

    private void performTransaction(Player buyer, Shop shop) {
        if (shop.getItem() == null) return;

        Block chestBlock = shop.getChestLocation().getBlock();
        if (!(chestBlock.getState() instanceof Chest chest)) {
            buyer.sendMessage(Component.text("Error: Chest not found!", NamedTextColor.RED));
            return;
        }

        Inventory chestInv = chest.getInventory();
        if (!chestInv.containsAtLeast(shop.getItem(), shop.getAmount())) {
            buyer.sendMessage(Component.text("Shop is out of stock!", NamedTextColor.RED));
            return;
        }

        // Check buyer's payment (Diamonds)
        ItemStack payment = new ItemStack(Material.DIAMOND, (int) shop.getPrice());
        if (shop.getPrice() > 0 && !buyer.getInventory().containsAtLeast(payment, (int) shop.getPrice())) {
            buyer.sendMessage(Component.text("You don't have enough Diamonds!", NamedTextColor.RED));
            return;
        }

        // Transaction
        if (shop.getPrice() > 0) {
            buyer.getInventory().removeItem(payment);
            chestInv.addItem(payment);
        }

        // Remove item from chest and give to buyer
        ItemStack toGive = shop.getItem().clone();
        toGive.setAmount(shop.getAmount());
        chestInv.removeItem(toGive);
        buyer.getInventory().addItem(toGive).values().forEach(item -> buyer.getWorld().dropItem(buyer.getLocation(), item));

        buyer.sendMessage(Component.text("Purchase successful!", NamedTextColor.GREEN));
        buyer.closeInventory();
    }
}