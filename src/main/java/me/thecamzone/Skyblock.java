package me.thecamzone;

import co.aikar.commands.PaperCommandManager;
import me.thecamzone.commands.IslandCommand;
import me.thecamzone.commands.SkyBlockCommand;
import me.thecamzone.database.MySQLDatabase;
import me.thecamzone.island.IslandManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.sql.Statement;

public final class Skyblock extends JavaPlugin {

    private static Skyblock instance;
    private static IslandManager islandManager;
    private MySQLDatabase db;

    @Override
    public void onEnable() {
        instance = this;

        createFiles();
        initializeDatabase();
        registerCommands();

        islandManager = new IslandManager(db);
        try {
            islandManager.loadIslands();
        } catch (SQLException e) {
            getLogger().severe("Could not load islands from the database.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        disconnectDatabase();
    }

    public static Skyblock getInstance() {
        return instance;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new SkyBlockCommand());
        commandManager.registerCommand(new IslandCommand());
    }

    public MySQLDatabase getDatabase() {
        return db;
    }

    private void createFiles() {
        saveDefaultConfig();
    }

    private void initializeDatabase() {
        ConfigurationSection mysqlConfig = getConfig().getConfigurationSection("mysql");

        if(mysqlConfig == null) {
            getLogger().severe("Failed to load the MySQL configuration.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        String address = mysqlConfig.getString("address");
        String port = mysqlConfig.getString("port");
        String username = mysqlConfig.getString("username");
        String password = mysqlConfig.getString("password");
        String database = mysqlConfig.getString("database");

        db = new MySQLDatabase(
                "jdbc:mysql://" + address + ":" + port + "/" + database,
                username,
                password
        );

        // Connect to database
        try {
            db.connect();
            getLogger().info("Connected to the database.");
        } catch (Exception e) {
            getLogger().severe("Failed to connect to the database.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Create Tables
        try {
            db.executeStatement(
                "CREATE TABLE IF NOT EXISTS skyblock_islands (" +
                "uuid varchar(36) primary key," +
                "owner text," +
                "name text," +
                "balance double," +
                "created_time timestamp DEFAULT CURRENT_TIMESTAMP"
            + ");");

            db.executeStatement(
                "CREATE TABLE IF NOT EXISTS skyblock_players (" +
                "uuid varchar(36) primary key," +
                "owned_island text" +
            ");");

            db.executeStatement(
                "CREATE TABLE IF NOT EXISTS skyblock_island_members (" +
                "id int not null primary key auto_increment," +
                "island_uuid varchar(36)," +
                "member_uuid text" +
            ");");

            getLogger().info("Created database tables.");
        }  catch (Exception e) {
            getLogger().severe("Could not create database tables.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    private void disconnectDatabase() {
        try {
            db.disconnect();
        } catch (SQLException e) {
            getLogger().severe("Could not close database connection.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
