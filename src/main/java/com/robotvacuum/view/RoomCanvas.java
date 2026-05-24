package com.robotvacuum.view;

import com.robotvacuum.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Canvas component that renders the room grid, robot, dirt, obstacles, and path.
 */
public class RoomCanvas extends Canvas {

    private static final int CELL_SIZE = 40;
    private static final Color COLOR_FLOOR = Color.rgb(245, 245, 240);
    private static final Color COLOR_FLOOR_ALT = Color.rgb(238, 238, 230);
    private static final Color COLOR_OBSTACLE = Color.rgb(80, 80, 90);
    private static final Color COLOR_CHARGING = Color.rgb(255, 200, 50);
    private static final Color COLOR_GRID = Color.rgb(200, 200, 195);
    private static final Color COLOR_CLEANED = Color.rgb(200, 230, 255, 0.45);
    private static final Color COLOR_PATH = Color.rgb(100, 160, 220, 0.55);
    private static final Color COLOR_DUST = Color.rgb(180, 150, 100);
    private static final Color COLOR_LIQUID = Color.rgb(80, 140, 220);
    private static final Color COLOR_STAIN = Color.rgb(160, 60, 60);
    private static final Color COLOR_ROBOT = Color.rgb(40, 40, 50);
    private static final Color COLOR_ROBOT_HIGHLIGHT = Color.rgb(80, 180, 255);

    private final SimulationModel model;
    private boolean showGrid = true;
    private boolean showPath = true;

    public RoomCanvas(SimulationModel model) {
        super(
            Room.DEFAULT_COLS * CELL_SIZE,
            Room.DEFAULT_ROWS * CELL_SIZE
        );
        this.model = model;
    }

    /** Full redraw of the canvas */
    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        Room room = model.getRoom();
        Robot robot = model.getRobot();

        int w = (int) getWidth();
        int h = (int) getHeight();

        // Background
        gc.setFill(Color.rgb(230, 228, 220));
        gc.fillRect(0, 0, w, h);

        // Draw cells
        for (int x = 0; x < room.getCols(); x++) {
            for (int y = 0; y < room.getRows(); y++) {
                drawCell(gc, room, x, y);
            }
        }

        // Draw path history
        if (showPath) {
            drawPath(gc, robot);
        }

        // Draw grid lines
        if (showGrid) {
            drawGrid(gc, room);
        }

        // Draw row/col coordinates
        drawCoordinates(gc, room);

