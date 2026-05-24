package com.robotvacuum.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the room as a 2D grid of cells.
 * Manages obstacles, dirt, and the charging station.
 */
public class Room {

    public static final int DEFAULT_COLS = 20;
    public static final int DEFAULT_ROWS = 14;

    private final int cols;
    private final int rows;
    private final Cell[][] grid;
    private int chargingStationX;
    private int chargingStationY;

    public Room(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.grid = new Cell[cols][rows];
        initializeGrid();
        // Default charging station at top-left corner
        setChargingStation(0, 0);
    }

    public Room() {
        this(DEFAULT_COLS, DEFAULT_ROWS);
    }

    private void initializeGrid() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                grid[x][y] = new Cell(x, y);
            }
        }
    }

    public Cell getCell(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return grid[x][y];
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    public boolean isPassable(int x, int y) {
        if (!isInBounds(x, y)) return false;
        return !grid[x][y].isObstacle();
    }

    public void setObstacle(int x, int y) {
        if (!isInBounds(x, y)) return;
        // Cannot place obstacle on charging station
        if (grid[x][y].isChargingStation()) return;
        grid[x][y].setType(Cell.CellType.OBSTACLE);
        grid[x][y].clearDirt();
    }

    public void removeObstacle(int x, int y) {
        if (!isInBounds(x, y)) return;
        if (grid[x][y].isObstacle()) {
            grid[x][y].setType(Cell.CellType.FLOOR);
        }
    }

    public void setChargingStation(int x, int y) {
        if (!isInBounds(x, y)) return;
        // Clear old charging station
        if (isInBounds(chargingStationX, chargingStationY)) {
            Cell old = grid[chargingStationX][chargingStationY];
            if (old.isChargingStation()) {
                old.setType(Cell.CellType.FLOOR);
            }
        }
        chargingStationX = x;
        chargingStationY = y;
        grid[x][y].setType(Cell.CellType.CHARGING_STATION);
        grid[x][y].clearDirt();
    }

    public void addDirt(int x, int y, DirtType type) {
        if (!isInBounds(x, y)) return;
        Cell c = grid[x][y];
        if (c.isObstacle() || c.isChargingStation()) return;
        c.setDirt(type);
    }

    public void reset() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                Cell c = grid[x][y];
                if (!c.isObstacle() && !c.isChargingStation()) {
                    c.clearDirt();
                    c.setCleaned(false);
                }
            }
        }
    }

    public void fullReset() {
        initializeGrid();
        setChargingStation(0, 0);
    }

    public int getCols() { return cols; }
    public int getRows() { return rows; }
    public int getChargingStationX() { return chargingStationX; }
    public int getChargingStationY() { return chargingStationY; }

    /** Returns total walkable cells (non-obstacle) */
    public int getTotalFloorCells() {
        int count = 0;
        for (int x = 0; x < cols; x++)
            for (int y = 0; y < rows; y++)
                if (!grid[x][y].isObstacle()) count++;
        return count;
    }

    /** Returns number of cells that have been cleaned at least once */
    public int getCleanedCellCount() {
        int count = 0;
        for (int x = 0; x < cols; x++)
            for (int y = 0; y < rows; y++)
                if (grid[x][y].isCleaned()) count++;
        return count;
    }

    /** Returns number of cells that still have dirt */
    public int getDirtyCellCount() {
        int count = 0;
        for (int x = 0; x < cols; x++)
            for (int y = 0; y < rows; y++)
                if (grid[x][y].hasDirt()) count++;
        return count;
    }

    /** Returns list of all cells that have dirt */
    public List<Cell> getDirtyCells() {
        List<Cell> dirty = new ArrayList<>();
        for (int x = 0; x < cols; x++)
            for (int y = 0; y < rows; y++)
                if (grid[x][y].hasDirt()) dirty.add(grid[x][y]);
        return dirty;
    }
}
