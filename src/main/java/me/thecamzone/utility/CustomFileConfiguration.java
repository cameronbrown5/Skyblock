package me.thecamzone.utility;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomFileConfiguration {

    private File file;
    private FileConfiguration config;
    private JavaPlugin plugin;
    private String fileName;

    public CustomFileConfiguration(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        createFile();
    }

    private void createFile() {
        file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }

        config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Could not load config file " + fileName);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config file " + fileName);
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}
