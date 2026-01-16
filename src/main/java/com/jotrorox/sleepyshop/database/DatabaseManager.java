package com.jotrorox.sleepyshop.database;

import com.jotrorox.sleepyshop.model.Shop;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;
    private final ExecutorService executor;
    private static final String SHOP_TABLE_DDL =
        "CREATE TABLE IF NOT EXISTS shops (" +
        "sign_location TEXT PRIMARY KEY," +
        "chest_location TEXT NOT NULL," +
        "owner TEXT NOT NULL," +
        "sell_item TEXT," +
        "payment_item TEXT," +
        "take_amount INTEGER," +
        "output_amount INTEGER," +
        "shop_name TEXT," +
        "show_display BOOLEAN," +
        "show_stock_message BOOLEAN," +
        "display_entity_id TEXT" +
        ");";

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.executor = Executors.newSingleThreadExecutor();
        initDatabase();
    }

    private void initDatabase() {
        try {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().severe("H2 driver not found on classpath!");
                return;
            }
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                var ignored = dataFolder.mkdirs();
            }
            File h2BaseFile = new File(dataFolder, "shops");
            File sqliteFile = new File(dataFolder, "shops.db");
            connection = DriverManager.getConnection(
                "jdbc:h2:file:" +
                h2BaseFile.getAbsolutePath() +
                ";AUTO_SERVER=FALSE;DATABASE_TO_UPPER=FALSE"
            );

            try (Statement statement = connection.createStatement()) {
                statement.execute(SHOP_TABLE_DDL);
            }

            if (sqliteFile.exists()) {
                migrateFromSqlite(sqliteFile, h2BaseFile);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not initialize H2 database!");
            plugin.getLogger().severe("H2 init error: " + e.getMessage());
        }
    }

    private void migrateFromSqlite(File sqliteFile, File h2File) {
        try {
            if (connection == null) {
                return;
            }
            if (!isTableEmpty(connection)) {
                plugin
                    .getLogger()
                    .info(
                        "Skipping SQLite migration because H2 already has data."
                    );
                return;
            }

            if (!tableExists(connection, "shops")) {
                plugin
                    .getLogger()
                    .warning(
                        "Skipping SQLite migration because H2 shops table is missing."
                    );
                return;
            }

            plugin
                .getLogger()
                .info(
                    "Found legacy SQLite database. Migrating to H2: " +
                    h2File.getName()
                );

            int migratedRows = 0;
            connection.setAutoCommit(false);

            try (
                Connection sqliteConnection = DriverManager.getConnection(
                    "jdbc:sqlite:" + sqliteFile.getAbsolutePath()
                );
                Statement readStatement = sqliteConnection.createStatement();
                ResultSet rs = readStatement.executeQuery(
                    "SELECT * FROM shops"
                );
                PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO shops (sign_location, chest_location, owner, sell_item, payment_item, take_amount, output_amount, shop_name, show_display, show_stock_message, display_entity_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                )
            ) {
                if (!tableExists(sqliteConnection, "shops")) {
                    plugin
                        .getLogger()
                        .warning(
                            "Skipping SQLite migration because legacy shops table is missing."
                        );
                    connection.rollback();
                    return;
                }
                int legacyCount = countRows(sqliteConnection);
                plugin
                    .getLogger()
                    .info(
                        "SQLite shops rows detected: " + legacyCount + ". Starting migration..."
                    );
                while (rs.next()) {
                    try {
                        insert.setString(1, rs.getString("sign_location"));
                        insert.setString(2, rs.getString("chest_location"));
                        insert.setString(3, rs.getString("owner"));
                        insert.setString(4, rs.getString("sell_item"));
                        insert.setString(5, rs.getString("payment_item"));
                        insert.setInt(6, rs.getInt("take_amount"));
                        insert.setInt(7, rs.getInt("output_amount"));
                        insert.setString(8, rs.getString("shop_name"));
                        insert.setBoolean(9, rs.getBoolean("show_display"));
                        insert.setBoolean(10, rs.getBoolean("show_stock_message"));
                        insert.setString(11, rs.getString("display_entity_id"));
                        insert.addBatch();
                        migratedRows++;
                    } catch (SQLException rowException) {
                        plugin
                            .getLogger()
                            .warning(
                                "Skipping SQLite row during migration due to error: " +
                                rowException.getMessage()
                            );
                    }
                }
                insert.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                plugin
                    .getLogger()
                    .severe("Could not migrate SQLite data to H2!");
                return;
            } finally {
                connection.setAutoCommit(true);
            }

            int h2Count = countRows(connection);
            plugin
                .getLogger()
                .info(
                    "SQLite migration finished. Migrated rows: " +
                    migratedRows +
                    ", H2 rows: " +
                    h2Count
                );
            if (h2Count >= migratedRows) {
                File backupFile = new File(
                    sqliteFile.getParentFile(),
                    sqliteFile.getName() + ".bak"
                );
                if (sqliteFile.renameTo(backupFile)) {
                    plugin
                        .getLogger()
                        .info(
                            "SQLite migration complete. Backup created: " +
                            backupFile.getName()
                        );
                } else {
                    plugin
                        .getLogger()
                        .warning(
                            "SQLite migration complete, but could not rename old database."
                        );
                }
            } else {
                plugin
                    .getLogger()
                    .warning(
                        "SQLite migration finished with mismatched row counts; leaving old database intact."
                    );
            }
        } catch (SQLException e) {
            plugin
                .getLogger()
                .severe("Could not finalize SQLite to H2 migration!");
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(), null)) {
            return rs.next();
        }
    }

    private boolean isTableEmpty(Connection conn) throws SQLException {
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM shops")
        ) {
            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
        }
        return true;
    }

    private int countRows(Connection conn) throws SQLException {
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM shops")
        ) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            executor.shutdown();
        } catch (SQLException e) {
            plugin
                .getLogger()
                .severe("Could not initialize close SQLite database!");
        }
    }

    private String serializeLocation(Location loc) {
        if (loc == null) return null;
        return (
            loc.getWorld().getName() +
            ":" +
            loc.getX() +
            ":" +
            loc.getY() +
            ":" +
            loc.getZ() +
            ":" +
            loc.getYaw() +
            ":" +
            loc.getPitch()
        );
    }

    private Location deserializeLocation(String s) {
        if (s == null) return null;
        String[] parts = s.split(":");
        if (parts.length < 6) return null;
        org.bukkit.World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        return new Location(
            world,
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3]),
            Float.parseFloat(parts[4]),
            Float.parseFloat(parts[5])
        );
    }

    private String serializeItemStack(ItemStack item) {
        if (item == null) return null;
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);
        return config.saveToString();
    }

    private ItemStack deserializeItemStack(String s) {
        if (s == null) return null;
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(s);
            return config.getItemStack("item");
        } catch (Exception e) {
            return null;
        }
    }

    public CompletableFuture<Void> saveShop(Shop shop) {
        if (connection == null) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(
            () -> {
                String sql =
                    "MERGE INTO shops (sign_location, chest_location, owner, sell_item, payment_item, " +
                    "take_amount, output_amount, shop_name, show_display, show_stock_message, display_entity_id) " +
                    "KEY(sign_location) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (
                    PreparedStatement pstmt = connection.prepareStatement(sql)
                ) {
                    pstmt.setString(
                        1,
                        serializeLocation(shop.getSignLocation())
                    );
                    pstmt.setString(
                        2,
                        serializeLocation(shop.getChestLocation())
                    );
                    pstmt.setString(3, shop.getOwner().toString());
                    pstmt.setString(4, serializeItemStack(shop.getSellItem()));
                    pstmt.setString(
                        5,
                        serializeItemStack(shop.getPaymentItem())
                    );
                    pstmt.setInt(6, shop.getTakeAmount());
                    pstmt.setInt(7, shop.getOutputAmount());
                    pstmt.setString(8, shop.getShopName());
                    pstmt.setBoolean(9, shop.isShowDisplay());
                    pstmt.setBoolean(10, shop.isShowStockMessage());
                    pstmt.setString(
                        11,
                        shop.getDisplayEntityId() != null
                            ? shop.getDisplayEntityId().toString()
                            : null
                    );
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().severe("Could not save shop to H2!");
                }
            },
            executor
        );
    }

    public void removeShop(Location signLoc) {
        if (connection == null) {
            CompletableFuture.completedFuture(null);
            return;
        }
        CompletableFuture.runAsync(
            () -> {
                String sql = "DELETE FROM shops WHERE sign_location = ?";
                try (
                    PreparedStatement pstmt = connection.prepareStatement(sql)
                ) {
                    pstmt.setString(1, serializeLocation(signLoc));
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    plugin
                        .getLogger()
                        .severe("Could not remove shop from H2!");
                }
            },
            executor
        );
    }

    public CompletableFuture<List<Shop>> loadShops() {
        if (connection == null) return CompletableFuture.completedFuture(
            new ArrayList<>()
        );
        return CompletableFuture.supplyAsync(
            () -> {
                List<Shop> shops = new ArrayList<>();
                String sql = "SELECT * FROM shops";
                try (
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)
                ) {
                    while (rs.next()) {
                        Location signLoc = deserializeLocation(
                            rs.getString("sign_location")
                        );
                        Location chestLoc = deserializeLocation(
                            rs.getString("chest_location")
                        );
                        if (signLoc == null || chestLoc == null) continue;

                        UUID owner = UUID.fromString(rs.getString("owner"));

                        Shop shop = new Shop(signLoc, chestLoc, owner);
                        shop.setSellItem(
                            deserializeItemStack(rs.getString("sell_item"))
                        );
                        shop.setPaymentItem(
                            deserializeItemStack(rs.getString("payment_item"))
                        );
                        shop.setTakeAmount(rs.getInt("take_amount"));
                        shop.setOutputAmount(rs.getInt("output_amount"));
                        shop.setShopName(rs.getString("shop_name"));
                        shop.setShowDisplay(rs.getBoolean("show_display"));
                        shop.setShowStockMessage(
                            rs.getBoolean("show_stock_message")
                        );
                        String displayId = rs.getString("display_entity_id");
                        if (displayId != null) {
                            shop.setDisplayEntityId(UUID.fromString(displayId));
                        }
                        shops.add(shop);
                    }
                } catch (SQLException e) {
                    plugin
                        .getLogger()
                        .severe("Could not load shops from H2!");
            }
            return shops;
        },
        executor
        );
    }
}
