package com.robotvacuum.controller;

import com.robotvacuum.model.*;
import com.robotvacuum.view.MainView;
import javafx.animation.AnimationTimer;

/**
 * Simulation controller (MVC - Controller layer).
 * Handles user input, drives the animation loop, and coordinates model/view.
 */
public class SimulationController {

    private final SimulationModel model;
    private final MainView view;

    private AnimationTimer animationTimer;
    private long lastTickNanos = 0;

    /** Base tick interval in nanoseconds (1 second / 4 = 250ms per step at 1x speed) */
    private static final long BASE_TICK_NS = 250_000_000L;

    /** Current edit mode: "dirt", "obstacle", or null */
    private String editMode = null;

    public SimulationController(SimulationModel model, MainView view) {
        this.model = model;
        this.view = view;
    }

    public void initialize() {
        // Place default obstacles (furniture layout)
        placeDefaultFurniture();

        // Place some default dirt for demo
        placeDefaultDirt();

        // Build animation timer
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long tickIntervalNs = (long) (BASE_TICK_NS / model.getSpeedMultiplier());
                if (now - lastTickNanos >= tickIntervalNs) {
                    lastTickNanos = now;
                    model.tick();
                    view.getRoomCanvas().redraw();
                }
            }
        };
        animationTimer.start();
        view.getRoomCanvas().redraw();
    }

    // ==================== BUTTON ACTIONS ====================

    public void onStart() {
        editMode = null;
        model.start();
    }

    public void onPause() {
        model.pause();
    }

    public void onReset() {
        editMode = null;
        model.reset();
        placeDefaultFurniture();
        placeDefaultDirt();
        view.getRoomCanvas().redraw();
    }

    public void onReturnToStation() {
        model.returnToStation();
    }

    public void onAddDirtMode() {
        editMode = editMode != null && editMode.equals("dirt") ? null : "dirt";
        updateCursor();
    }

    public void onAddObstacleMode() {
        editMode = editMode != null && editMode.equals("obstacle") ? null : "obstacle";
        updateCursor();
    }

    public void onSelectDirtType(DirtType type) {
        model.setSelectedDirtType(type);
    }

    public void onSelectAlgorithm(CleaningAlgorithm algorithm) {
        model.setAlgorithm(algorithm);
    }

    public void onSpeedChange(double speed) {
        model.setSpeedMultiplier(speed);
    }

    public void onManualBatterySet(double value) {
        model.getRobot().setBattery(value);
        model.batteryProperty().set(value);
    }

    // ==================== CANVAS CLICK ====================

    public void onCanvasClick(int col, int row) {
        Room room = model.getRoom();
        if (!room.isInBounds(col, row)) return;

        if ("dirt".equals(editMode)) {
            room.addDirt(col, row, model.getSelectedDirtType());
            view.getRoomCanvas().redraw();
        } else if ("obstacle".equals(editMode)) {
            Cell cell = room.getCell(col, row);
            if (cell.isObstacle()) {
                room.removeObstacle(col, row);
            } else {
                // Don't place obstacle on robot's current position
                if (col == model.getRobot().getX() && row == model.getRobot().getY()) return;
                room.setObstacle(col, row);
            }
            view.getRoomCanvas().redraw();
        }
    }

    // ==================== HELPERS ====================

    private void updateCursor() {
        // Visual feedback for mode could be extended here
    }

    private void placeDefaultFurniture() {
        Room room = model.getRoom();
        // Sofa area (top-center)
        for (int x = 5; x <= 9; x++) room.setObstacle(x, 1);
        for (int x = 5; x <= 9; x++) room.setObstacle(x, 2);

        // Coffee table
        for (int x = 6; x <= 8; x++) {
            room.setObstacle(x, 5);
            room.setObstacle(x, 6);
        }

        // Right side shelf
        for (int y = 0; y <= 3; y++) room.setObstacle(17, y);
        room.setObstacle(18, 0);
        room.setObstacle(18, 1);

        // Small table top-right
        room.setObstacle(15, 5);
        room.setObstacle(15, 6);
        room.setObstacle(16, 5);
        room.setObstacle(16, 6);

        // Bottom left corner table
        room.setObstacle(2, 11);
        room.setObstacle(2, 12);
        room.setObstacle(3, 11);
    }

    private void placeDefaultDirt() {
        Room room = model.getRoom();
        // Scatter some dust
        int[][] dustSpots = {
            {4,3}, {11,3}, {13,4}, {3,7}, {10,10},
            {14,10}, {4,10}, {19,3}, {12,12}, {1,5}
        };
        for (int[] spot : dustSpots) room.addDirt(spot[0], spot[1], DirtType.DUST);

        // Some liquid spills
        int[][] liquidSpots = {{7,8}, {14,7}, {3,4}};
        for (int[] spot : liquidSpots) room.addDirt(spot[0], spot[1], DirtType.LIQUID);

        // A couple of stains
        int[][] stainSpots = {{11,11}, {17,4}};
        for (int[] spot : stainSpots) room.addDirt(spot[0], spot[1], DirtType.STAIN);
    }
}
