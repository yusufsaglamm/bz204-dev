package com.robotvacuum.model;

import com.robotvacuum.util.BFSPathFinder;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Central simulation model (MVC - Model layer).
 * Holds the room, robot, and all simulation state.
 * Exposes JavaFX properties for easy view binding.
 */
public class SimulationModel {

    // --- Room & Robot ---
    private final Room room;
    private final Robot robot;

    // --- Observable properties for View binding ---
    private final DoubleProperty batteryProperty = new SimpleDoubleProperty(100.0);
    private final StringProperty positionProperty = new SimpleStringProperty("(0, 0)");
    private final StringProperty directionProperty = new SimpleStringProperty("Doğu (→)");
    private final IntegerProperty cleanedAreaProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty totalAreaProperty = new SimpleIntegerProperty(1);
    private final IntegerProperty dirtyAreaProperty = new SimpleIntegerProperty(0);
    private final StringProperty elapsedTimeProperty = new SimpleStringProperty("00:00");
    private final StringProperty statusProperty = new SimpleStringProperty("Hazır");
    private final DoubleProperty collectedDustProperty = new SimpleDoubleProperty(0);

    // --- Simulation state ---
    private boolean running = false;
    private boolean paused = false;
    private long simulationStartTime = 0;
    private long pausedElapsedMs = 0;
    private long pauseStartTime = 0;

    // --- Settings ---
    private CleaningAlgorithm algorithm = CleaningAlgorithm.SPIRAL;
    private DirtType selectedDirtType = DirtType.DUST;
    private double speedMultiplier = 1.0;

    // --- Algorithm state ---
    private final Random random = new Random();
    private int spiralRadius = 0;
    private int spiralStep = 0;
    private int spiralStepsInLeg = 1;
    private int spiralLegCount = 0;
    private Direction spiralDirection = Direction.EAST;

    // --- Path following (BFS return to station) ---
    private Queue<int[]> plannedPath = new LinkedList<>();

    // --- Cleaning in progress ---
    private boolean cleaningInProgress = false;
    private int cleaningStepsRemaining = 0;
    private double pendingBatteryCost = 0;

    // --- Statistics ---
    private double totalDustCollected = 0;
    private int totalMoves = 0;
    private int initialDirtyCellCount = 0;

    public SimulationModel() {
        room = new Room();
        robot = new Robot(room.getChargingStationX(), room.getChargingStationY());
        updateProperties();
    }

    // ==================== SIMULATION CONTROL ====================

    public void start() {
        if (!running) {
            running = true;
            paused = false;
            simulationStartTime = System.currentTimeMillis();
            initialDirtyCellCount = room.getDirtyCellCount();
            statusProperty.set("Çalışıyor");
        } else if (paused) {
            paused = false;
            pausedElapsedMs += System.currentTimeMillis() - pauseStartTime;
            statusProperty.set("Çalışıyor");
        }
    }

    public void pause() {
        if (running && !paused) {
            paused = true;
            pauseStartTime = System.currentTimeMillis();
            statusProperty.set("Duraklatıldı");
        }
    }

    public void reset() {
        running = false;
        paused = false;
        simulationStartTime = 0;
        pausedElapsedMs = 0;
        cleaningInProgress = false;
        cleaningStepsRemaining = 0;
        totalDustCollected = 0;
        totalMoves = 0;
        spiralRadius = 0;
        spiralStep = 0;
        spiralStepsInLeg = 1;
        spiralLegCount = 0;
        spiralDirection = Direction.EAST;
        plannedPath.clear();

        room.reset();
        robot.reset(room.getChargingStationX(), room.getChargingStationY());
        statusProperty.set("Sıfırlandı");
        updateProperties();
    }

    public void returnToStation() {
        if (!running || paused) return;
        planPathToStation();
        robot.setReturningToStation(true);
        statusProperty.set("İstasyona Dönüyor");
    }

    // ==================== SIMULATION TICK ====================

