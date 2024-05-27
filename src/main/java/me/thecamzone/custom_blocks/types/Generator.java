package me.thecamzone.custom_blocks.types;

import com.maximfiedler.hologramapi.hologram.TextHologram;
import com.sk89q.worldedit.blocks.Blocks;
import me.thecamzone.Skyblock;
import me.thecamzone.custom_blocks.CustomBlock;
import me.thecamzone.custom_blocks.blocks.IronGenerator;
import me.thecamzone.utility.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public abstract class Generator extends CustomBlock {

    private static final Random random = new Random();

    private Material material;
    private int level;
    private double chance;
    private Location location;
    private TextHologram hologram;

    public Generator(JavaPlugin plugin, Material material, int level) {
        super(plugin);

        this.material = material;
        this.level = level;
        this.chance = calculateChance(level);
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public TextHologram getHologram() {
        return hologram;
    }

    public void setHologram(TextHologram hologram) {
        this.hologram = hologram;
    }

    public Location getHologramLocation() {
        return location.clone().add(0.5, 1.5, 0.5);
    }

    public TextHologram createHologram() {
        TextHologram hologram = new TextHologram("generator_hologram_" + UUID.randomUUID())
                .setText(StringUtil.color("&f&lIron Generator"))
                .addLine(StringUtil.color("&aLevel: &f" + getLevel()))
                .addLine(StringUtil.color("&aChance: &f" + getChance() + "%"))
                .setBillboard(Display.Billboard.VERTICAL)
                .setTextShadow(true)
                .setSize(0.75f, 0.75f, 0.75f)
                .setBackgroundColor(Color.fromARGB(0, 255, 236, 222))
                .spawn(getHologramLocation());

        this.hologram = hologram;
        return hologram;
    }

    public HashSet<Location> getCheckedBlocks() {
        HashSet<Location> blocks = new HashSet<>();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Location block = location.clone().add(x, y, z);
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    public double calculateChance(int level) {
        if (level < 1 || level > 20) {
            throw new IllegalArgumentException("Level must be between 1 and 20");
        }
        double minChance = 10.0;
        double maxChance = 100.0;
        double maxLevel = 20.0;
        double chance = minChance + (maxChance - minChance) * Math.log(level) / Math.log(maxLevel);
        return Math.round(chance * 100.0) / 100.0;
    }

    public boolean rollChance(int level) {
        double chance = calculateChance(level);
        double roll = random.nextDouble() * 100;
        return roll < chance;
    }

    public boolean rollChance() {
        double chance = calculateChance(this.level);
        double roll = random.nextDouble() * 100;
        return roll < chance;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Generator{");
        sb.append("material=").append(getMaterial());
        sb.append(", level=").append(getLevel());
        sb.append(", chance=").append(getChance());
        sb.append(", location=").append(StringUtil.locationToString(getLocation()));
        sb.append("}");

        return sb.toString();
    }

    public static Generator fromString(String s, JavaPlugin plugin) {
        String[] parts = s.split(",");
        Material material = Material.valueOf(parts[1].split("=")[1]);
        int level = Integer.parseInt(parts[2].split("=")[1]);
        Location location = StringUtil.stringToLocation(parts[3].split("=")[1]);
        return new IronGenerator(plugin, material, level);
    }
}
