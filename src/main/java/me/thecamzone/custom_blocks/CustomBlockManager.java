package me.thecamzone.custom_blocks;

import me.thecamzone.Skyblock;
import me.thecamzone.custom_blocks.blocks.IronGenerator;
import me.thecamzone.custom_blocks.types.Generator;
import me.thecamzone.utility.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CustomBlockManager {

    private final HashMap<Location, CustomBlock> placedCustomBlocks = new HashMap<>();
    private final HashMap<String, CustomBlock> availableCustomBlocks = new HashMap<>();
    private final NamespacedKey customBlockKey = new NamespacedKey(Skyblock.getInstance(), "serialized_custom_block");

    {
        load();
    }

    public NamespacedKey getCustomBlockKey() {
        return customBlockKey;
    }

    public CustomBlock getCustomBlockFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        NamespacedKey key = new NamespacedKey(Skyblock.getInstance(), "serialized_custom_block");
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (!container.has(key, PersistentDataType.STRING)) {
            return null;
        }

        String customBlockType = container.get(key, PersistentDataType.STRING);
        if (customBlockType == null) {
            return null;
        }

        // Split the string into parts
        String[] parts = customBlockType.split("\\{");
        // The first part should be the class name
        String className = parts[0];

        CustomBlock customBlock = getCustomBlock(className);
        if (customBlock == null) {
            return null;
        }

        //iron_generator{material=IRON_BLOCK, level=1, chance=0.1, location=world,0,0,0}
        if(customBlock instanceof Generator generator) {
            // Extract properties from the string
            String[] properties = parts[1].substring(0, parts[1].length() - 1).split(", ");
            Map<String, String> propertyMap = new HashMap<>();
            for (String property : properties) {
                String[] keyValue = property.split("=");
                propertyMap.put(keyValue[0], keyValue[1]);
            }

            // Set properties of the generator
            generator.setMaterial(Material.valueOf(propertyMap.get("material")));
            generator.setLevel(Integer.parseInt(propertyMap.get("level")));
            generator.setChance(Double.parseDouble(propertyMap.get("chance")));
            generator.setLocation(StringUtil.stringToLocation(propertyMap.get("location")));

            return generator;
        }

        return customBlock;
    }

    public Map<Location, CustomBlock> getPlacedCustomBlocks() {
        return placedCustomBlocks;
    }

    public void removePlacedCustomBlock(Location location) {
        FileConfiguration config = Skyblock.getInstance().getGeneratorsFile().getConfig();
        config.set("custom_blocks." + StringUtil.locationToString(location), null);
        placedCustomBlocks.remove(location);
        Skyblock.getInstance().getGeneratorsFile().saveConfig();
    }

    public void addAvailableCustomBlock(String name, CustomBlock customBlock) {
        availableCustomBlocks.put(name, customBlock);
    }

    public CustomBlock getCustomBlock(String name) {
        return availableCustomBlocks.get(name);
    }

    public ItemStack getCustomItem(String item) {
        CustomBlock customBlock = availableCustomBlocks.get(item);
        if (customBlock != null) {
            return customBlock.getCustomItem();
        }
        return null;
    }

    public void save() {
        for (Location location : placedCustomBlocks.keySet()) {
            CustomBlock customBlock = placedCustomBlocks.get(location);

            FileConfiguration config = Skyblock.getInstance().getGeneratorsFile().getConfig();

            if(customBlock instanceof Generator generator) {
                if(generator instanceof IronGenerator ironGenerator) {
                    config.set("custom_blocks." + StringUtil.locationToString(location), ironGenerator.toString());
                    continue;
                }

                config.set("custom_blocks." + StringUtil.locationToString(location), generator.toString());
            }
        }

        Skyblock.getInstance().getGeneratorsFile().saveConfig();
    }

    public void load() {
        Skyblock.getInstance().getLogger().info("Loading custom blocks");

        FileConfiguration config = Skyblock.getInstance().getGeneratorsFile().getConfig();

        if(!config.contains("custom_blocks")) {
            Skyblock.getInstance().getLogger().info("No custom blocks found in storage file");
            return;
        }

        for (String key : config.getConfigurationSection("custom_blocks").getKeys(false)) {
            if(config.getString(key) == null) {
                Skyblock.getInstance().getLogger().info("custom_blocks does not exist in storage file.");
                continue;
            }

            placedCustomBlocks.put(StringUtil.stringToLocation(key), getCustomBlock(config.getString("custom_blocks." + key)));

            Skyblock.getInstance().getLogger().info("Loaded custom block at " + key);
        }

        Skyblock.getInstance().getLogger().info("Loaded " + placedCustomBlocks.size() + " custom blocks");
    }

}
