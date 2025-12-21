package de.relaxogames.shop;

import de.relaxogames.shop.listener.ShopListener;
import de.relaxogames.shop.listener.SignListener;
import de.relaxogames.shop.manager.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SleepyShop extends JavaPlugin {

    private ShopManager shopManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.shopManager = new ShopManager(this);
        Bukkit.getPluginManager().registerEvents(new ShopListener(shopManager), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(shopManager), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ShopManager getShopManager() {
        return shopManager;
    }
}
