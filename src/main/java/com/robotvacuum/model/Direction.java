package com.robotvacuum.model;

/**
 * Cardinal directions for robot movement.
 */
public enum Direction {
    NORTH(0, -1, "Kuzey (↑)"),
    EAST(1, 0, "Doğu (→)"),
    SOUTH(0, 1, "Güney (↓)"),
    WEST(-1, 0, "Batı (←)");

    private final int dx;
    private final int dy;
    private final String displayName;

    Direction(int dx, int dy, String displayName) {
        this.dx = dx;
        this.dy = dy;
        this.displayName = displayName;
    }

    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public String getDisplayName() { return displayName; }

    /** Returns the opposite direction */
    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    /** Returns a 90-degree clockwise turn */
    public Direction turnRight() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }

    /** Returns a 90-degree counter-clockwise turn */
    public Direction turnLeft() {
        return switch (this) {
            case NORTH -> WEST;
            case WEST -> SOUTH;
            case SOUTH -> EAST;
            case EAST -> NORTH;
        };
    }
}
