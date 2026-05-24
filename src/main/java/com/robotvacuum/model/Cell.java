package com.robotvacuum.model;

/**
 * Represents a single cell in the room grid.
 * A cell can be empty, have an obstacle, or have dirt.
 */
public class Cell {

    public enum CellType {
        FLOOR,
        OBSTACLE,
        CHARGING_STATION
    }

    private final int x;
    private final int y;
    private CellType type;
    private DirtType dirtType;
    /** Remaining cleaning steps needed to fully clean the dirt */
    private int remainingCleaningSteps;
    private boolean cleaned;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.type = CellType.FLOOR;
        this.dirtType = null;
        this.remainingCleaningSteps = 0;
        this.cleaned = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public CellType getType() { return type; }
    public void setType(CellType type) { this.type = type; }

    public boolean isObstacle() { return type == CellType.OBSTACLE; }
    public boolean isChargingStation() { return type == CellType.CHARGING_STATION; }

    public boolean hasDirt() { return dirtType != null && remainingCleaningSteps > 0; }

    public DirtType getDirtType() { return dirtType; }

    public void setDirt(DirtType dirtType) {
        if (type == CellType.OBSTACLE || type == CellType.CHARGING_STATION) return;
        this.dirtType = dirtType;
        this.remainingCleaningSteps = dirtType.getCleaningSteps();
        this.cleaned = false;
    }

    public void clearDirt() {
        this.dirtType = null;
        this.remainingCleaningSteps = 0;
    }

    /**
     * Applies one cleaning step. Returns true if dirt is fully removed.
     */
    public boolean clean() {
        if (!hasDirt()) {
            cleaned = true;
            return true;
        }
        remainingCleaningSteps--;
        if (remainingCleaningSteps <= 0) {
            dirtType = null;
            remainingCleaningSteps = 0;
            cleaned = true;
            return true;
        }
        return false;
    }

    public int getRemainingCleaningSteps() { return remainingCleaningSteps; }

    public boolean isCleaned() { return cleaned; }
    public void setCleaned(boolean cleaned) { this.cleaned = cleaned; }

    @Override
    public String toString() {
        return "Cell(" + x + "," + y + ") type=" + type + " dirt=" + dirtType;
    }
}
