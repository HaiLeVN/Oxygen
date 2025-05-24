package org.haile.oxygen.managers;

import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.haile.oxygen.Oxygen;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class OxygenDataManager {
    private final Oxygen plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public OxygenDataManager(Oxygen plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");

        // Initialize data storage
        loadData();
    }

    /**
     * Gets the plugin instance
     * @return The plugin
     */
    public Oxygen getPlugin() {
        return plugin;
    }

    /**
     * Initializes the data storage
     */
    public void loadData() {
        // Create data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Create data file if it doesn't exist
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                plugin.getLogger().info("Created new playerdata.yml file");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create playerdata.yml", e);
            }
        }

        // Load data config
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        plugin.getLogger().info("Loaded player data from playerdata.yml");
    }

    /**
     * Reloads data from file
     */
    public void reloadData() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        plugin.getLogger().info("Reloaded player data");
    }

    /**
     * Saves oxygen level for a player
     * @param player The player
     * @param oxygenLevel The oxygen level to save
     */
    public void saveOxygenLevel(Player player, int oxygenLevel) {
        UUID uuid = player.getUniqueId();
        dataConfig.set("players." + uuid.toString() + ".oxygen", oxygenLevel);
        dataConfig.set("players." + uuid.toString() + ".name", player.getName());
        saveData();
    }

    /**
     * Loads oxygen level for a player
     * @param player The player
     * @return The oxygen level or 100 if not found
     */
    public int loadOxygenLevel(Player player) {
        UUID uuid = player.getUniqueId();
        return dataConfig.getInt("players." + uuid.toString() + ".oxygen", 100);
    }

    /**
     * Checks if player has saved data
     * @param player The player
     * @return True if player has saved data
     */
    public boolean hasData(Player player) {
        UUID uuid = player.getUniqueId();
        return dataConfig.contains("players." + uuid.toString());
    }

    /**
     * Saves data to file
     */
    public void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data to playerdata.yml", e);
        }
    }
}