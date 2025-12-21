package de.relaxogames.shop;

import de.relaxogames.shop.database.DatabaseManager;
import de.relaxogames.shop.listener.ShopListener;
import de.relaxogames.shop.listener.SignListener;
import de.relaxogames.shop.manager.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SleepyShop extends JavaPlugin {

    private ShopManager shopManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.databaseManager = new DatabaseManager(this);
        this.shopManager = new ShopManager(this);
        Bukkit.getPluginManager().registerEvents(new ShopListener(shopManager), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(shopManager), this);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
