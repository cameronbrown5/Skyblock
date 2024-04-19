package me.thecamzone.island;

import java.sql.Date;
import java.util.*;

public class Island {

    private final UUID id;
    private UUID owner;
    private List<UUID> players;
    private String name;
    private double balance;
    private final Date created;

    private final List<UUID> invitedPlayers = new ArrayList<>();

    public Island(UUID id, UUID owner, String name, double balance, Date created) {
        this.id = id;
        this.owner = owner;
        this.players = new ArrayList<>(Collections.singletonList(owner));
        this.name = name;
        this.balance = balance;
        this.created = created;
    }

    public Island(UUID id, UUID owner, List<UUID> players, String name, double balance, Date created) {
        this.id = id;
        this.owner = owner;
        this.players = players;
        this.name = name;
        this.balance = balance;
        this.created = created;
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

    public Date getCreated() {
        return created;
    }

    public List<UUID> getInvitedPlayers() {
        return invitedPlayers;
    }

    public void addPlayerInvite(UUID player) {
        invitedPlayers.add(player);
    }

    public void removePlayerInvite(UUID player) {
        invitedPlayers.remove(player);
    }

    public boolean acceptInvite(UUID player) {
        if(!invitedPlayers.contains(player)) return false;

        invitedPlayers.remove(player);
        players.add(player);
        return true;
    }

    public boolean addPlayer(UUID player) {
        if(!invitedPlayers.contains(player)) return false;

        players.add(player);
        return true;
    }

    public boolean removePlayer(UUID player) {
        if(!players.contains(player)) return false;

        players.remove(player);
        return true;
    }
}
