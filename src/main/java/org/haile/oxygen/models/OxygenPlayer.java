package org.haile.oxygen.models;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Class representing a player with oxygen data
 */
public class OxygenPlayer {
    private final UUID playerId;
    private final String playerName;
    private int oxygenLevel;
    private int lastWarningLevel;

    /**
     * Constructor from a Player object
     *
     * @param player Bukkit Player
     * @param startingOxygen Initial oxygen level
     */
    public OxygenPlayer(Player player, int startingOxygen) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.oxygenLevel = startingOxygen;
        this.lastWarningLevel = 100; // Start with full oxygen as last warning level
    }

    /**
     * Constructor for loading from storage
     *
     * @param playerId Player UUID
     * @param playerName Player name
     * @param oxygenLevel Current oxygen level
     */
    public OxygenPlayer(UUID playerId, String playerName, int oxygenLevel) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.oxygenLevel = oxygenLevel;
        this.lastWarningLevel = 100;
    }

    /**
     * Get player UUID
     * @return UUID of player
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Get player name
     * @return Name of player
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Get current oxygen level
     * @return Current oxygen level
     */
    public int getOxygenLevel() {
        return oxygenLevel;
    }

    /**
     * Set oxygen level
     * @param oxygenLevel New oxygen level
     */
    public void setOxygenLevel(int oxygenLevel) {
        this.oxygenLevel = oxygenLevel;
    }

    /**
     * Decrease oxygen by specified amount
     * @param amount Amount to decrease
     */
    public void decreaseOxygen(int amount) {
        this.oxygenLevel = Math.max(0, this.oxygenLevel - amount);
    }

    /**
     * Increase oxygen by specified amount up to max
     * @param amount Amount to increase
     * @param maxOxygen Maximum oxygen level
     */
    public void increaseOxygen(int amount, int maxOxygen) {
        this.oxygenLevel = Math.min(maxOxygen, this.oxygenLevel + amount);
    }

    /**
     * Get last warning level
     * @return Last warning level
     */
    public int getLastWarningLevel() {
        return lastWarningLevel;
    }

    /**
     * Set last warning level
     * @param lastWarningLevel New last warning level
     */
    public void setLastWarningLevel(int lastWarningLevel) {
        this.lastWarningLevel = lastWarningLevel;
    }
}