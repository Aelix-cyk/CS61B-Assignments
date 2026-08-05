package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import static org.junit.Assert.*;

public class WorldGeneratorTest {

    private static final int WIDTH = 80;
    private static final int HEIGHT = 30;

    @Test
    public void testDeterministicSameSeed() {
        TETile[][] a = WorldGenerator.generateWorld(WIDTH, HEIGHT, 12345L);
        TETile[][] b = WorldGenerator.generateWorld(WIDTH, HEIGHT, 12345L);
        assertEquals(TETile.toString(a), TETile.toString(b));
    }

    @Test
    public void testDifferentSeedsProduceDifferentWorlds() {
        TETile[][] base = WorldGenerator.generateWorld(WIDTH, HEIGHT, 0L);
        boolean anyDifferent = false;
        for (long seed = 1; seed < 10; seed += 1) {
            TETile[][] w = WorldGenerator.generateWorld(WIDTH, HEIGHT, seed);
            if (!TETile.toString(w).equals(TETile.toString(base))) {
                anyDifferent = true;
                break;
            }
        }
        assertTrue("different seeds should produce different worlds", anyDifferent);
    }

    @Test
    public void testWorldNotEmpty() {
        TETile[][] world = WorldGenerator.generateWorld(WIDTH, HEIGHT, 42L);
        boolean hasFloor = false;
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (world[x][y] == Tileset.FLOOR) {
                    hasFloor = true;
                }
            }
        }
        assertTrue("world should contain floor tiles", hasFloor);
    }

    @Test
    public void testWallsExist() {
        TETile[][] world = WorldGenerator.generateWorld(WIDTH, HEIGHT, 42L);
        int wallCount = 0;
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (world[x][y] == Tileset.WALL) {
                    wallCount += 1;
                }
            }
        }
        assertTrue("world should contain wall tiles, got " + wallCount, wallCount > 0);
    }

    @Test
    public void testNoFloorHasNothingNeighbor() {
        TETile[][] world = WorldGenerator.generateWorld(WIDTH, HEIGHT, 42L);
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (world[x][y] != Tileset.FLOOR) {
                    continue;
                }
                assertNotEquals("left neighbor of floor at (" + x + "," + y + ") is NOTHING",
                        Tileset.NOTHING, neighborOrWall(world, x - 1, y));
                assertNotEquals("right neighbor of floor at (" + x + "," + y + ") is NOTHING",
                        Tileset.NOTHING, neighborOrWall(world, x + 1, y));
                assertNotEquals("below neighbor of floor at (" + x + "," + y + ") is NOTHING",
                        Tileset.NOTHING, neighborOrWall(world, x, y - 1));
                assertNotEquals("above neighbor of floor at (" + x + "," + y + ") is NOTHING",
                        Tileset.NOTHING, neighborOrWall(world, x, y + 1));
            }
        }
    }

    @Test
    public void testAllFloorConnected() {
        TETile[][] world = WorldGenerator.generateWorld(WIDTH, HEIGHT, 42L);
        Position start = null;
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (world[x][y] == Tileset.FLOOR) {
                    start = new Position(x, y);
                    break;
                }
            }
            if (start != null) {
                break;
            }
        }
        assertNotNull("world should have at least one floor tile", start);

        int reachable = floodFillCount(world, start);
        int totalFloor = countTile(world, Tileset.FLOOR);
        assertEquals("all floor tiles must be reachable from " + start, totalFloor, reachable);
    }

    @Test
    public void testNoOutOfBoundsAcrossSeedsAndSizes() {
        int[] widths = {30, 60, 80};
        int[] heights = {20, 30, 40};
        for (int w : widths) {
            for (int h : heights) {
                for (long seed = 0; seed < 5; seed += 1) {
                    TETile[][] world = WorldGenerator.generateWorld(w, h, seed);
                    assertEquals(w, world.length);
                    assertEquals(h, world[0].length);
                }
            }
        }
    }

    /** Returns the tile at (x,y), or WALL if out of bounds (treated as a wall for the neighbor check). */
    private static TETile neighborOrWall(TETile[][] world, int x, int y) {
        if (x < 0 || x >= world.length || y < 0 || y >= world[0].length) {
            return Tileset.WALL;
        }
        return world[x][y];
    }

    private static int floodFillCount(TETile[][] world, Position start) {
        int width = world.length;
        int height = world[0].length;
        Set<Position> visited = new HashSet<>();
        Queue<Position> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            Position p = queue.remove();
            int[] dx = {1, -1, 0, 0};
            int[] dy = {0, 0, 1, -1};
            for (int i = 0; i < 4; i += 1) {
                int nx = p.x + dx[i];
                int ny = p.y + dy[i];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }
                Position np = new Position(nx, ny);
                if (world[nx][ny] == Tileset.FLOOR && !visited.contains(np)) {
                    visited.add(np);
                    queue.add(np);
                }
            }
        }
        return visited.size();
    }

    private static int countTile(TETile[][] world, TETile tile) {
        int count = 0;
        for (int x = 0; x < world.length; x += 1) {
            for (int y = 0; y < world[0].length; y += 1) {
                if (world[x][y] == tile) {
                    count += 1;
                }
            }
        }
        return count;
    }
}
