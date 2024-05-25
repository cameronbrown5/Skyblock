package me.thecamzone.chunks;

import org.bukkit.Chunk;
import org.bukkit.World;

public record ChunkCoordinates(World world, int x, int z) {

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public Chunk getChunk() {
        return world.getChunkAt(x, z);
    }

    @Override
    public String toString() {
        return "ChunkCoordinates{" +
                "x=" + x +
                ", z=" + z +
                '}';
    }
}
