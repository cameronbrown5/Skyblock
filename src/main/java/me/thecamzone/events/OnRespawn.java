package me.thecamzone.events;

import me.thecamzone.Skyblock;
import me.thecamzone.ZoneMCUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class OnRespawn implements Listener {
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().scheduleSyncDelayedTask(Skyblock.getInstance(), () -> {
            player.teleport(ZoneMCUtility.getInstance().getSpawnLocation());
        }, 1L);
    }
}
