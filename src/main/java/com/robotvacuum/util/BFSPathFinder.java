package com.robotvacuum.util;

import com.robotvacuum.model.Room;

import java.util.*;

/**
 * BFS (Breadth-First Search) pathfinder.
 * Used to find the shortest path from robot to charging station.
 */
public class BFSPathFinder {

    /**
     * Finds the shortest path from (startX, startY) to (goalX, goalY)
     * avoiding obstacles.
     *
     * @return List of [x, y] steps from start (exclusive) to goal (inclusive),
     *         or empty list if no path exists.
     */
    public static List<int[]> findPath(Room room, int startX, int startY, int goalX, int goalY) {
        if (startX == goalX && startY == goalY) return Collections.emptyList();

        int cols = room.getCols();
        int rows = room.getRows();

        // Visited array and parent tracking
        boolean[][] visited = new boolean[cols][rows];
        int[][] parentX = new int[cols][rows];
        int[][] parentY = new int[cols][rows];

        for (int[] row : parentX) Arrays.fill(row, -1);
        for (int[] row : parentY) Arrays.fill(row, -1);

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        boolean found = false;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0], cy = current[1];

            if (cx == goalX && cy == goalY) {
                found = true;
                break;
            }

            for (int d = 0; d < 4; d++) {
                int nx = cx + dx[d];
                int ny = cy + dy[d];

                if (!room.isInBounds(nx, ny)) continue;
                if (visited[nx][ny]) continue;
                // Allow passing through all non-obstacle cells (including charging station)
                if (!room.isPassable(nx, ny) && !(nx == goalX && ny == goalY)) continue;

                visited[nx][ny] = true;
                parentX[nx][ny] = cx;
                parentY[nx][ny] = cy;
                queue.add(new int[]{nx, ny});
            }
        }

        if (!found) return Collections.emptyList();

        // Reconstruct path
        List<int[]> path = new ArrayList<>();
        int cx = goalX, cy = goalY;
        while (!(cx == startX && cy == startY)) {
            path.add(0, new int[]{cx, cy});
            int px = parentX[cx][cy];
            int py = parentY[cx][cy];
            cx = px;
            cy = py;
        }
        return path;
    }
}
