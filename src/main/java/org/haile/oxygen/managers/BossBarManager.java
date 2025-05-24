package org.haile.oxygen.managers;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.haile.oxygen.Oxygen;
import org.haile.oxygen.models.ConfigSettings;
import org.haile.oxygen.utils.FormatUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager {
    private final Map<UUID, BossBar> playerBossBars = new HashMap<>();
    private final Oxygen plugin;
    private final ConfigSettings configSettings;

    public BossBarManager(Oxygen plugin, ConfigSettings configSettings) {
        this.plugin = plugin;
        this.configSettings = configSettings;
    }

    /**
     * Loads configuration for boss bar from ConfigSettings
     */
    public void loadConfiguration() {
        // Configuration is now loaded through ConfigSettings
        // This method can be used to trigger updates when config changes
        plugin.getLogger().info("BossBar configuration loaded from ConfigSettings");
    }

    /**
     * Converts Bukkit BarColor to Adventure BossBar.Color
     */
    private BossBar.Color convertBukkitColorToAdventure(org.bukkit.boss.BarColor bukkitColor) {
        return switch (bukkitColor) {
            case PINK -> BossBar.Color.PINK;
            case BLUE -> BossBar.Color.BLUE;
            case RED -> BossBar.Color.RED;
            case GREEN -> BossBar.Color.GREEN;
            case YELLOW -> BossBar.Color.YELLOW;
            case PURPLE -> BossBar.Color.PURPLE;
            case WHITE -> BossBar.Color.WHITE;
            default -> BossBar.Color.WHITE;
        };
    }

    /**
     * Converts Bukkit BarStyle to Adventure BossBar.Overlay
     */
    private BossBar.Overlay convertBukkitStyleToAdventure(org.bukkit.boss.BarStyle bukkitStyle) {
        return switch (bukkitStyle) {
            case SOLID -> BossBar.Overlay.PROGRESS;
            case SEGMENTED_6 -> BossBar.Overlay.NOTCHED_6;
            case SEGMENTED_10 -> BossBar.Overlay.NOTCHED_10;
            case SEGMENTED_12 -> BossBar.Overlay.NOTCHED_12;
            case SEGMENTED_20 -> BossBar.Overlay.NOTCHED_20;
            default -> BossBar.Overlay.PROGRESS;
        };
    }

    /**
     * Creates a boss bar for a player
     * @param player The player
     */
    public void createBossBar(Player player) {
        // Remove old boss bar if exists
        removeBossBar(player);

        // Get settings from ConfigSettings
        String titleFormat = configSettings.getBossBarTitleFormat();
        BossBar.Color highColor = convertBukkitColorToAdventure(configSettings.getHighColor());
        BossBar.Overlay barOverlay = convertBukkitStyleToAdventure(configSettings.getBarStyle());

        // Format title with default values using MiniMessage
        Component titleComponent;

        try {
            // Sử dụng FormatUtils để thay thế placeholders và tạo Component
            titleComponent = FormatUtils.formatOxygenComponent(titleFormat, 100, configSettings.getMaxOxygenLevel());
        } catch (Exception e) {
            // Fallback nếu có lỗi với MiniMessage
            plugin.getLogger().warning("Error formatting BossBar title with MiniMessage: " + e.getMessage());
            String formattedTitle = FormatUtils.formatOxygenText(titleFormat, 100, configSettings.getMaxOxygenLevel());
            titleComponent = Component.text(formattedTitle);
        }

        // Tạo Adventure BossBar với Component đã có màu hex
        BossBar bossBar = BossBar.bossBar(
                titleComponent,
                1.0f,  // progress (100%)
                highColor,
                barOverlay
        );

        // Hiển thị cho người chơi
        player.showBossBar(bossBar);

        // Store boss bar
        playerBossBars.put(player.getUniqueId(), bossBar);

        plugin.getLogger().info("Created Adventure BossBar for " + player.getName());
    }

    /**
     * Updates a player's boss bar with current oxygen level
     * @param player The player
     * @param oxygen Current oxygen level
     */
    public void updateBossBar(Player player, int oxygen) {
        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        if (bossBar == null) {
            // Create a new boss bar if none exists
            createBossBar(player);
            bossBar = playerBossBars.get(player.getUniqueId());
            if (bossBar == null) return; // Still null? Then return
        }

        // Get settings from ConfigSettings
        String titleFormat = configSettings.getBossBarTitleFormat();
        int maxOxygen = configSettings.getMaxOxygenLevel();

        // Format title with current oxygen using MiniMessage
        Component titleComponent;

        try {
            // Sử dụng FormatUtils để thay thế placeholders và tạo Component
            titleComponent = FormatUtils.formatOxygenComponent(titleFormat, oxygen, maxOxygen);
        } catch (Exception e) {
            // Fallback nếu có lỗi với MiniMessage
            plugin.getLogger().warning("Error formatting BossBar title with MiniMessage: " + e.getMessage());
            String formattedTitle = FormatUtils.formatOxygenText(titleFormat, oxygen, maxOxygen);
            titleComponent = Component.text(formattedTitle);
        }

        // Cập nhật title với màu hex được bảo toàn
        bossBar.name(titleComponent);

        // Update progress (0.0 to 1.0)
        float progress = Math.max(0.0f, Math.min(1.0f, (float) oxygen / maxOxygen));
        bossBar.progress(progress);

        // **FIX: Use thresholds from ConfigSettings instead of hardcoded values**
        BossBar.Color newColor;
        int mediumThreshold = configSettings.getMediumThreshold();
        int lowThreshold = configSettings.getLowThreshold();

        if (oxygen > mediumThreshold) {
            newColor = convertBukkitColorToAdventure(configSettings.getHighColor());
        } else if (oxygen > lowThreshold) {
            newColor = convertBukkitColorToAdventure(configSettings.getMediumColor());
        } else {
            newColor = convertBukkitColorToAdventure(configSettings.getLowColor());
        }

        // Chỉ cập nhật màu nếu khác với màu hiện tại (tối ưu hiệu suất)
        if (bossBar.color() != newColor) {
            bossBar.color(newColor);
        }
    }

    /**
     * Removes a player's boss bar
     * @param player The player
     */
    public void removeBossBar(Player player) {
        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }

    /**
     * Updates all active boss bars with new config
     */
    public void updateAllBossBars() {
        // ConfigSettings is already updated by the main plugin class

        // Update all active boss bars
        for (Map.Entry<UUID, BossBar> entry : playerBossBars.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                BossBar bossBar = entry.getValue();

                // Update overlay/style from ConfigSettings
                bossBar.overlay(convertBukkitStyleToAdventure(configSettings.getBarStyle()));

                // Oxygen level is needed to update color correctly
                int oxygen = plugin.getOxygenManager().getOxygen(player);
                updateBossBar(player, oxygen);
            }
        }
    }
}