    /**
     * Called every animation frame tick. Advances simulation by one step.
     */
    public void tick() {
        if (!running || paused) return;

        // Update elapsed time
        updateElapsedTime();

        // If battery is empty, stop
        if (robot.isBatteryEmpty()) {
            robot.setCharging(false);
            statusProperty.set("Batarya Bitti!");
            running = false;
            return;
        }

        // Handle charging at station
        if (robot.isCharging()) {
            robot.chargeBattery(1.5);
            batteryProperty.set(robot.getBattery());
            if (robot.getBattery() >= Robot.MAX_BATTERY) {
                robot.setCharging(false);
                robot.setReturningToStation(false);
                plannedPath.clear();
                statusProperty.set("Şarj Tamamlandı - Çalışıyor");
            }
            return;
        }

        // Handle cleaning in progress (multi-step dirt)
        if (cleaningInProgress) {
            cleaningStepsRemaining--;
            robot.drainBattery(pendingBatteryCost);
            batteryProperty.set(robot.getBattery());
            if (cleaningStepsRemaining <= 0) {
                cleaningInProgress = false;
                room.getCell(robot.getX(), robot.getY()).clean();
                totalDustCollected++;
                collectedDustProperty.set(totalDustCollected);
                updateStats();
            }
            return;
        }

        // Check if battery is low - return to station
        if (robot.isBatteryLow() && !robot.isReturningToStation()) {
            returnToStation();
        }

        // Move robot
        if (robot.isReturningToStation()) {
            moveAlongPath();
        } else {
            moveByAlgorithm();
        }

        // Clean current cell
        Cell currentCell = room.getCell(robot.getX(), robot.getY());
        if (currentCell != null && currentCell.hasDirt()) {
            DirtType dirt = currentCell.getDirtType();
            int steps = dirt.getCleaningSteps();
            double costPerStep = Robot.MOVE_BATTERY_COST * dirt.getBatteryCostMultiplier();
            if (steps > 1) {
                cleaningInProgress = true;
                cleaningStepsRemaining = steps - 1;
                pendingBatteryCost = costPerStep;
                currentCell.clean(); // first step
            } else {
                currentCell.clean();
                robot.drainBattery(costPerStep);
                totalDustCollected++;
                collectedDustProperty.set(totalDustCollected);
            }
            updateStats();
        } else {
            currentCell.setCleaned(true);
        }

        updateProperties();
    }

    // ==================== MOVEMENT ====================

    private void moveAlongPath() {
        if (plannedPath.isEmpty()) {
            // Reached station
            if (robot.getX() == room.getChargingStationX() && robot.getY() == room.getChargingStationY()) {
                robot.setCharging(true);
                statusProperty.set("Şarj Oluyor...");
            } else {
                robot.setReturningToStation(false);
                statusProperty.set("Çalışıyor");
            }
            return;
        }

        int[] next = plannedPath.poll();
        robot.drainBattery(Robot.MOVE_BATTERY_COST);
        robot.setPosition(next[0], next[1]);
        updateDirectionFromMove(next[0], next[1]);
        totalMoves++;
    }

    private void planPathToStation() {
        List<int[]> path = BFSPathFinder.findPath(
            room, robot.getX(), robot.getY(),
            room.getChargingStationX(), room.getChargingStationY()
        );
        plannedPath.clear();
        plannedPath.addAll(path);
    }

    private void updateDirectionFromMove(int newX, int newY) {
        int dx = newX - robot.getX();
        int dy = newY - robot.getY();
        for (Direction d : Direction.values()) {
            if (d.getDx() == dx && d.getDy() == dy) {
                robot.setDirection(d);
                break;
            }
        }
    }

    private void moveByAlgorithm() {
        switch (algorithm) {
            case RANDOM -> moveRandom();
            case SPIRAL -> moveSpiral();
            case WALL_FOLLOW -> moveWallFollow();
        }
    }

    private void moveRandom() {
        List<Direction> available = getAvailableDirections();
        if (available.isEmpty()) return;

        // Prefer current direction to reduce zigzagging
        if (available.contains(robot.getDirection()) && random.nextDouble() > 0.3) {
            performMove(robot.getDirection());
        } else {
            Direction chosen = available.get(random.nextInt(available.size()));
            robot.setDirection(chosen);
            performMove(chosen);
        }
    }

    private void moveSpiral() {
        // Outward spiral: move in a direction for increasing leg lengths
        if (canMove(spiralDirection)) {
            performMove(spiralDirection);
            spiralStep++;
            if (spiralStep >= spiralStepsInLeg) {
                spiralStep = 0;
                spiralLegCount++;
                spiralDirection = spiralDirection.turnRight();
                if (spiralLegCount % 2 == 0) spiralStepsInLeg++;
            }
        } else {
            // Hit obstacle, try turning right, then left, then reverse
            Direction right = spiralDirection.turnRight();
            Direction left = spiralDirection.turnLeft();
            if (canMove(right)) {
                spiralDirection = right;
                spiralStep = 0;
                performMove(spiralDirection);
            } else if (canMove(left)) {
                spiralDirection = left;
                spiralStep = 0;
                performMove(spiralDirection);
            } else {
                // Fallback to random
                List<Direction> available = getAvailableDirections();
                if (!available.isEmpty()) {
                    spiralDirection = available.get(random.nextInt(available.size()));
                    performMove(spiralDirection);
                }
            }
        }
    }

