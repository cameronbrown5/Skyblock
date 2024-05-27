package me.thecamzone.events;

import me.thecamzone.Skyblock;
import me.thecamzone.custom_blocks.CustomBlock;
import me.thecamzone.custom_blocks.types.Generator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;

public class OnBlockForm implements Listener {
    @EventHandler
    public void onBlockForm(BlockFromToEvent event) {
        if(!(event.getBlock().getType() == Material.LAVA && event.getToBlock().getType() == Material.AIR)) {
            return;
        }

        for (CustomBlock customBlock : Skyblock.getInstance().getCustomBlockManager().getPlacedCustomBlocks().values()) {
            if (customBlock instanceof Generator generator) {
                if(generator.getCheckedBlocks() == null) {
                    return;
                }

                if (generator.getCheckedBlocks().contains(event.getBlock().getLocation())) {
                    if(generator.rollChance()) {
                        event.setCancelled(true);
                        event.getToBlock().setType(generator.getMaterial());
                    }
                }
            }
        }
    }

}
