package me.thecamzone.player;

import me.thecamzone.Skyblock;
import me.thecamzone.database.MySQLDatabase;
import me.thecamzone.utility.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerConfig {

    private final MySQLDatabase db = Skyblock.getInstance().getDatabase();
    private UUID player;
    private UUID ownedIsland;
    private boolean showNearby;
    private boolean creatingIsland = false;

    public PlayerConfig(UUID player) {
        this.player = player;
        this.ownedIsland = null;
        this.showNearby = false;
    }

    public PlayerConfig(UUID player, UUID ownedIsland, boolean showNearby) {
        this.player = player;
        this.ownedIsland = ownedIsland;
        this.showNearby = showNearby;
    }

    public UUID getOwnedIsland() {
        return ownedIsland;
    }

    public void setOwnedIsland(UUID ownedIsland) {
        this.ownedIsland = ownedIsland;
    }

    public boolean getShowNearby() {
        return showNearby;
    }

    public void setShowNearby(boolean showNearby) {
        this.showNearby = showNearby;
    }

    public boolean isCreatingIsland() {
        return creatingIsland;
    }

    public void setCreatingIsland(boolean creatingIsland) {
        this.creatingIsland = creatingIsland;
    }

    public void save() {
        // Save playerconfig to the database
        try {
            PreparedStatement statement = db.getConnection().prepareStatement(
                    "INSERT INTO skyblock_players (uuid, owned_island, showNearby) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE uuid = VALUES(uuid), owned_island = VALUES(owned_island), showNearby = VALUES(showNearby);"
            );

            statement.setString(1, player.toString());
            statement.setString(2, ownedIsland != null ? ownedIsland.toString() : null);
            statement.setString(3, showNearby ? "1" : "0");
            statement.executeUpdate();
            statement.close();
        } catch(SQLException e) {
            Skyblock.getInstance().getLogger().severe("Failed to save player config for " + Bukkit.getOfflinePlayer(player).getName() + " to the database.");
            e.printStackTrace();
            return;
        }
    }

}
