package me.thecamzone.chunks;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChunkSearch {

    private World world;

    public ChunkSearch(World world) {
        this.world = world;
    }

    public boolean isChunkFree(int chunkX, int chunkZ, Connection conn) throws SQLException {
        String worldName = world.getName();

        String query = "SELECT COUNT(*) FROM skyblock_island_claims WHERE world = ? AND chunkX BETWEEN ? - 10 AND ? + 10 AND chunkZ BETWEEN ? - 10 AND ? + 10";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, worldName);
            stmt.setInt(2, chunkX);
            stmt.setInt(3, chunkX);
            stmt.setInt(4, chunkZ);
            stmt.setInt(5, chunkZ);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // Returns true if no claimed chunks are found in the radius
            }
        }
        return false; // Default to false in case of an error
    }

    public ChunkCoordinates findFreeChunk(Connection conn, int worldSize) throws SQLException {
        Random random = new Random();
        int chunkX, chunkZ;
        do {
            chunkX = random.nextInt(worldSize) - worldSize / 2; // Centered around 0,0
            chunkZ = random.nextInt(worldSize) - worldSize / 2;
        } while (!isChunkFree(chunkX, chunkZ, conn));

        return new ChunkCoordinates(world, chunkX, chunkZ);
    }

}
