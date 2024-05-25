package me.thecamzone.events;

import me.thecamzone.ZoneMCUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class OnRespawn implements Listener {
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        player.teleport(ZoneMCUtility.getInstance().getSpawnLocation());
    }
}
