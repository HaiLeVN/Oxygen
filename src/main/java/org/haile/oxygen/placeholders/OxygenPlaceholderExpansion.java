package org.haile.oxygen.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.haile.oxygen.Oxygen;
import org.haile.oxygen.managers.OxygenPlayerManager;
import org.haile.oxygen.models.ConfigSettings;
import org.jetbrains.annotations.NotNull;

public class OxygenPlaceholderExpansion extends PlaceholderExpansion {

    private Oxygen plugin;

    public OxygenPlaceholderExpansion(Oxygen plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "oxygen";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HaiLe";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @NotNull String getRequiredPlugin() {
        return "Oxygen";
    }

    @Override
    public boolean canRegister() {
        return (plugin = (Oxygen) Bukkit.getPluginManager().getPlugin(getRequiredPlugin())) != null;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        // Only work with online players for oxygen data
        if (!(offlinePlayer instanceof Player)) {
            return "N/A";
        }

        Player player = (Player) offlinePlayer;
        OxygenPlayerManager oxygenManager = plugin.getOxygenManager();
        ConfigSettings configSettings = plugin.getConfigSettings();

        if (oxygenManager == null || configSettings == null) {
            return "Error";
        }

        switch (params.toLowerCase()) {
            case "current":
            case "level":
                // Current oxygen level
                return String.valueOf(oxygenManager.getOxygen(player));

            case "max":
            case "maximum":
                // Maximum oxygen level (considers unlimited permission)
                int maxOxygen = player.hasPermission("oxygen.unlimited") ? 9999 : configSettings.getMaxOxygenLevel();
                return String.valueOf(maxOxygen);

            case "percentage":
            case "percent":
                // Oxygen as percentage
                int current = oxygenManager.getOxygen(player);
                int max = player.hasPermission("oxygen.unlimited") ? 9999 : configSettings.getMaxOxygenLevel();
                if (max == 0) return "0";
                double percentage = ((double) current / max) * 100;
                return String.format("%.1f", percentage);

            case "bar":
            case "progress":
                // Visual progress bar (20 characters)
                return createProgressBar(oxygenManager.getOxygen(player),
                        player.hasPermission("oxygen.unlimited") ? 9999 : configSettings.getMaxOxygenLevel(), 20);

            case "status":
                // Status based on oxygen level
                int oxygen = oxygenManager.getOxygen(player);
                int maxOxy = player.hasPermission("oxygen.unlimited") ? 9999 : configSettings.getMaxOxygenLevel();
                double ratio = (double) oxygen / maxOxy;

                if (ratio > 0.6) return "Healthy";
                else if (ratio > 0.3) return "Low";
                else if (ratio > 0) return "Critical";
                else return "Depleted";

            case "color":
                // Color code based on oxygen level
                int oxyLevel = oxygenManager.getOxygen(player);
                int maxLevel = player.hasPermission("oxygen.unlimited") ? 9999 : configSettings.getMaxOxygenLevel();
                double oxyRatio = (double) oxyLevel / maxLevel;

                if (oxyRatio > 0.6) return "&a"; // Green
                else if (oxyRatio > 0.3) return "&e"; // Yellow
                else if (oxyRatio > 0) return "&c"; // Red
                else return "&4"; // Dark Red

            case "formatted":
                // Formatted display with color
                int currentOxy = oxygenManager.getOxygen(player);
                int maxOxy2 = player.hasPermission("oxygen.unlimited") ? 9999 : configSettings.getMaxOxygenLevel();
                double ratio2 = (double) currentOxy / maxOxy2;

                String color;
                if (ratio2 > 0.6) color = "&a";
                else if (ratio2 > 0.3) color = "&e";
                else if (ratio2 > 0) color = "&c";
                else color = "&4";

                return color + currentOxy + "&7/" + maxOxy2;

            case "remaining_time":
                // Estimated remaining time in minutes (rough calculation)
                int remainingOxy = oxygenManager.getOxygen(player);
                int decreaseRate = configSettings.getDecreaseRate(); // ticks
                if (decreaseRate <= 0 || remainingOxy <= 0) return "0";

                // Rough calculation: oxygen_left * decrease_rate_ticks / (20 ticks/second * 60 seconds/minute)
                double minutesLeft = (remainingOxy * decreaseRate) / (20.0 * 60.0);
                return String.format("%.1f", minutesLeft);

            default:
                return null;
        }
    }

    /**
     * Creates a visual progress bar
     * @param current Current value
     * @param max Maximum value
     * @param length Length of the bar
     * @return Progress bar string
     */
    private String createProgressBar(int current, int max, int length) {
        if (max <= 0) return "█".repeat(length);

        double ratio = Math.min(1.0, (double) current / max);
        int filled = (int) (ratio * length);
        int empty = length - filled;

        String color;
        if (ratio > 0.6) color = "&a";
        else if (ratio > 0.3) color = "&e";
        else if (ratio > 0) color = "&c";
        else color = "&4";

        return color + "█".repeat(filled) + "&7" + "░".repeat(empty);
    }
}