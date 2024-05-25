package me.thecamzone.utility;

import me.thecamzone.Skyblock;
import me.thecamzone.chunks.ChunkCoordinates;
import me.thecamzone.island.Island;
import me.thecamzone.island.IslandManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MapUtil {

    public static void showMap(Player player, Chunk playerChunk) {
        IslandManager islandManager = Skyblock.getInstance().getIslandManager();

        // Define the size of the map
        int mapWidth = 30;
        int mapHeight = 8;

        StringBuilder map = new StringBuilder();
        HashMap<String, String> islandLegend = new HashMap<>(); // Store unique islands and their symbols

        // Create a HashMap to store chunks and their corresponding islands
        HashMap<String, Island> chunkIslandMap = new HashMap<>();

        // Query all islands from the database and store them in the HashMap
        for (Island island : islandManager.getAllIslands()) {
            try {
                for (ChunkCoordinates chunk : island.getClaimedChunks()) {
                    chunkIslandMap.put(chunk.getX() + "," + chunk.getZ(), island);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        // Calculate the half sizes of the map
        int halfMapWidth = mapWidth / 2;
        int halfMapHeight = mapHeight / 2;

        player.sendMessage("");

        for (int x = -halfMapHeight; x <= halfMapHeight; x++) {
            for (int z = -halfMapWidth; z <= halfMapWidth; z++) {
                // Calculate the chunk coordinates around the player's chunk
                int chunkX = playerChunk.getX() + x;
                int chunkZ = playerChunk.getZ() + z;

                // Get the island from the HashMap
                Island island = chunkIslandMap.get(chunkX + "," + chunkZ);

                if (x == 0 && z == 0) {
                    // This is the player's current position
                    map.append(ChatColor.AQUA + "+");
                } else if (island != null) {
                    // Use the island's unique symbol and color
                    map.append(island.getSymbol());
                    islandLegend.put(island.getName(), island.getSymbol()); // Add to legend
                } else {
                    // Use a default symbol for unclaimed chunks
                    map.append(ChatColor.DARK_GRAY + "/");
                }
            }
            map.append("\n");
        }

        // Add a legend at the bottom
        StringBuilder legend = new StringBuilder();
        legend.append(ChatColor.AQUA + "+" + ChatColor.GRAY + " You, ");
        for (Map.Entry<String, String> entry : islandLegend.entrySet()) {
            legend.append(entry.getValue()).append(ChatColor.GRAY + " ").append(entry.getKey()).append(", ");
        }

        // Remove the trailing comma and space
        if (legend.length() > 0) {
            legend.setLength(legend.length() - 2); // remove last comma and space
        }

        player.sendMessage(String.valueOf(legend));
        player.sendMessage(map.toString());
    }

}
