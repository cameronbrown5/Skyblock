package me.thecamzone;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.Quests;
import me.pikamug.quests.quests.Quest;
import me.thecamzone.commands.DeveloperCommand;
import me.thecamzone.commands.IslandCommand;
import me.thecamzone.commands.SkyBlockCommand;
import me.thecamzone.database.MySQLDatabase;
import me.thecamzone.events.OnRespawn;
import me.thecamzone.island.IslandProtectionListener;
import me.thecamzone.events.OnPlayerInteract;
import me.thecamzone.events.OnPlayerJoin;
import me.thecamzone.events.OnPlayerMove;
import me.thecamzone.island.Island;
import me.thecamzone.island.IslandManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.sql.SQLException;

public final class Skyblock extends JavaPlugin {

    private static Skyblock instance;
    private static IslandManager islandManager;
    private MySQLDatabase db;

    private CamsLootTables camsLootTables;
    private ZoneMCUtility zoneMCUtility;
    private BukkitQuestsPlugin questsPlugin;

    @Override
    public void onEnable() {
        instance = this;

        registerCommands();
        registerListeners();

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            createFiles();
            initializeDatabase();

            initializeIslandManager();
            hookPlugins();
        }, 1);

    }

    @Override
    public void onDisable() {
        disconnectDatabase();
    }

    public void reload() {
        reloadConfig();
        disconnectDatabase();
        initializeDatabase();

        islandManager = new IslandManager(db);
        try {
            islandManager.loadIslands();
        } catch (SQLException e) {
            getLogger().severe("Could not load islands from the database.");
            throw new RuntimeException(e);
        }
    }

    public static Skyblock getInstance() {
        return instance;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    private void initializeIslandManager() {
        islandManager = new IslandManager(db);
        try {
            islandManager.loadIslands();
        } catch (SQLException e) {
            getLogger().severe("Could not load islands from the database.");
            getServer().getPluginManager().disablePlugin(this);
            getServer().shutdown();
            throw new RuntimeException(e);
        }
    }

    private void hookPlugins() {
        camsLootTables = (CamsLootTables) Bukkit.getPluginManager().getPlugin("CamsLootTables");
        if(camsLootTables == null) {
            getLogger().severe("Could not find CamsLootTables plugin.");
            getServer().getPluginManager().disablePlugin(this);
            getServer().shutdown();
            return;
        } else {
            getLogger().info("Hooked into CamsLootTables plugin.");
        }

        zoneMCUtility = (ZoneMCUtility) Bukkit.getPluginManager().getPlugin("ZoneMC");
        if(zoneMCUtility == null) {
            getLogger().severe("Could not find ZoneMCUtility plugin.");
            getServer().getPluginManager().disablePlugin(this);
            getServer().shutdown();
            return;
        } else {
            getLogger().info("Hooked into ZoneMCUtility plugin.");
        }

        questsPlugin = (BukkitQuestsPlugin) Bukkit.getPluginManager().getPlugin("Quests");
        if(questsPlugin == null) {
            getLogger().severe("Could not find Quests plugin.");
            getServer().getPluginManager().disablePlugin(this);
            getServer().shutdown();
            return;
        } else {
            getLogger().info("Hooked into Quests plugin.");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new IslandProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerInteract(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerMove(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new OnRespawn(), this);
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.registerCommand(new SkyBlockCommand());
        commandManager.registerCommand(new IslandCommand());
        commandManager.registerCommand(new DeveloperCommand());

        commandManager.getCommandCompletions().registerAsyncCompletion("player_islands", c ->
            islandManager.getIslandsPlayerBelongsTo(c.getPlayer()).stream()
                .map(Island::getName)
                .collect(ImmutableList.toImmutableList()
        ));

        commandManager.getCommandCompletions().registerAsyncCompletion("player_invites", c ->
            islandManager.getInvitedIslands(c.getPlayer()).stream()
                .map(Island::getName)
                .collect(ImmutableList.toImmutableList()
        ));

        commandManager.getCommandCompletions().registerAsyncCompletion("island_players", c ->
            islandManager.getOwnedIsland(c.getPlayer()).getPlayers().stream()
                .map(player -> getServer().getOfflinePlayer(player).getName())
                .collect(ImmutableList.toImmutableList()
        ));

        commandManager.getCommandCompletions().registerAsyncCompletion("islands", c ->
            islandManager.getIslands().stream()
                .map(Island::getName)
                .collect(ImmutableList.toImmutableList()
        ));
    }

    public MySQLDatabase getDatabase() {
        return db;
    }

    public Quests getQuestsPlugin() {
        return questsPlugin;
    }

    private void createFiles() {
        saveDefaultConfig();

        createSchematicsFolder();
        copySchematic();
    }

    private void createSchematicsFolder() {
        File schematicsFolder = new File(getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
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
                "location text," +
                "symbol varchar(255)," +
                "maxClaimedChunks int," +
                "created_time timestamp DEFAULT CURRENT_TIMESTAMP"
                + ");");

            db.executeStatement(
                "CREATE TABLE IF NOT EXISTS skyblock_players (" +
                "uuid varchar(36) primary key," +
                "owned_island text," +
                "showNearby boolean" +
            ");");

            db.executeStatement(
                "CREATE TABLE IF NOT EXISTS skyblock_island_members (" +
                "id int not null primary key auto_increment," +
                "island_uuid varchar(36)," +
                "member_uuid text" +
            ");");

            db.executeStatement(
                "CREATE TABLE IF NOT EXISTS skyblock_island_claims (" +
                "id int not null primary key auto_increment," +
                "island_uuid varchar(36)," +
                "world text," +
                "chunkX INT," +
                "chunkZ INT," +
                "UNIQUE KEY chunk_coordinates (chunkX, chunkZ)" +
            ");");

            getLogger().info("Created database tables.");
        }  catch (Exception e) {
            getLogger().severe("Could not create database tables.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    public NamespacedKey getChunkClaimedKey() {
        return new NamespacedKey(this, "chunk_claimed");
    }

    public CamsLootTables getCamsLootTables() {
        return camsLootTables;
    }

    private void copySchematic() {
        String resourceName = "/island_starter.schem"; // the resource name
        File schematicsFolder = new File(getDataFolder(), "schematics");
        File destinationFile = new File(schematicsFolder, resourceName);

        try (InputStream resourceStream = getClass().getResourceAsStream(resourceName)) {
            if (resourceStream == null) {
                // handle the case where the resource is not found
                System.out.println("Resource not found: " + resourceName);
            } else {
                Files.copy(resourceStream, destinationFile.toPath());
            }
        } catch (FileAlreadyExistsException e) {
            return;
        } catch (IOException e) {
            // handle the IOException
            e.printStackTrace();
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
