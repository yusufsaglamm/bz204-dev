package com.robotvacuum.model;

/**
 * Represents the types of dirt the robot can clean.
 * Each type has a different cleaning duration and battery cost.
 */
public enum DirtType {
    DUST("Toz", 1, 1.0),
    LIQUID("Sıvı", 3, 2.0),
    STAIN("Leke", 5, 3.0);

    private final String displayName;
    /** Number of cleaning steps required to fully clean this dirt type */
    private final int cleaningSteps;
    /** Additional battery consumption multiplier when cleaning this type */
    private final double batteryCostMultiplier;

    DirtType(String displayName, int cleaningSteps, double batteryCostMultiplier) {
        this.displayName = displayName;
        this.cleaningSteps = cleaningSteps;
        this.batteryCostMultiplier = batteryCostMultiplier;
    }

    public String getDisplayName() { return displayName; }
    public int getCleaningSteps() { return cleaningSteps; }
    public double getBatteryCostMultiplier() { return batteryCostMultiplier; }
}
