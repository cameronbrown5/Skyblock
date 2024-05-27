package me.thecamzone.custom_blocks;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CustomBlock {

    protected final JavaPlugin plugin;
    protected final NamespacedKey customBlockKey;

    public CustomBlock(JavaPlugin plugin) {
        this.plugin = plugin;
        this.customBlockKey = new NamespacedKey(plugin, "custom_block");
    }



    public abstract ItemStack getCustomItem();
    public abstract void placeBlock(Location location);

    public NamespacedKey getCustomBlockKey() {
        return customBlockKey;
    }

    public String toString() {
        return "CustomBlock{}";
    }


}
