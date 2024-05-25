package me.thecamzone.island;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import me.thecamzone.Skyblock;
import me.thecamzone.chunks.ChunkCoordinates;
import me.thecamzone.database.MySQLDatabase;
import me.thecamzone.utility.StringUtil;
import me.thecamzone.utility.WorldEditUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class IslandManager {

    private final MySQLDatabase db;
    private HashMap<UUID, Island> islands = null;
    // Key: Invited Player, Value: List of islands they are invited to
    private final HashMap<UUID, List<Island>> invitedPlayers;

    public IslandManager(MySQLDatabase db) {
        this.db = db;
        this.islands = new HashMap<>();
        this.invitedPlayers = new HashMap<>();
    }

    public void invitePlayer(Player target, Island island) {
        List<Island> invites = invitedPlayers.getOrDefault(target.getUniqueId(), new ArrayList<>());

        invites.add(island);

        invitedPlayers.put(target.getUniqueId(), invites);
    }

    public void removeInvite(Player player, Island island) {
        List<Island> invites = invitedPlayers.get(player.getUniqueId());
        if(invites == null) {
            return;
        }

        invites.remove(island);
        invitedPlayers.put(player.getUniqueId(), invites);
    }

    public List<Island> getInvitedIslands(Player player) {
        List<Island> invites = invitedPlayers.get(player.getUniqueId());
        if(invites == null) {
            return new ArrayList<>();
        }

        return invites;
    }

    public void acceptInvite(Player player, Island island) {
        removeInvite(player, island);
        island.addPlayer(player.getUniqueId());

        saveIsland(island);
    }

    public Island createIsland(Player player, Chunk chunk, String name) {
        Clipboard schematic = WorldEditUtil.load(new File(Skyblock.getInstance().getDataFolder(), "schematics/island_starter.schem"));
        Location location = chunk.getBlock(8, 0, 8).getLocation().add(0, 2, 0);
        Location spawn = location.add(0.5, 0, 0.5);
        WorldEditUtil.paste(schematic, location);

        // Create an island
        Island island = new Island(UUID.randomUUID(), player.getUniqueId(), name, 0, new Date(System.currentTimeMillis()), spawn, StringUtil.generateRandomIslandSymbol());
        islands.put(island.getId(), island);

        saveIsland(island);

        Skyblock.getInstance().getLogger().info(player.getName() + " created island named " + name + ".");

        return island;
    }

    public void deleteIsland(Island island) {
        island.deleteChunks();

        islands.remove(island.getId());
        try {
            PreparedStatement deleteSkyblockIslands = db.getConnection().prepareStatement(
                "DELETE FROM skyblock_islands WHERE uuid = ?;"
            );
            deleteSkyblockIslands.setString(1, island.getId().toString());
            deleteSkyblockIslands.executeUpdate();
            deleteSkyblockIslands.close();

            // Delete all members of the island from the database
            PreparedStatement deleteStatement = db.getConnection().prepareStatement(
                "DELETE FROM skyblock_island_members WHERE island_uuid = ?;"
            );
            deleteStatement.setString(1, island.getId().toString());
            deleteStatement.executeUpdate();
            deleteStatement.close();

            // Delete all members of the island from the database
            PreparedStatement deleteClaimsStatement = db.getConnection().prepareStatement(
                    "DELETE FROM skyblock_island_claims WHERE island_uuid = ?;"
            );
            deleteClaimsStatement.setString(1, island.getId().toString());
            deleteClaimsStatement.executeUpdate();
            deleteClaimsStatement.close();
        } catch (SQLException e) {
            Skyblock.getInstance().getLogger().severe("Failed to delete island " + island.getId());
            e.printStackTrace();
        }
    }

    public List<Island> getIslands() {
        return new ArrayList<>(islands.values());
    }

    public Island getIslandByChunk(Chunk chunk) throws SQLException {
        if(chunk == null) return null;

        if(islands == null || islands.isEmpty()) {
            return null;
        }

        for(Island island : islands.values()) {
            if(island.getClaimedChunks().contains(new ChunkCoordinates(chunk.getWorld(), chunk.getX(), chunk.getZ()))) {
                return island;
            }
        }

        return null;
    }

    public List<Island> getIslandsPlayerBelongsTo(Player player) {
        List<Island> playerIslands = new ArrayList<>();
        for(Island island : islands.values()) {
            if(island.getPlayers().contains(player.getUniqueId())) {
                playerIslands.add(island);
            }
        }
        return playerIslands;
    }

    public Island getIsland(String name) {
        for(Island i : islands.values()) {
            if(i.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }

        return null;
    }

    public Island getOwnedIsland(Player player) {
        for(Island island : islands.values()) {
            if(island.getOwner().equals(player.getUniqueId())) {
                return island;
            }
        }
        return null;
    }

    public void loadIslands() throws SQLException {
        Statement statement = db.getConnection().createStatement();

        // Load islands from the database
        String sql = "SELECT * FROM skyblock_islands";
        ResultSet results = statement.executeQuery(sql);

        // Add islands to the HashMap
        while(results.next()) {
            UUID id = UUID.fromString(results.getString("uuid"));
            UUID owner = UUID.fromString(results.getString("owner"));
            String name = results.getString("name");
            double balance = results.getDouble("balance");
            Date created = results.getDate("created_time");
            Location location = results.getString("location") != null ? StringUtil.stringToLocation(results.getString("location")) : null;
            String symbol = results.getString("symbol");

            Island island = new Island(id, owner, name, balance, created, location, symbol);
            try {
                island.setPlayers(loadMembers(island));
            } catch(SQLException e) {
                Skyblock.getInstance().getLogger().severe("Failed to load members for island " + id);
                e.printStackTrace();
            }
            islands.put(id, island);
        }

        results.close();
        statement.close();

        Skyblock.getInstance().getLogger().info("Loaded " + islands.size() + " islands.");
    }

    public List<UUID> loadMembers(Island island) throws SQLException {
        List<UUID> members = new ArrayList<>();
        Statement statement = db.getConnection().createStatement();

        // Load islands from the database
        String sql = "SELECT island_uuid,member_uuid FROM skyblock_island_members WHERE island_uuid='" + island.getId() + "'";
        ResultSet results = statement.executeQuery(sql);

        // Add islands to the HashMap
        while(results.next()) {
            UUID id = UUID.fromString(results.getString("member_uuid"));
            members.add(id);
        }

        results.close();
        statement.close();

        return members;
    }

    public List<Island> getAllIslands() {
        List<Island> allIslands = new ArrayList<>();
        try {
            Statement statement = db.getConnection().createStatement();
            String sql = "SELECT * FROM skyblock_islands";
            ResultSet results = statement.executeQuery(sql);

            while(results.next()) {
                UUID id = UUID.fromString(results.getString("uuid"));
                UUID owner = UUID.fromString(results.getString("owner"));
                String name = results.getString("name");
                double balance = results.getDouble("balance");
                Date created = results.getDate("created_time");
                Location location = StringUtil.stringToLocation(results.getString("location"));
                String symbol = results.getString("symbol");

                Island island = new Island(id, owner, name, balance, created, location, symbol);
                island.setPlayers(loadMembers(island));
                allIslands.add(island);
            }

            results.close();
            statement.close();
        } catch(SQLException e) {
            Skyblock.getInstance().getLogger().severe("Failed to load all islands");
            e.printStackTrace();
        }

        return allIslands;
    }

    public void saveIsland(Island island) {
        // Save an island to the database
        try {
            PreparedStatement statement = db.getConnection().prepareStatement(
                    "INSERT INTO skyblock_islands (uuid, owner, name, balance, location, symbol, maxClaimedChunks) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE owner = VALUES(owner), name = VALUES(name), balance = VALUES(balance), location = VALUES(location), symbol = VALUES(symbol), maxClaimedChunks = VALUES(maxClaimedChunks);"
            );

            statement.setString(1, island.getId().toString());
            statement.setString(2, island.getOwner().toString());
            statement.setString(3, island.getName());
            statement.setDouble(4, island.getBalance());
            statement.setString(5, StringUtil.locationToString(island.getSpawn()));
            statement.setString(6, island.getSymbol());
            statement.setInt(7, island.getMaxChunks());
            statement.executeUpdate();
            statement.close();
        } catch(SQLException e) {
            Skyblock.getInstance().getLogger().severe("Failed to save island " + island.getId());
            e.printStackTrace();
            return;
        }

        try {
            // Delete all members of the island from the database
            PreparedStatement deleteStatement = db.getConnection().prepareStatement(
                "DELETE FROM skyblock_island_members WHERE island_uuid = ?;"
            );
            deleteStatement.setString(1, island.getId().toString());
            deleteStatement.executeUpdate();
            deleteStatement.close();

            // Insert the current members of the island
            PreparedStatement insertStatement = db.getConnection().prepareStatement(
                "INSERT INTO skyblock_island_members (island_uuid, member_uuid) VALUES (?, ?);"
            );

            for(UUID member : island.getPlayers()) {
                insertStatement.setString(1, island.getId().toString());
                insertStatement.setString(2, member.toString());
                insertStatement.executeUpdate();
            }
            insertStatement.close();
        } catch(SQLException e) {
            Skyblock.getInstance().getLogger().severe("Failed to save island " + island.getId());
            e.printStackTrace();
            return;
        }

        try {
            // Delete all claimed chunks of the island from the database
            PreparedStatement deleteStatement = db.getConnection().prepareStatement(
                "DELETE FROM skyblock_island_claims WHERE island_uuid = ?;"
            );
            deleteStatement.setString(1, island.getId().toString());
            deleteStatement.executeUpdate();
            deleteStatement.close();

            // Insert the current claimed chunks of the island
            PreparedStatement insertStatement = db.getConnection().prepareStatement(
                "INSERT INTO skyblock_island_claims (island_uuid, world, chunkX, chunkZ) VALUES (?, ?, ?, ?);"
            );

            for(ChunkCoordinates chunk : island.getClaimedChunks()) {
                insertStatement.setString(1, island.getId().toString());
                insertStatement.setString(2, chunk.getWorld().getName());
                insertStatement.setInt(3, chunk.getX());
                insertStatement.setInt(4, chunk.getZ());
                insertStatement.executeUpdate();
            }
            insertStatement.close();
        } catch(SQLException e) {
            Skyblock.getInstance().getLogger().severe("Failed to save island " + island.getId());
            e.printStackTrace();
            return;
        }
    }

}
