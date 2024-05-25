package me.thecamzone.island;

import me.thecamzone.Skyblock;
import me.thecamzone.chunks.ChunkCoordinates;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class Island {

    private final UUID id;
    private UUID owner;
    private List<UUID> players;
    private String name;
    private double balance;
    private final Date created;
    private Location spawn;
    private final String symbol;
    private HashSet<ChunkCoordinates> claimedChunks = null;
    private final int maxClaimedChunks = 20;

    public Island(UUID id, UUID owner, String name, double balance, Date created, Location spawn, String symbol) {
        this.id = id;
        this.owner = owner;
        this.players = new ArrayList<>(Collections.singletonList(owner));
        this.name = name;
        this.balance = balance;
        this.spawn = spawn;
        this.created = created;
        this.symbol = symbol;
        try {
            this.claimedChunks = getClaimedChunks();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void setPlayers(List<UUID> players) {
        this.players = players;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public HashSet<ChunkCoordinates> getClaimedChunks() throws SQLException {
        if(claimedChunks != null) return claimedChunks;

        HashSet<ChunkCoordinates> chunkCoordinatesList = new HashSet<>();

        Connection connection = Skyblock.getInstance().getDatabase().getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT chunkX, chunkZ, world FROM skyblock_island_claims WHERE island_uuid = '" + getId().toString() + "'");

        while (resultSet.next()) {
            int x = resultSet.getInt("chunkX");
            int z = resultSet.getInt("chunkZ");
            String worldName = resultSet.getString("world");
            ChunkCoordinates chunkCoordinates = new ChunkCoordinates(Bukkit.getWorld(worldName), x, z);
            chunkCoordinatesList.add(chunkCoordinates);
        }

        resultSet.close();
        statement.close();

        claimedChunks = chunkCoordinatesList;

        return chunkCoordinatesList;
    }

    public boolean addClaimedChunk(Chunk chunk) {
        ChunkCoordinates chunkCoordinates = new ChunkCoordinates(chunk.getWorld(), chunk.getX(), chunk.getZ());

        if(claimedChunks.size() >= maxClaimedChunks) return false;

        if(claimedChunks.contains(chunkCoordinates)) return false;

        claimedChunks.add(chunkCoordinates);
        chunk.getPersistentDataContainer().set(Skyblock.getInstance().getChunkClaimedKey(), PersistentDataType.STRING, getId().toString());
        return true;
    }

    public boolean removeClaimedChunk(Chunk chunk) {
        ChunkCoordinates chunkCoordinates = new ChunkCoordinates(chunk.getWorld(), chunk.getX(), chunk.getZ());

        if(!claimedChunks.contains(chunkCoordinates)) return false;

        claimedChunks.remove(chunkCoordinates);
        chunk.getPersistentDataContainer().remove(Skyblock.getInstance().getChunkClaimedKey());
        return true;
    }

    public void deleteChunks() {
        claimedChunks.forEach(chunk -> {
            World world = chunk.getWorld();
            Chunk bukkitChunk = chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ());
            bukkitChunk.getPersistentDataContainer().remove(Skyblock.getInstance().getChunkClaimedKey());
            world.regenerateChunk(chunk.getX(), chunk.getZ());
        });
        claimedChunks.clear();
    }

    public int getMaxChunks() {
        return maxClaimedChunks;
    }

    public Date getCreated() {
        return created;
    }

    public boolean addPlayer(UUID player) {
        if(players.contains(player)) return false;

        players.add(player);
        return true;
    }

    public boolean removePlayer(UUID player) {
        if(!players.contains(player)) return false;

        players.remove(player);
        return true;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public String getSymbol() {
        return symbol;
    }
}
