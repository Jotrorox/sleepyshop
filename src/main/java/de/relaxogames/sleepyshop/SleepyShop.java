package de.relaxogames.sleepyshop;

import de.relaxogames.sleepyshop.database.DatabaseManager;
import de.relaxogames.sleepyshop.listener.ShopListener;
import de.relaxogames.sleepyshop.listener.SignListener;
import de.relaxogames.sleepyshop.manager.ShopManager;
import de.relaxogames.sleepyshop.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SleepyShop extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.databaseManager = new DatabaseManager(this);
        ShopManager shopManager = new ShopManager(this);
        Bukkit.getPluginManager().registerEvents(
            new ShopListener(shopManager),
            this
        );
        Bukkit.getPluginManager().registerEvents(
            new SignListener(shopManager),
            this
        );

        getLogger().info(
            "SleepyShop has been enabled! Made by the RelaxoGames Development Team."
        );

        (new UpdateChecker(this, "Jotrorox/SleepyShop")).performCheck();
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnection();
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
