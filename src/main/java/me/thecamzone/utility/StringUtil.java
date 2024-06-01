package me.thecamzone.utility;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    private static final java.util.Random RANDOM = new java.util.Random();

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> color(String ...messages) {
        List<String> builder = new ArrayList<>();
        for (int i = 0; i < messages.length; i++) {
            messages[i] = ChatColor.translateAlternateColorCodes('&', messages[i]);
            builder.add(messages[i]);
        }
        return builder;
    }

    public static List<String> color(List<String> messages) {
        List<String> builder = new ArrayList<>();
        for (String message : messages) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            builder.add(message);
        }
        return builder;
    }

    public static String renameMinecraftId(String minecraftId) {
        // Split the string by underscores and capitalize each word
        String[] words = minecraftId.toLowerCase().split("_");
        StringBuilder friendlyName = new StringBuilder();

        for (String word : words) {
            if (friendlyName.length() > 0) {
                friendlyName.append(" ");
            }
            friendlyName.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }

        return friendlyName.toString();
    }

    public static String locationToString(Location location) {
        if(location == null) return "null";

        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static String locationToStringReadable(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static Location stringToLocation(String string) {
        if(string.equalsIgnoreCase("null")) {
            return null;
        }

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
