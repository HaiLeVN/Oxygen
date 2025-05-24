package org.haile.oxygen.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.haile.oxygen.Oxygen;

public class RegionChecker {
    private final Oxygen plugin;

    public RegionChecker(Oxygen plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if a player is in a spaceship region
     * @param player The player to check
     * @return True if player is in a spaceship region
     */
    public boolean isInSpaceshipRegion(Player player) {
        try {
            // Convert Bukkit Location to WorldEdit Location
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(player.getWorld());
            BlockVector3 position = BukkitAdapter.asBlockVector(player.getLocation());

            // Get the RegionContainer and RegionManager
            var regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            var regionManager = regionContainer.get(world);

            if (regionManager == null) {
                return false; // No regions in this world
            }

            // Get applicable regions at the player's location
            ApplicableRegionSet regions = regionManager.getApplicableRegions(position);

            // Check if any region has "spaceship" in its ID
            for (ProtectedRegion region : regions) {
                if (region.getId().toLowerCase().contains("spaceship")) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking spaceship region: " + e.getMessage());
            return false; // Return false on error to avoid breaking functionality
        }
    }
}