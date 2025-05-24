package org.haile.oxygen.utils;

import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.haile.oxygen.models.WarningLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class for handling configuration operations
 * Updated to support custom sounds from resource packs
 */
public class ConfigUtils {
    /**
     * Loads BarColor from configuration
     *
     * @param colorName Color name from config
     * @param defaultColor Default color if invalid
     * @param logger Logger for warnings
     * @return Loaded BarColor
     */
    public static BarColor loadBarColor(String colorName, BarColor defaultColor, Logger logger) {
        try {
            return BarColor.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            if (logger != null) {
                logger.warning("Invalid BossBar color in config: " + colorName + ", using default");
            }
            return defaultColor;
        }
    }

    /**
     * Loads BarStyle from configuration
     *
     * @param styleName Style name from config
     * @param defaultStyle Default style if invalid
     * @param logger Logger for warnings
     * @return Loaded BarStyle
     */
    public static BarStyle loadBarStyle(String styleName, BarStyle defaultStyle, Logger logger) {
        try {
            return BarStyle.valueOf(styleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            if (logger != null) {
                logger.warning("Invalid BossBar style in config: " + styleName + ", using default");
            }
            return defaultStyle;
        }
    }

    /**
     * Loads Sound from configuration (built-in Bukkit sounds only)
     *
     * @param soundName Sound name from config
     * @param logger Logger for warnings
     * @return Loaded Sound or null if invalid
     */
    public static Sound loadSound(String soundName, Logger logger) {
        try {
            return Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            if (logger != null) {
                logger.warning("Invalid built-in sound in config: " + soundName);
            }
            return null;
        }
    }

    /**
     * Checks if a sound name is a valid built-in Bukkit sound
     *
     * @param soundName Sound name to check
     * @return true if it's a valid built-in sound
     */
    public static boolean isBuiltInSound(String soundName) {
        try {
            Sound.valueOf(soundName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Loads warning levels from configuration with custom sound support
     *
     * @param config Plugin configuration
     * @param logger Logger for warnings
     * @return Map of warning levels
     */
    public static Map<String, WarningLevel> loadWarningLevels(FileConfiguration config, Logger logger) {
        Map<String, WarningLevel> warningLevels = new HashMap<>();

        // Check if notifications are enabled
        boolean notificationsEnabled = config.getBoolean("notifications.enabled", true);
        if (!notificationsEnabled) {
            return warningLevels;
        }

        // Load warning levels
        ConfigurationSection warningsSection = config.getConfigurationSection("notifications.warnings");
        if (warningsSection == null) {
            if (logger != null) {
                logger.warning("No warning levels configured in config.yml");
            }
            return warningLevels;
        }

        for (String key : warningsSection.getKeys(false)) {
            ConfigurationSection levelSection = warningsSection.getConfigurationSection(key);
            if (levelSection == null) continue;

            boolean enabled = levelSection.getBoolean("enabled", true);
            if (!enabled) continue;

            int oxygenLevel = levelSection.getInt("oxygen-level", 0);
            String subtitle = levelSection.getString("subtitle", "&cWarning: Low oxygen!");

            // Sound settings
            boolean soundEnabled = levelSection.getBoolean("sound.enabled", false);
            String soundName = levelSection.getString("sound.name", "ENTITY_PLAYER_LEVELUP");
            float volume = (float) levelSection.getDouble("sound.volume", 1.0);
            float pitch = (float) levelSection.getDouble("sound.pitch", 1.0);

            // Determine if it's a built-in sound or custom sound
            Sound builtInSound = null;
            String customSound = null;

            if (isBuiltInSound(soundName)) {
                // It's a built-in Bukkit sound
                builtInSound = loadSound(soundName, logger);
                if (logger != null) {
                    logger.info("Loaded built-in sound for warning '" + key + "': " + soundName);
                }
            } else {
                // It's a custom sound from resource pack
                customSound = soundName;
                if (logger != null) {
                    logger.info("Loaded custom sound for warning '" + key + "': " + soundName);
                }
            }

            // Create warning level with appropriate sound type
            WarningLevel warningLevel = new WarningLevel(
                    key,
                    oxygenLevel,
                    subtitle,
                    soundEnabled,
                    builtInSound,
                    customSound,
                    volume,
                    pitch
            );

            warningLevels.put(key, warningLevel);
        }

        if (logger != null) {
            logger.info("Loaded " + warningLevels.size() + " warning levels");
        }

        return warningLevels;
    }
}