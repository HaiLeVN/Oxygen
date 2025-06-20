package org.haile.oxygen.tasks;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.haile.oxygen.Oxygen;
import org.haile.oxygen.managers.NotificationManager;
import org.haile.oxygen.managers.OxygenPlayerManager;
import org.haile.oxygen.managers.RegionChecker;
import org.haile.oxygen.models.ConfigSettings;
import org.haile.oxygen.utils.WorldUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class OxygenConsumerTasks extends BukkitRunnable {
    private final Oxygen plugin;
    private final OxygenPlayerManager oxygenManager;
    private final RegionChecker regionChecker;
    private final NotificationManager notificationManager;
    private final ConfigSettings configSettings;

    // Track last oxygen decrease time for each player
    private final Map<UUID, Long> lastDecreaseTime = new HashMap<>();

    // Pumpkin helmet increases oxygen decrease interval by 5x (from 1s to 5s)
    private static final long PUMPKIN_MULTIPLIER = 5L;

    public OxygenConsumerTasks(Oxygen plugin, OxygenPlayerManager oxygenManager,
                               RegionChecker regionChecker, NotificationManager notificationManager,
                               ConfigSettings configSettings) {
        this.plugin = plugin;
        this.oxygenManager = oxygenManager;
        this.regionChecker = regionChecker;
        this.notificationManager = notificationManager;
        this.configSettings = configSettings;
    }

    /**
     * Loads configuration for the tasks from ConfigSettings
     */
    public void loadConfiguration() {
        // Configuration is now loaded through ConfigSettings
        // This method can be used to trigger updates when config changes
        plugin.getLogger().info("OxygenConsumerTasks configuration loaded from ConfigSettings");
    }

    /**
     * Checks if player is wearing a carved pumpkin helmet
     * @param player The player to check
     * @return true if player is wearing carved pumpkin on head
     */
    private boolean isWearingCarvedPumpkin(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        return helmet != null && helmet.getType() == Material.CARVED_PUMPKIN;
    }

    /**
     * Gets the oxygen decrease interval for a player based on their equipment
     * @param player The player to check
     * @return Interval in milliseconds between oxygen decreases
     */
    private long getOxygenDecreaseInterval(Player player) {
        // Get base decrease rate from config (in ticks)
        long baseRateTicks = configSettings.getDecreaseRate();

        // Convert ticks to milliseconds (1 tick = 50ms) - tang so giay len 5s khi deo mu
        long baseRateMs = baseRateTicks * 50L;

        // If wearing carved pumpkin, multiply the interval by PUMPKIN_MULTIPLIER
        if (isWearingCarvedPumpkin(player)) {
            return baseRateMs * PUMPKIN_MULTIPLIER;
        }

        return baseRateMs;
    }

    /**
     * Checks if enough time has passed to decrease oxygen for this player
     * @param player The player to check
     * @return true if oxygen should be decreased
     */
    private boolean shouldDecreaseOxygen(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Get the required interval for this player
        long requiredInterval = getOxygenDecreaseInterval(player);

        // Get last decrease time, default to 0 if not found
        long lastTime = lastDecreaseTime.getOrDefault(playerId, 0L);

        // Check if enough time has passed
        if (currentTime - lastTime >= requiredInterval) {
            // Update last decrease time
            lastDecreaseTime.put(playerId, currentTime);
            return true;
        }

        return false;
    }

    @Override
    public void run() {
        // Get settings from ConfigSettings
        Set<GameMode> allowedGameModes = configSettings.getAllowedGameModes();
        Set<String> allowedWorlds = configSettings.getAllowedWorlds();
        boolean worldsAreWhitelist = configSettings.isWorldsAreWhitelist();
        int maxOxygen = configSettings.getMaxOxygenLevel();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();

            // Skip players in disallowed gamemodes
            if (!WorldUtils.isPlayerInAllowedGameMode(player, allowedGameModes)) {
                // Clean up tracking for players in disallowed gamemodes
                lastDecreaseTime.remove(playerId);
                continue;
            }

            // Check if player is in an allowed world
            boolean worldAllowed = WorldUtils.isPlayerInAllowedWorld(player, allowedWorlds, worldsAreWhitelist);

            if (!worldAllowed) {
                // If player is in a disallowed world, ensure they have full oxygen
                oxygenManager.setOxygen(player, maxOxygen);
                // Clean up tracking for this player
                lastDecreaseTime.remove(playerId);
                continue;
            }

            // If player is in a spaceship region, then will not consume oxygen
            if (regionChecker.isInSpaceshipRegion(player)) {
                // Don't clean up tracking - they might leave the spaceship region
                continue;
            }

            // Check if enough time has passed to decrease oxygen for this specific player
            if (!shouldDecreaseOxygen(player)) {
                continue; // Skip oxygen decrease - not enough time passed for this player
            }

            // Decrease oxygen for this player
            int currentOxygen = oxygenManager.getOxygen(player);
            oxygenManager.decreaseOxygen(player);

            // Check if we need to display warnings
            notificationManager.checkAndSendWarnings(player, currentOxygen);

            // Apply damage if oxygen is depleted
            if (oxygenManager.getOxygen(player) == 0) {
                oxygenManager.applyDamage(player);
            }
        }
    }

    @Override
    public void cancel() {
        // Clean up tracking when task is cancelled
        lastDecreaseTime.clear();
        super.cancel();
    }
}