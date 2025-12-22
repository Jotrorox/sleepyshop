package com.jotrorox.shop.manager;

import com.jotrorox.shop.SleepyShop;
import com.jotrorox.shop.model.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopManager {
    private final JavaPlugin plugin;
    private final Map<String, Shop> shops = new HashMap<>();

    public ShopManager(JavaPlugin plugin) {
        this.plugin = plugin;
        migrateIfNecessary();
        loadShops();
    }

    private void migrateIfNecessary() {
        File yamlFile = new File(plugin.getDataFolder(), "shops.yml");
        if (yamlFile.exists()) {
            plugin.getLogger().info("Found shops.yml, migrating to SQLite...");
            FileConfiguration config = YamlConfiguration.loadConfiguration(yamlFile);
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null) continue;

                Location signLoc = section.getLocation("sign");
                Location chestLoc = section.getLocation("chest");
                String ownerStr = section.getString("owner");
                if (signLoc == null || chestLoc == null || ownerStr == null) continue;

                Shop shop = new Shop(signLoc, chestLoc, UUID.fromString(ownerStr));
                shop.setSellItem(section.getItemStack("item"));
                shop.setPaymentItem(section.getItemStack("paymentItem", new ItemStack(Material.DIAMOND)));
                shop.setTakeAmount(section.getInt("price"));
                shop.setOutputAmount(section.getInt("amount", 1));
                shop.setShopName(section.getString("shopName"));
                shop.setShowDisplay(section.getBoolean("showDisplay", true));
                shop.setShowStockMessage(section.getBoolean("showStockMessage", true));

                String displayIdStr = section.getString("displayId");
                if (displayIdStr != null) {
                    shop.setDisplayEntityId(UUID.fromString(displayIdStr));
                }

                ((SleepyShop) plugin).getDatabaseManager().saveShop(shop).join();
            }
            var ignored = yamlFile.renameTo(new File(plugin.getDataFolder(), "shops.yml.bak"));
            plugin.getLogger().info("Migration complete! shops.yml renamed to shops.yml.bak");
        }
    }

    private void loadShops() {
        ((SleepyShop) plugin).getDatabaseManager().loadShops().thenAccept(loadedShops -> Bukkit.getScheduler().runTask(plugin, () -> {
            for (Shop shop : loadedShops) {
                shops.put(locationToString(shop.getSignLocation()), shop);
                updateDisplay(shop);
            }
        }));
    }

    public void saveShop(Shop shop) {
        String id = locationToString(shop.getSignLocation());
        shops.put(id, shop);
        ((SleepyShop) plugin).getDatabaseManager().saveShop(shop).thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> updateDisplay(shop)));
    }

    public void removeShop(Location signLoc) {
        String id = locationToString(signLoc);
        Shop shop = shops.remove(id);
        if (shop != null && shop.getDisplayEntityId() != null) {
            Entity entity = Bukkit.getEntity(shop.getDisplayEntityId());
            if (entity != null) entity.remove();
        }
        ((SleepyShop) plugin).getDatabaseManager().removeShop(signLoc);
    }

    public void updateDisplay(Shop shop) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> updateDisplay(shop));
            return;
        }

        if (shop.getChestLocation() == null || shop.getChestLocation().getWorld() == null) return;
        
        Location loc = shop.getChestLocation().clone().add(0.5, 1.2, 0.5);
        if (!loc.isChunkLoaded()) return;

        if (!shop.isShowDisplay()) {
            if (shop.getDisplayEntityId() != null) {
                Entity entity = Bukkit.getEntity(shop.getDisplayEntityId());
                if (entity != null) entity.remove();
                shop.setDisplayEntityId(null);
                ((SleepyShop) plugin).getDatabaseManager().saveShop(shop);
            }
            return;
        }

        TextDisplay display = null;

        if (shop.getDisplayEntityId() != null) {
            Entity entity = Bukkit.getEntity(shop.getDisplayEntityId());
            if (entity instanceof TextDisplay td) {
                display = td;
            }
        }

        if (display == null) {
            // Search for an existing display at the location to avoid duplicates
            for (Entity nearby : loc.getWorld().getNearbyEntities(loc, 0.1, 0.1, 0.1)) {
                if (nearby instanceof TextDisplay td) {
                    display = td;
                    shop.setDisplayEntityId(display.getUniqueId());
                    ((SleepyShop) plugin).getDatabaseManager().saveShop(shop);
                    break;
                }
            }
        }

        if (display == null) {
            display = loc.getWorld().spawn(loc, TextDisplay.class);
            shop.setDisplayEntityId(display.getUniqueId());
            display.setBillboard(Display.Billboard.CENTER);

            // Save the displayId immediately
            ((SleepyShop) plugin).getDatabaseManager().saveShop(shop);
        } else {
            display.teleport(loc);
        }

        String ownerName = Bukkit.getOfflinePlayer(shop.getOwner()).getName();
        if (ownerName == null) ownerName = "Unknown";

        String shopName = shop.getShopName();
        String sellItemName = shop.getSellItem() != null ? shop.getSellItem().getType().name() : "None";
        String payItemName = shop.getPaymentItem() != null ? shop.getPaymentItem().getType().name() : "None";

        // Stock check
        boolean outOfStock = false;
        if (shop.getSellItem() != null) {
            Block chestBlock = shop.getChestLocation().getBlock();
            if (chestBlock.getState() instanceof Chest chest) {
                Inventory chestInv = chest.getInventory();
                if (!chestInv.containsAtLeast(shop.getSellItem(), shop.getOutputAmount())) {
                    outOfStock = true;
                }
            }
        }

        FileConfiguration pluginConfig = plugin.getConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        String titleStr;
        if (shopName != null && !shopName.isEmpty()) {
            titleStr = pluginConfig.getString("shop-display.custom-title", "<gold><b>{shopname}</b>")
                    .replace("{shopname}", shopName);
        } else {
            titleStr = pluginConfig.getString("shop-display.title", "<gold><b>{owner}'s Shop</b>")
                    .replace("{owner}", ownerName);
        }

        Component title = mm.deserialize(titleStr);
        Component selling = mm.deserialize(pluginConfig.getString("shop-display.selling", "<white>Selling: <green>{amount}x {item}")
                .replace("{amount}", String.valueOf(shop.getOutputAmount()))
                .replace("{item}", sellItemName));
        Component price = mm.deserialize(pluginConfig.getString("shop-display.price", "<white>Price: <aqua>{price}x {payitem}")
                .replace("{price}", String.valueOf(shop.getTakeAmount()))
                .replace("{payitem}", payItemName));

        Component text = title
                .append(Component.newline())
                .append(selling)
                .append(Component.newline())
                .append(price);

        if (outOfStock && shop.isShowStockMessage()) {
            text = text.append(Component.newline())
                    .append(mm.deserialize(pluginConfig.getString("shop-display.out-of-stock", "<red><b>OUT OF STOCK</b>")));
        }

        display.text(text);
    }

    public Shop getShop(Location signLoc) {
        return shops.get(locationToString(signLoc));
    }

    public Map<String, Shop> getShops() {
        return shops;
    }

    public boolean isShopSign(Location loc) {
        return shops.containsKey(locationToString(loc));
    }

    private String locationToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return "null";
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

}