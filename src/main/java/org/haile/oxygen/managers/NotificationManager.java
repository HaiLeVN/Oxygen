package org.haile.oxygen.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.haile.oxygen.Oxygen;
import org.haile.oxygen.models.ConfigSettings;
import org.haile.oxygen.models.WarningLevel;
import org.haile.oxygen.utils.ConfigUtils;
import org.haile.oxygen.utils.FormatUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Updated NotificationManager with custom sound support
 */
public class NotificationManager {
    private final Oxygen plugin;
    private final ConfigSettings configSettings;

    // Lưu trữ mức oxy cuối cùng của mỗi người chơi để theo dõi khi vượt qua ngưỡng
    private final Map<UUID, Integer> lastOxygenLevels = new HashMap<>();

    // Warning levels configuration
    private final Map<String, WarningLevel> warningLevels = new HashMap<>();

    public NotificationManager(Oxygen plugin, ConfigSettings configSettings) {
        this.plugin = plugin;
        this.configSettings = configSettings;
        loadConfiguration();
    }

    /**
     * Loads notification configuration
     */
    public void loadConfiguration() {
        // Clear existing warning levels
        warningLevels.clear();

        // Check if notifications are enabled from ConfigSettings
        if (!configSettings.isNotificationsEnabled()) {
            return;
        }

        // Load warning levels from FileConfiguration (vì ConfigSettings chưa có warning levels)
        FileConfiguration config = plugin.getConfig();
        Map<String, WarningLevel> loadedWarningLevels = ConfigUtils.loadWarningLevels(config, plugin.getLogger());
        warningLevels.putAll(loadedWarningLevels);

        plugin.getLogger().info("Loaded " + warningLevels.size() + " warning levels");
    }

    /**
     * Kiểm tra xem người chơi có nên nhận cảnh báo không
     * Chỉ gửi cảnh báo khi vừa vượt qua ngưỡng (giảm xuống dưới ngưỡng)
     *
     * @param player Người chơi
     * @param oxygenLevel Mức oxy hiện tại
     */
    public void checkAndSendWarnings(Player player, int oxygenLevel) {
        if (!configSettings.isNotificationsEnabled() || warningLevels.isEmpty()) {
            return;
        }

        UUID playerUUID = player.getUniqueId();

        // Lấy mức oxy cuối cùng đã lưu
        int lastOxygen = lastOxygenLevels.getOrDefault(playerUUID, configSettings.getMaxOxygenLevel());

        // Lưu mức oxy hiện tại để so sánh lần sau
        lastOxygenLevels.put(playerUUID, oxygenLevel);

        // Kiểm tra từng ngưỡng cảnh báo
        for (WarningLevel warning : warningLevels.values()) {
            int threshold = warning.getOxygenLevel();

            // Chỉ gửi cảnh báo khi oxy vừa vượt qua ngưỡng (giảm xuống dưới ngưỡng)
            // Điều kiện: Mức cũ trên ngưỡng, mức mới dưới hoặc bằng ngưỡng
            if (lastOxygen > threshold && oxygenLevel <= threshold) {
                sendWarning(player, warning, oxygenLevel);
            }
        }
    }

    /**
     * Gửi cảnh báo đến người chơi với hỗ trợ custom sound
     *
     * @param player Người chơi
     * @param warning Cấp độ cảnh báo
     * @param oxygenLevel Mức oxy hiện tại
     */
    private void sendWarning(Player player, WarningLevel warning, int oxygenLevel) {
        // Format subtitle with oxygen level using MiniMessage
        Component subtitle;

        try {
            // Sử dụng FormatUtils để thay thế placeholders và tạo Component
            subtitle = FormatUtils.formatOxygenComponent(warning.getSubtitle(), oxygenLevel, configSettings.getMaxOxygenLevel());
        } catch (Exception e) {
            // Fallback nếu có lỗi với MiniMessage
            plugin.getLogger().warning("Error formatting warning subtitle with MiniMessage: " + e.getMessage());
            String formattedSubtitle = FormatUtils.formatOxygenText(warning.getSubtitle(), oxygenLevel, configSettings.getMaxOxygenLevel());
            subtitle = Component.text(formattedSubtitle);
        }

        // Create title times
        Title.Times times = Title.Times.times(
                Duration.ofMillis(200),  // Fade in
                Duration.ofMillis(1500), // Stay
                Duration.ofMillis(300)   // Fade out
        );

        // Create and show title
        Title title = Title.title(Component.empty(), subtitle, times);
        player.showTitle(title);

        // Play sound if enabled
        if (warning.isSoundEnabled()) {
            playWarningSound(player, warning);
        }
    }

    /**
     * Plays warning sound (supports both built-in and custom sounds)
     *
     * @param player Player to play sound for
     * @param warning Warning level containing sound information
     */
    private void playWarningSound(Player player, WarningLevel warning) {
        try {
            if (warning.isCustomSound()) {
                // Play custom sound from resource pack
                String customSoundName = warning.getCustomSound();
                player.playSound(
                        player.getLocation(),
                        customSoundName,
                        warning.getVolume(),
                        warning.getPitch()
                );

                plugin.getLogger().fine("Played custom sound '" + customSoundName +
                        "' for player " + player.getName());

            } else if (warning.getSound() != null) {
                // Play built-in Bukkit sound
                player.playSound(
                        player.getLocation(),
                        warning.getSound(),
                        warning.getVolume(),
                        warning.getPitch()
                );

                plugin.getLogger().fine("Played built-in sound '" + warning.getSound().name() +
                        "' for player " + player.getName());
            } else {
                plugin.getLogger().warning("Warning '" + warning.getId() +
                        "' has sound enabled but no valid sound configured!");
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error playing warning sound '" + warning.getSoundName() +
                    "' for player " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Gets information about all loaded warning levels (for debugging)
     *
     * @return Map of warning level information
     */
    public Map<String, String> getWarningLevelInfo() {
        Map<String, String> info = new HashMap<>();

        for (Map.Entry<String, WarningLevel> entry : warningLevels.entrySet()) {
            WarningLevel warning = entry.getValue();
            String soundInfo = warning.isSoundEnabled() ?
                    (warning.isCustomSound() ? "Custom: " + warning.getCustomSound() : "Built-in: " + warning.getSoundName()) :
                    "Disabled";

            info.put(entry.getKey(),
                    "Oxygen: " + warning.getOxygenLevel() +
                            ", Sound: " + soundInfo +
                            ", Volume: " + warning.getVolume() +
                            ", Pitch: " + warning.getPitch());
        }

        return info;
    }
}