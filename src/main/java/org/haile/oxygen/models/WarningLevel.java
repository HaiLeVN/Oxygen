package org.haile.oxygen.models;

import org.bukkit.Sound;

/**
 * Class representing a warning level for oxygen notifications
 * Updated to support custom sounds from resource packs
 */
public class WarningLevel {
    private final String id;
    private final int oxygenLevel;
    private final String subtitle;
    private final boolean soundEnabled;
    private final Sound sound; // Bukkit built-in sound (can be null)
    private final String customSound; // Custom sound from resource pack (can be null)
    private final float volume;
    private final float pitch;

    /**
     * Constructor for built-in Bukkit sounds
     */
    public WarningLevel(String id, int oxygenLevel, String subtitle,
                        boolean soundEnabled, Sound sound, float volume, float pitch) {
        this.id = id;
        this.oxygenLevel = oxygenLevel;
        this.subtitle = subtitle;
        this.soundEnabled = soundEnabled;
        this.sound = sound;
        this.customSound = null;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Constructor for custom sounds from resource pack
     */
    public WarningLevel(String id, int oxygenLevel, String subtitle,
                        boolean soundEnabled, String customSound, float volume, float pitch) {
        this.id = id;
        this.oxygenLevel = oxygenLevel;
        this.subtitle = subtitle;
        this.soundEnabled = soundEnabled;
        this.sound = null;
        this.customSound = customSound;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Constructor for both sound types (auto-detect based on input)
     */
    public WarningLevel(String id, int oxygenLevel, String subtitle,
                        boolean soundEnabled, Sound sound, String customSound,
                        float volume, float pitch) {
        this.id = id;
        this.oxygenLevel = oxygenLevel;
        this.subtitle = subtitle;
        this.soundEnabled = soundEnabled;
        this.sound = sound;
        this.customSound = customSound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public String getId() {
        return id;
    }

    public int getOxygenLevel() {
        return oxygenLevel;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Gets the built-in Bukkit sound
     * @return Bukkit Sound or null if using custom sound
     */
    public Sound getSound() {
        return sound;
    }

    /**
     * Gets the custom sound name from resource pack
     * @return Custom sound name or null if using built-in sound
     */
    public String getCustomSound() {
        return customSound;
    }

    /**
     * Checks if this warning level uses a custom sound
     * @return true if using custom sound, false if using built-in sound
     */
    public boolean isCustomSound() {
        return customSound != null && !customSound.isEmpty();
    }

    /**
     * Gets the sound identifier for logging/debugging
     * @return Sound name (either built-in or custom)
     */
    public String getSoundName() {
        if (isCustomSound()) {
            return customSound;
        } else if (sound != null) {
            return sound.name();
        } else {
            return "NONE";
        }
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}