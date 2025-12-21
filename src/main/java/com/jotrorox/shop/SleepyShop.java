package com.jotrorox.shop;

import com.jotrorox.shop.database.DatabaseManager;
import com.jotrorox.shop.listener.ShopListener;
import com.jotrorox.shop.listener.SignListener;
import com.jotrorox.shop.manager.ShopManager;
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

        getLogger().info("SleepyShop has been enabled! Made by Jotrorox and RelaxoGames.");
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