    private void moveWallFollow() {
        // Wall-following (right-hand rule): try to keep a wall on the right
        Direction rightOfCurrent = robot.getDirection().turnRight();
        Direction current = robot.getDirection();
        Direction leftOfCurrent = robot.getDirection().turnLeft();
        Direction back = robot.getDirection().opposite();

        if (canMove(rightOfCurrent)) {
            robot.setDirection(rightOfCurrent);
            performMove(rightOfCurrent);
        } else if (canMove(current)) {
            performMove(current);
        } else if (canMove(leftOfCurrent)) {
            robot.setDirection(leftOfCurrent);
            performMove(leftOfCurrent);
        } else if (canMove(back)) {
            robot.setDirection(back);
            performMove(back);
        }
        // Completely stuck - no move
    }

    private boolean canMove(Direction d) {
        int nx = robot.getX() + d.getDx();
        int ny = robot.getY() + d.getDy();
        return room.isPassable(nx, ny);
    }

    private void performMove(Direction d) {
        int nx = robot.getX() + d.getDx();
        int ny = robot.getY() + d.getDy();
        if (room.isPassable(nx, ny)) {
            robot.setDirection(d);
            robot.drainBattery(Robot.MOVE_BATTERY_COST);
            robot.setPosition(nx, ny);
            totalMoves++;
        } else {
            // Turn on collision
            robot.setDirection(d.turnRight());
        }
    }

    private List<Direction> getAvailableDirections() {
        List<Direction> dirs = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (canMove(d)) dirs.add(d);
        }
        return dirs;
    }

    // ==================== PROPERTIES & STATS ====================

    private void updateProperties() {
        batteryProperty.set(robot.getBattery());
        positionProperty.set("(" + robot.getX() + ", " + robot.getY() + ")");
        directionProperty.set(robot.getDirection().getDisplayName());
        totalAreaProperty.set(room.getTotalFloorCells());
        updateStats();
    }

    private void updateStats() {
        cleanedAreaProperty.set(room.getCleanedCellCount());
        dirtyAreaProperty.set(room.getDirtyCellCount());
    }

    private void updateElapsedTime() {
        if (simulationStartTime == 0) return;
        long totalMs = System.currentTimeMillis() - simulationStartTime - pausedElapsedMs;
        long seconds = totalMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        elapsedTimeProperty.set(String.format("%02d:%02d", minutes, seconds));
    }

    // ==================== GETTERS / SETTERS ====================

    public Room getRoom() { return room; }
    public Robot getRobot() { return robot; }

    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }

    public CleaningAlgorithm getAlgorithm() { return algorithm; }
    public void setAlgorithm(CleaningAlgorithm algorithm) {
        this.algorithm = algorithm;
        spiralRadius = 0; spiralStep = 0;
        spiralStepsInLeg = 1; spiralLegCount = 0;
        spiralDirection = Direction.EAST;
    }

    public DirtType getSelectedDirtType() { return selectedDirtType; }
    public void setSelectedDirtType(DirtType type) { this.selectedDirtType = type; }

    public double getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(double speedMultiplier) { this.speedMultiplier = speedMultiplier; }

    // Observable properties
    public DoubleProperty batteryProperty() { return batteryProperty; }
    public StringProperty positionProperty() { return positionProperty; }
    public StringProperty directionProperty() { return directionProperty; }
    public IntegerProperty cleanedAreaProperty() { return cleanedAreaProperty; }
    public IntegerProperty totalAreaProperty() { return totalAreaProperty; }
    public IntegerProperty dirtyAreaProperty() { return dirtyAreaProperty; }
    public StringProperty elapsedTimeProperty() { return elapsedTimeProperty; }
    public StringProperty statusProperty() { return statusProperty; }
    public DoubleProperty collectedDustProperty() { return collectedDustProperty; }

    public int getTotalInitialDirt() {
        return Math.max(1, initialDirtyCellCount > 0 ? initialDirtyCellCount : room.getDirtyCellCount() + (int) totalDustCollected);
    }
}
