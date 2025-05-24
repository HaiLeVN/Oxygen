package org.haile.oxygen;

import org.haile.oxygen.commands.OxygenCommands;
import org.haile.oxygen.listeners.PlayerOxygenEventListener;
import org.haile.oxygen.listeners.PluginReloadListener;
import org.haile.oxygen.managers.BossBarManager;
import org.haile.oxygen.managers.NotificationManager;
import org.haile.oxygen.managers.OxygenDataManager;
import org.haile.oxygen.managers.OxygenPlayerManager;
import org.haile.oxygen.managers.RegionChecker;
import org.haile.oxygen.models.ConfigSettings;
import org.haile.oxygen.tasks.OxygenConsumerTasks;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Oxygen extends JavaPlugin {

    private OxygenPlayerManager oxygenManager;
    private RegionChecker regionChecker;
    private BossBarManager bossBarManager;
    private OxygenDataManager dataManager;
    private NotificationManager notificationManager;
    private OxygenConsumerTasks oxygenTask;
    private ConfigSettings configSettings;
    private FileConfiguration config;
    private int taskId = -1;

    @Override
    public void onEnable() {
        try {
            // Load configuration
            loadConfiguration();

            // Initialize ConfigSettings và load từ config
            configSettings = new ConfigSettings();
            configSettings.loadFromConfig(getConfig(), getLogger());

            // Initialize managers với ConfigSettings
            getLogger().info("Initializing managers...");
            bossBarManager = new BossBarManager(this, configSettings);
            dataManager = new OxygenDataManager(this);
            notificationManager = new NotificationManager(this, configSettings);
            oxygenManager = new OxygenPlayerManager(bossBarManager, dataManager, configSettings);
            regionChecker = new RegionChecker(this);

            // Register commands and tab completer
            OxygenCommands oxygenCommands = new OxygenCommands(this, oxygenManager);
            getCommand("oxygen").setExecutor(oxygenCommands);
            getCommand("oxygen").setTabCompleter(new org.haile.oxygen.commands.OxygenTabCompleter(this, oxygenManager));

            // Register listeners
            getServer().getPluginManager().registerEvents(
                    new PlayerOxygenEventListener(oxygenManager, bossBarManager), this);
            getServer().getPluginManager().registerEvents(
                    new PluginReloadListener(this), this);

            // Start oxygen check task
            startOxygenTask();

            getLogger().info("Oxygen plugin has been enabled successfully!");
        } catch (Exception e) {
            getLogger().severe("Error enabling Oxygen: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Starts the oxygen consumption task
     */
    private void startOxygenTask() {
        // Cancel existing task if running
        if (taskId != -1) {
            getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        // Get decrease rate from ConfigSettings
        int decreaseRate = configSettings.getDecreaseRate();

        // Create new task với ConfigSettings
        oxygenTask = new OxygenConsumerTasks(this, oxygenManager, regionChecker, notificationManager, configSettings);
        taskId = oxygenTask.runTaskTimer(this, 0L, decreaseRate).getTaskId();

        getLogger().info("Started oxygen task with rate: " + decreaseRate + " ticks");
    }

    @Override
    public void onDisable() {
        // Cancel running task
        if (taskId != -1) {
            getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        // Save player data on server shutdown
        if (dataManager != null) {
            dataManager.saveData();
        }

        // Remove all boss bars
        if (bossBarManager != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                bossBarManager.removeBossBar(player);
            }
        }

        getLogger().info("Oxygen plugin has been disabled.");
    }

    /**
     * Reloads the plugin configuration and restarts tasks
     */
    public void reloadPlugin() {
        getLogger().info("Starting plugin reload...");

        // Reload configuration
        reloadConfig();
        config = getConfig();

        // **FIX: Reload ConfigSettings FIRST before updating managers**
        if (configSettings != null) {
            configSettings.loadFromConfig(config, getLogger());
            getLogger().info("Reloaded ConfigSettings with new values");
        }

        // Reload managers with updated ConfigSettings
        if (dataManager != null) {
            dataManager.reloadData();
        }

        if (notificationManager != null) {
            notificationManager.loadConfiguration();
        }

        if (bossBarManager != null) {
            bossBarManager.loadConfiguration();
        }

        if (oxygenManager != null) {
            oxygenManager.loadConfiguration();
        }

        // **FIX: Update all boss bars AFTER managers are reloaded**
        if (bossBarManager != null) {
            // Remove all existing boss bars first
            for (Player player : getServer().getOnlinePlayers()) {
                bossBarManager.removeBossBar(player);
            }

            // Recreate boss bars with new configuration
            for (Player player : getServer().getOnlinePlayers()) {
                bossBarManager.createBossBar(player);
                if (oxygenManager != null) {
                    int currentOxygen = oxygenManager.getOxygen(player);
                    bossBarManager.updateBossBar(player, currentOxygen);
                }
            }
        }

        // Restart oxygen task with new configuration
        if (oxygenTask != null) {
            oxygenTask.loadConfiguration();
            startOxygenTask();
        }

        getLogger().info("Oxygen plugin configuration reloaded successfully");
    }

    /**
     * Loads or creates the configuration file
     */
    private void loadConfiguration() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
            getLogger().info("Created plugin data folder");
        }

        if (!configFile.exists()) {
            saveDefaultConfig();
            getLogger().info("Created default configuration file");
        } else {
            reloadConfig();
            getLogger().info("Loaded existing configuration file");
        }

        config = getConfig();

        setupDefaultConfig();
    }

    /**
     * Sets up default configuration values if they don't exist
     */
    private void setupDefaultConfig() {
        // Oxygen settings
        if (!config.contains("oxygen.max-level")) {
            config.set("oxygen.max-level", 100);
        }

        if (!config.contains("oxygen.decrease-rate")) {
            config.set("oxygen.decrease-rate", 100);
        }

        if (!config.contains("oxygen.damage")) {
            config.set("oxygen.damage", 50);
        }

        if (!config.contains("oxygen.gamemodes")) {
            config.set("oxygen.gamemodes", new String[]{"SURVIVAL", "ADVENTURE"});
        }

        // **ADD: Default thresholds if they don't exist**
        if (!config.contains("display.bossbar.thresholds.medium")) {
            config.set("display.bossbar.thresholds.medium", 60);
        }

        if (!config.contains("display.bossbar.thresholds.low")) {
            config.set("display.bossbar.thresholds.low", 30);
        }

        // Storage settings
        if (!config.contains("storage.type")) {
            config.set("storage.type", "file");
        }

        saveConfig();
    }

    // Getters for managers
    public OxygenPlayerManager getOxygenManager() {
        return oxygenManager;
    }

    public RegionChecker getRegionChecker() {
        return regionChecker;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public OxygenDataManager getDataManager() {
        return dataManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public ConfigSettings getConfigSettings() {
        return configSettings;
    }
}