package me.thecamzone.player;

import me.thecamzone.Skyblock;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerConfigManager {

    private static PlayerConfigManager instance;
    private HashMap<UUID, PlayerConfig> playerConfigs = new HashMap<>();

    public PlayerConfig getPlayerConfig(UUID player) {
        return playerConfigs.get(player);
    }

    public void setPlayerConfig(UUID player, PlayerConfig config) {
        playerConfigs.put(player, config);
    }

    public void removePlayerConfig(UUID player) {
        playerConfigs.remove(player);
    }

    public static PlayerConfigManager getInstance() {
        if (instance == null) {
            instance = new PlayerConfigManager();
        }
        return instance;
    }

    public void loadPlayerConfig(UUID player) {
        // Load player config from the database
        try {
            PreparedStatement statement = Skyblock.getInstance().getDatabase().getConnection().prepareStatement(
                "SELECT * FROM skyblock_players WHERE uuid = ?;"
            );

            statement.setString(1, player.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                UUID ownedIsland = resultSet.getString("owned_island") != null ? UUID.fromString(resultSet.getString("owned_island")) : null;
                boolean showNearby = resultSet.getBoolean("showNearby");

                PlayerConfig config = new PlayerConfig(player, ownedIsland, showNearby);
                playerConfigs.put(player, config);
            } else {
                PlayerConfigManager.getInstance().setPlayerConfig(player, new PlayerConfig(player));
            }

            resultSet.close();
            statement.close();
        } catch(SQLException e) {
            Skyblock.getInstance().getLogger().severe("Failed to load player config for " + Bukkit.getOfflinePlayer(player).getName());
            e.printStackTrace();
        }
    }

}
