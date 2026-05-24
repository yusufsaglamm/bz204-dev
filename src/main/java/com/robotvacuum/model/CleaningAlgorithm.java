package com.robotvacuum.model;

/**
 * Available cleaning algorithms for the robot vacuum.
 */
public enum CleaningAlgorithm {
    RANDOM("Rastgele"),
    SPIRAL("Spiral"),
    WALL_FOLLOW("Duvar Takip");

    private final String displayName;

    CleaningAlgorithm(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
