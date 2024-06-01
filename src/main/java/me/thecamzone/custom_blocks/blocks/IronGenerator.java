package me.thecamzone.custom_blocks.blocks;

import me.thecamzone.Skyblock;
import me.thecamzone.custom_blocks.CustomBlock;
import me.thecamzone.custom_blocks.types.Generator;
import me.thecamzone.utility.StringUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class IronGenerator extends Generator  {

    public IronGenerator(JavaPlugin plugin, Material material, int level) {
        super(plugin, material, level);
    }

    public IronGenerator(String[] parameters) {
        super(JavaPlugin.getPlugin(Skyblock.class), Material.valueOf(parameters[0]), Integer.parseInt(parameters[1]));
        this.setChance(Double.parseDouble(parameters[2]));
        this.setLocation(StringUtil.stringToLocation(parameters[3]));
    }

    @Override
    public ItemStack getCustomItem() {
        ItemStack item = new ItemStack(Material.IRON_BLOCK); // Your custom item type
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(StringUtil.color("&f&lIron Generator")); // Your custom item name
        meta.setLore(StringUtil.color(
            "&7Has a chance to generate iron in",
            "&7nearby cobblestone generators",
            " ",
            "&aLevel: &f" + getLevel(),
            "&aChance: &f" + calculateChance(getLevel()) + "%"
        ));
        meta.getPersistentDataContainer().set(new NamespacedKey(Skyblock.getInstance(), "serialized_custom_block"), PersistentDataType.STRING, toString());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void placeBlock(Location location) {
        Skyblock.getInstance().getCustomBlockManager().getPlacedCustomBlocks().put(location, this);
        setLocation(location);
        createHologram();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("iron_generator{");
        sb.append("material=").append(getMaterial());
        sb.append(", level=").append(getLevel());
        sb.append(", chance=").append(getChance());
        sb.append(", location=").append(StringUtil.locationToString(getLocation()));
        sb.append("}");

        return sb.toString();
    }
}
