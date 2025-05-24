package org.haile.oxygen.utils;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Utility class for world and player location related operations
 */
public class WorldUtils {
    /**
     * Checks if a player is in an allowed world
     *
     * @param player Player to check
     * @param allowedWorlds Set of allowed world names (lowercase)
     * @param isWhitelist Whether the allowed worlds list is a whitelist
     * @return True if player is in an allowed world
     */
    public static boolean isPlayerInAllowedWorld(Player player, Set<String> allowedWorlds, boolean isWhitelist) {
        String worldName = player.getWorld().getName().toLowerCase();
        return isWhitelist == allowedWorlds.contains(worldName);
    }

    /**
     * Checks if a player's gamemode is allowed for oxygen mechanics
     *
     * @param player Player to check
     * @param allowedGameModes Set of allowed gamemodes
     * @return True if player's gamemode is allowed
     */
    public static boolean isPlayerInAllowedGameMode(Player player, Set<GameMode> allowedGameModes) {
        return allowedGameModes.contains(player.getGameMode());
    }
}