package de.relaxogames.shop.manager;

import de.relaxogames.shop.model.Shop;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
            shop.setItem(section.getItemStack("item"));
            shop.setPrice(section.getDouble("price"));
            shop.setAmount(section.getInt("amount", 1));
            
            shops.put(locationToString(signLoc), shop);
        }
    }

    public void saveShop(Shop shop) {
        String id = locationToString(shop.getSignLocation());
        shops.put(id, shop);
        
        config.set(id + ".sign", shop.getSignLocation());
        config.set(id + ".chest", shop.getChestLocation());
        config.set(id + ".owner", shop.getOwner().toString());
        config.set(id + ".item", shop.getItem());
        config.set(id + ".price", shop.getPrice());
        config.set(id + ".amount", shop.getAmount());
        save();
    }

    public void removeShop(Location signLoc) {
        String id = locationToString(signLoc);
        shops.remove(id);
        config.set(id, null);
        save();
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