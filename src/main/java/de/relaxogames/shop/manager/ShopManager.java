package de.relaxogames.shop.manager;

import de.relaxogames.shop.model.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopManager {
    private final File file;
    private final FileConfiguration config;
    private final Map<String, Shop> shops = new HashMap<>();

    public ShopManager(JavaPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "shops.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadShops();
    }

    private void loadShops() {
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

            String displayIdStr = section.getString("displayId");
            if (displayIdStr != null) {
                shop.setDisplayEntityId(UUID.fromString(displayIdStr));
            }
            
            shops.put(locationToString(signLoc), shop);
            updateDisplay(shop);
        }
    }

    public void saveShop(Shop shop) {
        String id = locationToString(shop.getSignLocation());
        shops.put(id, shop);
        
        config.set(id + ".sign", shop.getSignLocation());
        config.set(id + ".chest", shop.getChestLocation());
        config.set(id + ".owner", shop.getOwner().toString());
        config.set(id + ".item", shop.getSellItem());
        config.set(id + ".paymentItem", shop.getPaymentItem());
        config.set(id + ".price", shop.getTakeAmount());
        config.set(id + ".amount", shop.getOutputAmount());
        if (shop.getDisplayEntityId() != null) {
            config.set(id + ".displayId", shop.getDisplayEntityId().toString());
        }
        save();
        updateDisplay(shop);
    }

    public void removeShop(Location signLoc) {
        String id = locationToString(signLoc);
        Shop shop = shops.remove(id);
        if (shop != null && shop.getDisplayEntityId() != null) {
            Entity entity = Bukkit.getEntity(shop.getDisplayEntityId());
            if (entity != null) entity.remove();
        }
        config.set(id, null);
        save();
    }

    public void updateDisplay(Shop shop) {
        if (shop.getChestLocation() == null || shop.getChestLocation().getWorld() == null) return;
        
        Location loc = shop.getChestLocation().clone().add(0.5, 1.2, 0.5);
        if (!loc.isChunkLoaded()) return;

        TextDisplay display = null;

        if (shop.getDisplayEntityId() != null) {
            Entity entity = Bukkit.getEntity(shop.getDisplayEntityId());
            if (entity instanceof TextDisplay td) {
                display = td;
            }
        }

        if (display == null) {
            // Search for existing display at the location to avoid duplicates
            for (Entity nearby : loc.getWorld().getNearbyEntities(loc, 0.1, 0.1, 0.1)) {
                if (nearby instanceof TextDisplay td) {
                    display = td;
                    shop.setDisplayEntityId(display.getUniqueId());
                    // Update config with the found ID
                    String id = locationToString(shop.getSignLocation());
                    config.set(id + ".displayId", shop.getDisplayEntityId().toString());
                    save();
                    break;
                }
            }
        }

        if (display == null) {
            display = loc.getWorld().spawn(loc, TextDisplay.class);
            shop.setDisplayEntityId(display.getUniqueId());
            display.setBillboard(Display.Billboard.CENTER);

            // Save the displayId immediately
            String id = locationToString(shop.getSignLocation());
            config.set(id + ".displayId", shop.getDisplayEntityId().toString());
            save();
        } else {
            display.teleport(loc);
        }

        String ownerName = Bukkit.getOfflinePlayer(shop.getOwner()).getName();
        if (ownerName == null) ownerName = "Unknown";

        String sellItemName = shop.getSellItem() != null ? shop.getSellItem().getType().name() : "None";
        String payItemName = shop.getPaymentItem() != null ? shop.getPaymentItem().getType().name() : "None";

        Component text = Component.text()
                .append(Component.text(ownerName + "'s Shop", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("Selling: ", NamedTextColor.WHITE))
                .append(Component.text(shop.getOutputAmount() + "x " + sellItemName, NamedTextColor.GREEN))
                .append(Component.newline())
                .append(Component.text("Price: ", NamedTextColor.WHITE))
                .append(Component.text(shop.getTakeAmount() + "x " + payItemName, NamedTextColor.AQUA))
                .build();

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

    private void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}