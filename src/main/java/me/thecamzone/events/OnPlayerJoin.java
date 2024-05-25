package me.thecamzone.events;

import me.thecamzone.player.PlayerConfig;
import me.thecamzone.player.PlayerConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerConfig playerConfig = PlayerConfigManager.getInstance().getPlayerConfig(event.getPlayer().getUniqueId());
        if(playerConfig == null) {
            PlayerConfigManager.getInstance().loadPlayerConfig(event.getPlayer().getUniqueId());
        }
    }

}
