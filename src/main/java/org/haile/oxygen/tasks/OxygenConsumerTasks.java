package org.haile.oxygen.tasks;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.haile.oxygen.Oxygen;
import org.haile.oxygen.managers.NotificationManager;
import org.haile.oxygen.managers.OxygenPlayerManager;
import org.haile.oxygen.managers.RegionChecker;
import org.haile.oxygen.models.ConfigSettings;
import org.haile.oxygen.utils.WorldUtils;

import java.util.Set;

public class OxygenConsumerTasks extends BukkitRunnable {
    private final Oxygen plugin;
    private final OxygenPlayerManager oxygenManager;
    private final RegionChecker regionChecker;
    private final NotificationManager notificationManager;
    private final ConfigSettings configSettings;

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

    @Override
    public void run() {
        // Get settings from ConfigSettings
        Set<GameMode> allowedGameModes = configSettings.getAllowedGameModes();
        Set<String> allowedWorlds = configSettings.getAllowedWorlds();
        boolean worldsAreWhitelist = configSettings.isWorldsAreWhitelist();
        int maxOxygen = configSettings.getMaxOxygenLevel();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // Skip players in disallowed gamemodes
            if (!WorldUtils.isPlayerInAllowedGameMode(player, allowedGameModes)) {
                continue;
            }

            // Check if player is in an allowed world
            boolean worldAllowed = WorldUtils.isPlayerInAllowedWorld(player, allowedWorlds, worldsAreWhitelist);

            if (!worldAllowed) {
                // If player is in a disallowed world, ensure they have full oxygen
                oxygenManager.setOxygen(player, maxOxygen);
                continue;
            }

            // If player is in a spaceship region, then will not consume oxygen
            if (regionChecker.isInSpaceshipRegion(player)) {
                continue;
            }

            // Decrease oxygen for players in allowed worlds and gamemodes
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
}