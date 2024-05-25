package me.thecamzone.island;

import me.thecamzone.Skyblock;
import me.thecamzone.utility.StringUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.SQLException;

public class IslandProtectionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isPlayerAuthorized(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(StringUtil.color("&cYou are not authorized to break blocks here."));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isPlayerAuthorized(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(StringUtil.color("&cYou are not authorized to place blocks here."));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && !isPlayerAuthorized(event.getPlayer(), event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> !isEntityAuthorized(event.getEntity(), block.getLocation()));
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!isEntityAuthorized(event.getEntity(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    private boolean isPlayerAuthorized(Player player, Location location) {
        if(player.hasPermission("skyblock.bypass")) {
            return true;
        }

        try {
            if(Skyblock.getInstance().getIslandManager().getIslandByChunk(location.getChunk()) == null) {
                return true;
            }

            return Skyblock.getInstance().getIslandManager().getIslandByChunk(location.getChunk()).getPlayers().contains(player.getUniqueId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isEntityAuthorized(Entity entity, Location location) {
        // Implement your logic to check if the entity is authorized to modify the island at the given location
        return true;
    }
}
