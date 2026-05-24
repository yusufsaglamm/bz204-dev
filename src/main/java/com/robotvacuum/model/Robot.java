package com.robotvacuum.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the robot vacuum cleaner.
 * Manages position, direction, battery, and path history.
 */
public class Robot {

    public static final double MAX_BATTERY = 100.0;
    public static final double BATTERY_LOW_THRESHOLD = 20.0;
    public static final double MOVE_BATTERY_COST = 0.5;

    private int x;
    private int y;
    private Direction direction;
    private double battery;
    private boolean isCharging;
    private boolean returningToStation;
    private int cleaningStepCount;

    /** Path history for visualizing the robot's trail */
    private final List<int[]> pathHistory;

    public Robot(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.direction = Direction.EAST;
        this.battery = MAX_BATTERY;
        this.isCharging = false;
        this.returningToStation = false;
        this.cleaningStepCount = 0;
        this.pathHistory = new ArrayList<>();
        pathHistory.add(new int[]{startX, startY});
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        pathHistory.add(new int[]{x, y});
        // Keep history bounded to prevent memory issues
        if (pathHistory.size() > 500) {
            pathHistory.remove(0);
        }
    }

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }

    public double getBattery() { return battery; }
    public void setBattery(double battery) {
        this.battery = Math.max(0, Math.min(MAX_BATTERY, battery));
    }

    public void drainBattery(double amount) {
        this.battery = Math.max(0, this.battery - amount);
    }

    public void chargeBattery(double amount) {
        this.battery = Math.min(MAX_BATTERY, this.battery + amount);
    }

    public boolean isBatteryLow() {
        return battery <= BATTERY_LOW_THRESHOLD;
    }

    public boolean isBatteryEmpty() {
        return battery <= 0;
    }

    public boolean isCharging() { return isCharging; }
    public void setCharging(boolean charging) { isCharging = charging; }

    public boolean isReturningToStation() { return returningToStation; }
    public void setReturningToStation(boolean returningToStation) {
        this.returningToStation = returningToStation;
    }

    public int getCleaningStepCount() { return cleaningStepCount; }
    public void incrementCleaningStep() { cleaningStepCount++; }
    public void resetCleaningStep() { cleaningStepCount = 0; }

    public List<int[]> getPathHistory() { return pathHistory; }

    public void clearPathHistory() {
        pathHistory.clear();
        pathHistory.add(new int[]{x, y});
    }

    public double getBatteryPercentage() {
        return (battery / MAX_BATTERY) * 100.0;
    }

    public void reset(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.direction = Direction.EAST;
        this.battery = MAX_BATTERY;
        this.isCharging = false;
        this.returningToStation = false;
        this.cleaningStepCount = 0;
        clearPathHistory();
    }
}
