package org.haile.oxygen.models;

import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.haile.oxygen.utils.ConfigUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Class to store all plugin configuration settings
 */
public class ConfigSettings {
    // Oxygen settings
    private int maxOxygenLevel;
    private int decreaseRate;
    private int damageAmount;
    private Set<GameMode> allowedGameModes;
    private Set<String> allowedWorlds;
    private boolean worldsAreWhitelist;

    // BossBar settings
    private String bossBarTitleFormat;
    private BarColor highColor;
    private BarColor mediumColor;
    private BarColor lowColor;
    private BarStyle barStyle;
    private int mediumThreshold;
    private int lowThreshold;

    // Notification settings
    private boolean notificationsEnabled;

    // Storage settings
    private String storageType;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;
    private String mysqlTablePrefix;

    /**
     * Constructor with defaults
     */
    public ConfigSettings() {
        // Set defaults
        this.maxOxygenLevel = 100;
        this.decreaseRate = 100;
        this.damageAmount = 20;

        this.allowedGameModes = new HashSet<>();
        this.allowedGameModes.add(GameMode.SURVIVAL);
        this.allowedGameModes.add(GameMode.ADVENTURE);

        this.allowedWorlds = new HashSet<>();
        this.worldsAreWhitelist = true;

        // Updated default to use MiniMessage format
        this.bossBarTitleFormat = "<white><bold>Oxygen: <#3498db>{oxygen}</#3498db></bold></white><white>/<#3498db>{max}</#3498db></white>";
        this.highColor = BarColor.GREEN;
        this.mediumColor = BarColor.YELLOW;
        this.lowColor = BarColor.RED;
        this.barStyle = BarStyle.SOLID;
        this.mediumThreshold = 60;
        this.lowThreshold = 30;

        this.notificationsEnabled = true;

        this.storageType = "file";
        this.mysqlHost = "localhost";
        this.mysqlPort = 3306;
        this.mysqlDatabase = "minecraft";
        this.mysqlUsername = "root";
        this.mysqlPassword = "password";
        this.mysqlTablePrefix = "oxygen_";
    }

    /**
     * Load settings from configuration
     *
     * @param config Configuration to load from
     * @param logger Logger for warnings
     */
    public void loadFromConfig(FileConfiguration config, Logger logger) {
        // Oxygen settings
        this.maxOxygenLevel = config.getInt("oxygen.max-level", 100);
        this.decreaseRate = config.getInt("oxygen.decrease-rate", 100);
        this.damageAmount = config.getInt("oxygen.damage", 20);

        // Load allowed gamemodes
        this.allowedGameModes.clear();
        List<String> gameModes = config.getStringList("oxygen.gamemodes");
        for (String gameMode : gameModes) {
            try {
                GameMode mode = GameMode.valueOf(gameMode.toUpperCase());
                this.allowedGameModes.add(mode);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid GameMode in config: " + gameMode);
            }
        }

        // If no gamemodes are specified, default to SURVIVAL and ADVENTURE
        if (this.allowedGameModes.isEmpty()) {
            this.allowedGameModes.add(GameMode.SURVIVAL);
            this.allowedGameModes.add(GameMode.ADVENTURE);
        }

        // Load allowed worlds
        this.allowedWorlds.clear();
        List<String> worlds = config.getStringList("oxygen.worlds");
        for (String world : worlds) {
            this.allowedWorlds.add(world.toLowerCase());
        }
        this.worldsAreWhitelist = config.getBoolean("oxygen.worlds-whitelist", true);

        // BossBar settings - với default MiniMessage format
        this.bossBarTitleFormat = config.getString("display.bossbar.title",
                "<white><bold>Oxygen: <#3498db>{oxygen}</#3498db></bold></white><white>/<#3498db>{max}</#3498db></white>");

        String highColorStr = config.getString("display.bossbar.colors.high", "GREEN");
        String mediumColorStr = config.getString("display.bossbar.colors.medium", "YELLOW");
        String lowColorStr = config.getString("display.bossbar.colors.low", "RED");

        this.highColor = ConfigUtils.loadBarColor(highColorStr, BarColor.GREEN, logger);
        this.mediumColor = ConfigUtils.loadBarColor(mediumColorStr, BarColor.YELLOW, logger);
        this.lowColor = ConfigUtils.loadBarColor(lowColorStr, BarColor.RED, logger);

        String styleStr = config.getString("display.bossbar.style", "SOLID");
        this.barStyle = ConfigUtils.loadBarStyle(styleStr, BarStyle.SOLID, logger);

        // **FIX: Load thresholds from config instead of hardcoding**
        this.mediumThreshold = config.getInt("display.bossbar.thresholds.medium", 60);
        this.lowThreshold = config.getInt("display.bossbar.thresholds.low", 30);

        // Notification settings
        this.notificationsEnabled = config.getBoolean("notifications.enabled", true);

        // Storage settings
        this.storageType = config.getString("storage.type", "file");

        // MySQL settings
        if (this.storageType.equalsIgnoreCase("mysql")) {
            this.mysqlHost = config.getString("storage.mysql.host", "localhost");
            this.mysqlPort = config.getInt("storage.mysql.port", 3306);
            this.mysqlDatabase = config.getString("storage.mysql.database", "minecraft");
            this.mysqlUsername = config.getString("storage.mysql.username", "root");
            this.mysqlPassword = config.getString("storage.mysql.password", "password");
            this.mysqlTablePrefix = config.getString("storage.mysql.table-prefix", "oxygen_");
        }

        logger.info("Loaded ConfigSettings - Max Oxygen: " + maxOxygenLevel +
                ", Decrease Rate: " + decreaseRate +
                ", Medium Threshold: " + mediumThreshold +
                ", Low Threshold: " + lowThreshold);
    }

    // Getters
    public int getMaxOxygenLevel() {
        return maxOxygenLevel;
    }

    public int getDecreaseRate() {
        return decreaseRate;
    }

    public int getDamageAmount() {
        return damageAmount;
    }

    public Set<GameMode> getAllowedGameModes() {
        return allowedGameModes;
    }

    public Set<String> getAllowedWorlds() {
        return allowedWorlds;
    }

    public boolean isWorldsAreWhitelist() {
        return worldsAreWhitelist;
    }

    public String getBossBarTitleFormat() {
        return bossBarTitleFormat;
    }

    public BarColor getHighColor() {
        return highColor;
    }

    public BarColor getMediumColor() {
        return mediumColor;
    }

    public BarColor getLowColor() {
        return lowColor;
    }

    public BarStyle getBarStyle() {
        return barStyle;
    }

    public int getMediumThreshold() {
        return mediumThreshold;
    }

    public int getLowThreshold() {
        return lowThreshold;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public String getStorageType() {
        return storageType;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public String getMysqlTablePrefix() {
        return mysqlTablePrefix;
    }
}