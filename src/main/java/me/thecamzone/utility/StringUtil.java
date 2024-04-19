package me.thecamzone.utility;

import org.bukkit.ChatColor;

public class StringUtil {

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
