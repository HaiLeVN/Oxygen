package org.haile.oxygen.managers;

import org.bukkit.entity.Player;
import org.haile.oxygen.models.ConfigSettings;
import org.haile.oxygen.models.OxygenPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OxygenPlayerManager {
    private final Map<UUID, OxygenPlayer> oxygenPlayers = new HashMap<>();
    private final BossBarManager bossBarManager;
    private final OxygenDataManager dataManager;
    private final ConfigSettings configSettings;

    public OxygenPlayerManager(BossBarManager bossBarManager, OxygenDataManager dataManager, ConfigSettings configSettings) {
        this.bossBarManager = bossBarManager;
        this.dataManager = dataManager;
        this.configSettings = configSettings;
    }

    /**
     * Loads configuration for OxygenPlayerManager
     */
    public void loadConfiguration() {
        // Configuration is now loaded through ConfigSettings
        // This method can be used to trigger updates when config changes
    }

    /**
     * Sets a player's oxygen level and updates their boss bar
     * @param player The player
     * @param oxygen The new oxygen level
     */
    public void setOxygen(Player player, int oxygen) {
        // Check for unlimited permission
        int playerMaxOxygen = player.hasPermission("oxygen.unlimited") ? 9999 : configSettings.getMaxOxygenLevel();

        // Clamp oxygen value
        int clampedOxygen = Math.max(0, Math.min(playerMaxOxygen, oxygen));

        // Get or create OxygenPlayer
        UUID playerId = player.getUniqueId();
        OxygenPlayer oxygenPlayer = oxygenPlayers.computeIfAbsent(
                playerId,
                id -> new OxygenPlayer(player, configSettings.getMaxOxygenLevel())
        );

        // Update oxygen level
        oxygenPlayer.setOxygenLevel(clampedOxygen);

        // Update boss bar
        bossBarManager.updateBossBar(player, clampedOxygen);

        // Save to persistent storage
        dataManager.saveOxygenLevel(player, clampedOxygen);
    }

    /**
     * Gets a player's oxygen level
     * @param player The player
     * @return The player's oxygen level
     */
    public int getOxygen(Player player) {
        OxygenPlayer oxygenPlayer = oxygenPlayers.get(player.getUniqueId());
        return oxygenPlayer != null ? oxygenPlayer.getOxygenLevel() : configSettings.getMaxOxygenLevel();
    }

    /**
     * Gets the maximum oxygen level from ConfigSettings
     * @return Maximum oxygen level
     */
    public int getMaxOxygen() {
        return configSettings.getMaxOxygenLevel();
    }

    /**
     * Loads a player's oxygen level from storage
     * @param player The player
     */
    public void loadOxygen(Player player) {
        int oxygen = dataManager.loadOxygenLevel(player);

        // Create and store OxygenPlayer
        OxygenPlayer oxygenPlayer = new OxygenPlayer(
                player.getUniqueId(),
                player.getName(),
                oxygen
        );

        oxygenPlayers.put(player.getUniqueId(), oxygenPlayer);

        // Update boss bar
        bossBarManager.updateBossBar(player, oxygen);
    }

    /**
     * Decreases a player's oxygen level by 1
     * @param player The player
     */
    public void decreaseOxygen(Player player) {
        UUID playerId = player.getUniqueId();
        OxygenPlayer oxygenPlayer = oxygenPlayers.get(playerId);

        if (oxygenPlayer != null) {
            oxygenPlayer.decreaseOxygen(1);
            int newOxygen = oxygenPlayer.getOxygenLevel();

            // Update boss bar
            bossBarManager.updateBossBar(player, newOxygen);

            // Save to storage
            dataManager.saveOxygenLevel(player, newOxygen);
        } else {
            // If player not found, initialize with default value and decrease
            loadOxygen(player);
            decreaseOxygen(player); // Recursive but will only happen once
        }
    }

    /**
     * Applies damage to a player when oxygen is depleted
     * @param player The player
     */
    public void applyDamage(Player player) {
        if (player.getHealth() > 0) {
            double damage = Math.min(player.getHealth(), configSettings.getDamageAmount());
            player.damage(damage);
        }
    }

    /**
     * Gets OxygenPlayer object for a player
     * @param player The player
     * @return OxygenPlayer object or null if not found
     */
    public OxygenPlayer getOxygenPlayer(Player player) {
        return oxygenPlayers.get(player.getUniqueId());
    }
}