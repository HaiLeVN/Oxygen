package org.haile.oxygen.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.haile.oxygen.Oxygen;
import org.haile.oxygen.managers.BossBarManager;
import org.haile.oxygen.managers.OxygenPlayerManager;

/**
 * Listener for handling plugin reload events
 */
public class PluginReloadListener implements Listener {
    private final Oxygen plugin;

    public PluginReloadListener(Oxygen plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        // Only listen for our plugin being enabled (which happens during reload)
        if (event.getPlugin().getName().equals("Oxygen")) {
            plugin.getLogger().info("Oxygen plugin has been reloaded, recreating bossbars...");

            BossBarManager bossBarManager = plugin.getBossBarManager();
            OxygenPlayerManager oxygenManager = plugin.getOxygenManager();

            if (bossBarManager != null && oxygenManager != null) {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    // Recreate and update bossbars for all online players
                    bossBarManager.removeBossBar(player);
                    bossBarManager.createBossBar(player);
                    bossBarManager.updateBossBar(player, oxygenManager.getOxygen(player));

                    // Send a message to inform the player
                    player.sendMessage("§a[Oxygen] Plugin đã được tải lại.");
                }
            }
        }
    }
}