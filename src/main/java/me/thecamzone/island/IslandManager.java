package me.thecamzone.island;

import me.thecamzone.Skyblock;
import me.thecamzone.database.MySQLDatabase;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class IslandManager {

    private final MySQLDatabase db;
    private final HashMap<UUID, Island> islands;

    public IslandManager(MySQLDatabase db) {
        this.db = db;
        this.islands = new HashMap<>();
    }

    public void createIsland(Player player, String name) {
        // Create an island
        Island island = new Island(UUID.randomUUID(), player.getUniqueId(), name, 0, new Date(System.currentTimeMillis()));
        islands.put(island.getId(), island);

        saveIsland(island);

        Skyblock.getInstance().getLogger().info(player.getName() + " created island named " + name + ".");
    }

    public List<Island> getIslands() {
        return new ArrayList<>(islands.values());
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

            Island island = new Island(id, owner, name, balance, created);
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

    public void saveIsland(Island island) {
        // Save an island to the database
        try {
            PreparedStatement statement = db.getConnection().prepareStatement(
                    "INSERT INTO skyblock_islands (uuid, owner, name, balance) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE owner = VALUES(owner), name = VALUES(name), balance = VALUES(balance);"
            );

            statement.setString(1, island.getId().toString());
            statement.setString(2, island.getOwner().toString());
            statement.setString(3, island.getName());
            statement.setDouble(4, island.getBalance());
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
    }

}
