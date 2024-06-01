package me.thecamzone.events;

import com.jeff_media.customblockdata.CustomBlockData;
import me.thecamzone.Skyblock;
import me.thecamzone.utility.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class OnPlayerInteract implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();
        NamespacedKey customBlockKey = Skyblock.getInstance().getCustomBlockManager().getCustomBlockKey();

        // Check if the player is left clicking
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Check if the player has a block in their hand
            if (item != null && item.getType().isBlock()) {
                // Try to find the serialized_custom_block key
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    if (container.has(customBlockKey, PersistentDataType.STRING)) {
                        String serializedCustomBlock = container.get(customBlockKey, PersistentDataType.STRING);
                        // Print the serialized_custom_block key in chat
                        event.getPlayer().sendMessage(StringUtil.color("&bItem: " + serializedCustomBlock));
                    } else {
                        event.getPlayer().sendMessage("Item does not have custom block key");
                    }
                } else {
                    event.getPlayer().sendMessage("Item does not have meta");
                }
            }

            // If the player left clicks a block, do the same thing
            else if (block != null) {
                CustomBlockData blockData = new CustomBlockData(block, Skyblock.getInstance());
                if (blockData.has(customBlockKey, PersistentDataType.STRING)) {
                    String serializedCustomBlock = blockData.get(customBlockKey, PersistentDataType.STRING);
                    // Print the serialized_custom_block key in chat
                    event.getPlayer().sendMessage(StringUtil.color("&aBlock: " + serializedCustomBlock));
                } else {
                    event.getPlayer().sendMessage("Block does not have custom block key");
                }
            }
        }
    }

}
