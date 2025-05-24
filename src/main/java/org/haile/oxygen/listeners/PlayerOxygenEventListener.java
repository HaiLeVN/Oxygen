package org.haile.oxygen.listeners;

import org.haile.oxygen.managers.BossBarManager;
import org.haile.oxygen.managers.OxygenPlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerOxygenEventListener implements Listener {
    private final OxygenPlayerManager oxygenManager;
    private final BossBarManager bossBarManager;

    public PlayerOxygenEventListener(OxygenPlayerManager oxygenManager, BossBarManager bossBarManager) {
        this.oxygenManager = oxygenManager;
        this.bossBarManager = bossBarManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load oxygen level from storage and create boss bar
        oxygenManager.loadOxygen(event.getPlayer());
        bossBarManager.createBossBar(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Reset oxygen to 100 on respawn
        oxygenManager.setOxygen(event.getPlayer(), 100);
        bossBarManager.createBossBar(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save oxygen level before removing boss bar
        // Note: We don't need to explicitly save here since it's already saved in setOxygen
        // But we could add it for extra safety
        bossBarManager.removeBossBar(event.getPlayer());
    }
}