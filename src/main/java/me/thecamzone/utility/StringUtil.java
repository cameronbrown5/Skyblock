package me.thecamzone.utility;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class StringUtil {

    private static final java.util.Random RANDOM = new java.util.Random();

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static Location stringToLocation(String string) {
        String[] parts = string.split(",");

        return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
        );
    }

    public static char generateRandomSymbol() {
        String symbols = "@#$%&=+?^~";
        return symbols.charAt(RANDOM.nextInt(symbols.length()));
    }

    public static String generateRandomColor() {
        // Generate a random integer between 0x100000 (dark gray) and 0xFFFFFF (white)
        int color = RANDOM.nextInt(0xFFFFFF - 0x100000) + 0x100000;
        // Convert the color to a hex string
        return String.format("#%06X", color);
    }

    public static ChatColor hexToChatColor(String hexColor) {
        return ChatColor.of(hexColor);
    }

    public static String generateRandomIslandSymbol() {
        ChatColor color = hexToChatColor(generateRandomColor());
        return color + String.valueOf(generateRandomSymbol());
    }

}
