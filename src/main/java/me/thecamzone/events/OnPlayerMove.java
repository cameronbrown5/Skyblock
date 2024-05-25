package me.thecamzone.events;

import me.thecamzone.Skyblock;
import me.thecamzone.island.Island;
import me.thecamzone.island.IslandManager;
import me.thecamzone.player.PlayerConfigManager;
import me.thecamzone.utility.MapUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.sql.SQLException;

public class OnPlayerMove implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(PlayerConfigManager.getInstance().getPlayerConfig(event.getPlayer().getUniqueId()).getShowNearby()) {
            Chunk fromChunk = event.getFrom().getChunk();
            Chunk toChunk = event.getTo().getChunk();

            if (!fromChunk.equals(toChunk)) {
                MapUtil.showMap(event.getPlayer(), toChunk);
            }
        }

        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();

        if (!fromChunk.equals(toChunk)) {
            try {
                Island island = Skyblock.getInstance().getIslandManager().getIslandByChunk(toChunk);
                Island fromChunkIsland = Skyblock.getInstance().getIslandManager().getIslandByChunk(fromChunk);
                if (island != null) {
                    event.getPlayer().sendActionBar(Component.text(ChatColor.GREEN + "You are entering the " + island.getName() + " island!"));
                }

                if(fromChunkIsland != null && island == null) {
                    event.getPlayer().sendActionBar(Component.text(ChatColor.RED + "You have entered the wilderness!"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
