package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 60;
    private static final int HEIGHT = 60;

    /**
     * Returns the number of rows between row {@code row} and the nearest horizontal edge
     * (bottom or top) of a hexagon of side length {@code size}.
     */
    private static int rowsFromHorizontalEdge(int size, int row) {
        return Math.min(row, 2 * size - 1 - row);
    }

    /**
     * Returns the width (number of tiles) of row {@code row} for a hexagon of side length
     * {@code size}. Row 0 is the bottom row, row {@code 2*size - 1} is the top row.
     */
    private static int rowWidth(int size, int row) {
        return size + 2 * rowsFromHorizontalEdge(size, row);
    }

    /**
     * Returns the horizontal offset of row {@code row} relative to the anchor x of a hexagon
     * of side length {@code size}. The bounding box's left edge is offset 0.
     */
    private static int rowXOffset(int size, int row) {
        return (size - 1) - rowsFromHorizontalEdge(size, row);
    }

    /**
     * Draws a hexagon of side length {@code size} into {@code world}, with the bottom-left
     * corner of its bounding box at {@code p}. Writes outside the world bounds are skipped.
     */
    public static void addHexagon(Position p, int size, TETile tile, TETile[][] world) {
        for (int row = 0; row < 2 * size; row += 1) {
            int width = rowWidth(size, row);
            int xOffset = rowXOffset(size, row);
            int startX = p.x + xOffset;
            int rowY = p.y + row;
            if (rowY >= world[0].length || rowY < 0) {
                continue;
            }
            for (int col = 0; col < width; col += 1) {
                int worldX = startX + col;
                if (worldX >= world.length || worldX < 0) {
                    continue;
                }
                world[worldX][rowY] = tile;
            }
        }
    }

    /**
     * Returns the position of the neighbor of a hexagon of side length {@code size}
     * anchored at {@code p}, in the given {@code direction}.
     */
    public static Position neighborPosition(Position p, int size, Direction direction) {
        switch (direction) {
            case N:  return new Position(p.x, p.y + 2 * size);
            case NE: return new Position(p.x + (2 * size - 1), p.y + size);
            case NW: return new Position(p.x - (2 * size - 1), p.y + size);
            case S:  return new Position(p.x, p.y - 2 * size);
            case SE: return new Position(p.x + (2 * size - 1), p.y - size);
            case SW: return new Position(p.x - (2 * size - 1), p.y - size);
            default: throw new IllegalArgumentException("Unexpected direction: " + direction);
        }
    }

    /**
     * Draws a 19-hexagon tessellation into {@code world} by growing outward from a single
     * center hexagon anchored at {@code center}. Each round of growth adds all six neighbors
     * of every hexagon placed in the previous round (deduplicated), so the hexagon count
     * grows 1 -> 7 -> 19. Hexagons are drawn with tiles cycled from {@code tiles} in the
     * order they are placed, so each one can be told apart.
     */
    public static void addTessellation(Position center, int size, TETile[] tiles, TETile[][] world) {
        Set<Position> placed = new HashSet<>();
        Set<Position> frontier = new HashSet<>();
        placed.add(center);
        frontier.add(center);
        addHexagon(center, size, tiles[0], world);

        int rounds = 2;
        int placedCount = 1;
        for (int round = 0; round < rounds; round += 1) {
            Set<Position> nextFrontier = new HashSet<>();
            for (Position p : frontier) {
                for (Direction d : Direction.values()) {
                    Position neighbor = neighborPosition(p, size, d);
                    if (placed.add(neighbor)) {
                        nextFrontier.add(neighbor);
                        addHexagon(neighbor, size, tiles[placedCount % tiles.length], world);
                        placedCount += 1;
                    }
                }
            }
            frontier = nextFrontier;
        }
    }

    public static void main(String[] args) {
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        // initialize tiles
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        // draw a tessellation of 19 size-3 hexagons, centered near the middle of the world,
        // each hexagon drawn with a different tile so it can be told apart from its neighbors
        TETile[] tiles = {Tileset.FLOWER, Tileset.GRASS, Tileset.WALL, Tileset.SAND,
                          Tileset.MOUNTAIN, Tileset.TREE};
        addTessellation(new Position(30, 15), 3, tiles, world);

        // draws the world to the screen
        ter.renderFrame(world);
    }
}