        // Draw robot
        drawRobot(gc, robot);
    }

    private void drawCell(GraphicsContext gc, Room room, int x, int y) {
        Cell cell = room.getCell(x, y);
        double px = x * CELL_SIZE;
        double py = y * CELL_SIZE;

        switch (cell.getType()) {
            case OBSTACLE -> {
                gc.setFill(COLOR_OBSTACLE);
                gc.fillRect(px, py, CELL_SIZE, CELL_SIZE);
                // Furniture texture lines
                gc.setStroke(Color.rgb(60, 60, 70));
                gc.setLineWidth(0.5);
                gc.strokeLine(px + 4, py + 4, px + CELL_SIZE - 4, py + 4);
                gc.strokeLine(px + 4, py + CELL_SIZE - 4, px + CELL_SIZE - 4, py + CELL_SIZE - 4);
            }
            case CHARGING_STATION -> {
                gc.setFill(COLOR_CHARGING);
                gc.fillRect(px, py, CELL_SIZE, CELL_SIZE);
                // Lightning bolt symbol
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", 18));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("⚡", px + CELL_SIZE / 2.0, py + CELL_SIZE / 2.0 + 6);
            }
            default -> {
                // Checkerboard floor
                Color floorColor = (x + y) % 2 == 0 ? COLOR_FLOOR : COLOR_FLOOR_ALT;
                gc.setFill(floorColor);
                gc.fillRect(px, py, CELL_SIZE, CELL_SIZE);

                // Cleaned overlay
                if (cell.isCleaned() && !cell.hasDirt()) {
                    gc.setFill(COLOR_CLEANED);
                    gc.fillRect(px, py, CELL_SIZE, CELL_SIZE);
                }

                // Dirt
                if (cell.hasDirt()) {
                    drawDirt(gc, cell, px, py);
                }
            }
        }
    }

    private void drawDirt(GraphicsContext gc, Cell cell, double px, double py) {
        DirtType type = cell.getDirtType();
        double cx = px + CELL_SIZE / 2.0;
        double cy = py + CELL_SIZE / 2.0;

        switch (type) {
            case DUST -> {
                gc.setFill(COLOR_DUST);
                // Multiple small dots to represent dust
                double[] dotsX = {cx - 8, cx, cx + 8, cx - 5, cx + 5};
                double[] dotsY = {cy - 5, cy - 8, cy - 5, cy + 5, cy + 5};
                for (int i = 0; i < dotsX.length; i++) {
                    gc.fillOval(dotsX[i] - 2.5, dotsY[i] - 2.5, 5, 5);
                }
            }
            case LIQUID -> {
                gc.setFill(COLOR_LIQUID);
                // Irregular splash shape
                gc.fillOval(cx - 10, cy - 6, 20, 12);
                gc.fillOval(cx - 5, cy - 10, 10, 20);
                gc.setFill(Color.rgb(150, 200, 255, 0.6));
                gc.fillOval(cx - 3, cy - 3, 6, 6);
            }
            case STAIN -> {
                gc.setFill(COLOR_STAIN);
                // Irregular stain
                gc.fillOval(cx - 11, cy - 8, 22, 16);
                gc.setFill(Color.rgb(200, 80, 80, 0.6));
                gc.fillOval(cx - 5, cy - 4, 10, 8);
            }
        }

        // Show remaining cleaning steps as small indicator
        int remaining = cell.getRemainingCleaningSteps();
        if (remaining > 1) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial Bold", 9));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(remaining), cx, py + CELL_SIZE - 4);
        }
    }

    private void drawPath(GraphicsContext gc, Robot robot) {
        List<int[]> history = robot.getPathHistory();
        if (history.size() < 2) return;

        gc.setStroke(COLOR_PATH);
        gc.setLineWidth(2.5);
        gc.setLineDashes(4, 4);

        double offset = CELL_SIZE / 2.0;
        for (int i = 1; i < history.size(); i++) {
            int[] prev = history.get(i - 1);
            int[] curr = history.get(i);
            gc.strokeLine(
                prev[0] * CELL_SIZE + offset,
                prev[1] * CELL_SIZE + offset,
                curr[0] * CELL_SIZE + offset,
                curr[1] * CELL_SIZE + offset
            );
        }
        gc.setLineDashes(null);
    }

    private void drawGrid(GraphicsContext gc, Room room) {
        gc.setStroke(COLOR_GRID);
        gc.setLineWidth(0.5);
        int totalW = room.getCols() * CELL_SIZE;
        int totalH = room.getRows() * CELL_SIZE;

        for (int x = 0; x <= room.getCols(); x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, totalH);
        }
        for (int y = 0; y <= room.getRows(); y++) {
            gc.strokeLine(0, y * CELL_SIZE, totalW, y * CELL_SIZE);
        }
    }

    private void drawCoordinates(GraphicsContext gc, Room room) {
        gc.setFill(Color.rgb(150, 150, 150));
        gc.setFont(Font.font("Monospace", 9));
        gc.setTextAlign(TextAlignment.CENTER);

        // Column numbers top
        for (int x = 0; x < room.getCols(); x++) {
            gc.fillText(String.valueOf(x), x * CELL_SIZE + CELL_SIZE / 2.0, 10);
        }
        // Row numbers left
        for (int y = 0; y < room.getRows(); y++) {
            gc.fillText(String.valueOf(y), 10, y * CELL_SIZE + CELL_SIZE / 2.0 + 4);
        }
    }

    private void drawRobot(GraphicsContext gc, Robot robot) {
        double px = robot.getX() * CELL_SIZE;
        double py = robot.getY() * CELL_SIZE;
        double cx = px + CELL_SIZE / 2.0;
        double cy = py + CELL_SIZE / 2.0;
        double radius = CELL_SIZE * 0.38;

        // Shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillOval(cx - radius + 2, cy - radius + 2, radius * 2, radius * 2);

        // Body
        gc.setFill(COLOR_ROBOT);
        gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Top highlight
        gc.setFill(COLOR_ROBOT_HIGHLIGHT);
        gc.fillOval(cx - radius + 3, cy - radius + 3, radius * 0.8, radius * 0.8);

        // Direction indicator
        double dirLength = radius * 0.75;
        double dirX = cx + robot.getDirection().getDx() * dirLength;
        double dirY = cy + robot.getDirection().getDy() * dirLength;
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeLine(cx, cy, dirX, dirY);

        // Battery ring (colored arc)
        double battPct = robot.getBattery() / Robot.MAX_BATTERY;
        Color battColor = battPct > 0.5 ? Color.rgb(50, 200, 100)
                        : battPct > 0.2 ? Color.rgb(255, 180, 50)
                        : Color.rgb(220, 60, 60);
        gc.setStroke(battColor);
        gc.setLineWidth(2);
        double arcAngle = battPct * 360;
        gc.strokeArc(cx - radius - 3, cy - radius - 3,
                     (radius + 3) * 2, (radius + 3) * 2,
                     90, -arcAngle, javafx.scene.shape.ArcType.OPEN);

        // Charging indicator
        if (robot.isCharging()) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Arial", 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("⚡", cx, cy - radius - 5);
        }
    }

    public int getCellSize() { return CELL_SIZE; }
    public void setShowGrid(boolean showGrid) { this.showGrid = showGrid; }
    public void setShowPath(boolean showPath) { this.showPath = showPath; }

    /** Convert canvas X pixel to grid column */
    public int pixelToCol(double pixelX) {
        return (int) (pixelX / CELL_SIZE);
    }

    /** Convert canvas Y pixel to grid row */
    public int pixelToRow(double pixelY) {
        return (int) (pixelY / CELL_SIZE);
    }
}
