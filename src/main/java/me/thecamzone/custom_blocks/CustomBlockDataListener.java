package me.thecamzone.custom_blocks;

import com.jeff_media.customblockdata.CustomBlockData;
import com.maximfiedler.hologramapi.hologram.TextHologram;
import me.thecamzone.Skyblock;
import me.thecamzone.custom_blocks.types.Generator;
import me.thecamzone.utility.StringUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class CustomBlockDataListener implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey customBlockKey;

    public CustomBlockDataListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.customBlockKey = new NamespacedKey(plugin, "serialized_custom_block");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();

        CustomBlockData blockData = new CustomBlockData(block, plugin);
        if (!blockData.has(customBlockKey, PersistentDataType.STRING)) return;

        String serializedCustomBlock = blockData.get(customBlockKey, PersistentDataType.STRING);

        CustomBlock customBlock = Skyblock.getInstance().getCustomBlockManager().getPlacedCustomBlocks().get(location);

        if (customBlock == null) {
            return;
        }

        ItemStack item = customBlock.getCustomItem();
        ItemMeta meta = item.getItemMeta();

        if (customBlock instanceof Generator generator) {
            meta.getPersistentDataContainer().set(customBlockKey, PersistentDataType.STRING, serializedCustomBlock);
            item.setItemMeta(meta);
            removeGeneratorHologram(generator);
        }

        event.setDropItems(false);
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            block.getWorld().dropItemNaturally(block.getLocation(), item);
        }

        Skyblock.getInstance().getCustomBlockManager().removePlacedCustomBlock(location);
        Skyblock.getInstance().getCustomBlockManager().save();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Location location = block.getLocation();

        ItemStack item = event.getItemInHand();
        if (!item.hasItemMeta()) {
            Skyblock.getInstance().getLogger().info("Item does not have meta");
            return;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (!container.has(customBlockKey, PersistentDataType.STRING)) {
            Skyblock.getInstance().getLogger().info("Item does not have custom block key");
            return;
        }

        CustomBlock customBlock = Skyblock.getInstance().getCustomBlockManager().getCustomBlockFromItem(item);
        if (customBlock == null) {
            Skyblock.getInstance().getLogger().info("Custom block is null");
            return;
        }

        CustomBlockData blockData = new CustomBlockData(block, plugin);
        String serializedCustomBlock = container.get(customBlockKey, PersistentDataType.STRING);
        if(serializedCustomBlock == null) {
            Skyblock.getInstance().getLogger().info("Serialized custom block is null");
            return;
        }
        blockData.set(customBlockKey, PersistentDataType.STRING, serializedCustomBlock);

        customBlock.placeBlock(event.getBlockPlaced().getLocation());
        Skyblock.getInstance().getLogger().info("Generator placed");

        Skyblock.getInstance().getCustomBlockManager().save();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();
        if (block == null || item == null) return;

        Location location = block.getLocation();

        CustomBlockData blockData = new CustomBlockData(block, plugin);
        if (!blockData.has(customBlockKey, PersistentDataType.STRING)) return;

        CustomBlock customBlock = Skyblock.getInstance().getCustomBlockManager().getPlacedCustomBlocks().get(location);
        if (customBlock instanceof Generator generator) {
            handleGeneratorInteraction(event, item, generator);
        }
    }

    private void removeGeneratorHologram(Generator generator) {
        try {
            generator.getHologram().kill();
        } catch (NullPointerException e) {
            generator.getHologramLocation().getNearbyEntities(0.1, 0.1, 0.1).forEach(Entity::remove);
            Skyblock.getInstance().getLogger().info("Entity is null. Trying to remove nearby entities.");
        }
    }

    private void handleGeneratorInteraction(PlayerInteractEvent event, ItemStack item, Generator generator) {
        if (item.getType() != generator.getCustomItem().getType() || item.hasItemMeta()) return;

        int currentLevel = generator.getLevel();
        if (currentLevel >= 20) return;

        generator.setLevel(currentLevel + 1);
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        updateGeneratorHologram(generator);
        Skyblock.getInstance().getCustomBlockManager().save();

        event.setCancelled(true);
    }

    private void updateGeneratorHologram(Generator generator) {
        generator.getHologram()
                .setText(StringUtil.color("&f&lIron Generator"))
                .addLine(StringUtil.color("&aLevel: &f" + generator.getLevel()))
                .addLine(StringUtil.color("&aChance: &f" + generator.calculateChance(generator.getLevel()) + "%"))
                .update();
    }
}
