package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class HallwayTest {

    private static TETile[][] emptyWorld(int width, int height) {
        TETile[][] world = new TETile[width][height];
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        return world;
    }

    @Test
    public void testConstructorStoresEndpoints() {
        Position start = new Position(2, 2);
        Position end = new Position(8, 5);
        Hallway h = new Hallway(start, end, new Random(1));
        assertEquals(start, h.start);
        assertEquals(end, h.end);
    }

    @Test
    public void testWidthIsOneOrTwo() {
        for (int seed = 0; seed < 100; seed += 1) {
            Hallway h = new Hallway(new Position(2, 2), new Position(8, 5), new Random(seed));
            assertTrue("width " + h.width + " must be 1 or 2", h.width == 1 || h.width == 2);
        }
    }

    @Test
    public void testCornerIsValidTurn() {
        for (int seed = 0; seed < 100; seed += 1) {
            Hallway h = new Hallway(new Position(2, 2), new Position(8, 5), new Random(seed));
            boolean horizontalFirst = h.corner.equals(new Position(8, 2));
            boolean verticalFirst = h.corner.equals(new Position(2, 5));
            assertTrue("corner " + h.corner + " must be a valid L turn",
                    horizontalFirst || verticalFirst);
        }
    }

    @Test
    public void testConstructorDeterministic() {
        Random r1 = new Random(42);
        Random r2 = new Random(42);
        for (int i = 0; i < 50; i += 1) {
            Hallway a = new Hallway(new Position(2, 2), new Position(8, 5), r1);
            Hallway b = new Hallway(new Position(2, 2), new Position(8, 5), r2);
            assertEquals(a.corner, b.corner);
            assertEquals(a.width, b.width);
        }
    }

    @Test
    public void testCarveWidthOneCreatesPath() {
        // start (2,2), end (8,5), seed 1 -> check whichever L it picks is fully carved
        Hallway h = new Hallway(new Position(2, 2), new Position(8, 5), new Random(1));
        TETile[][] world = emptyWorld(20, 20);
        h.carve(world);

        // walk the two segments and assert every tile is FLOOR
        assertPathFloor(world, h.start, h.corner);
        assertPathFloor(world, h.corner, h.end);
    }

    @Test
    public void testCarveWidthTwoCreatesStrip() {
        Hallway h = new Hallway(new Position(2, 2), new Position(8, 5), new Random(3));
        TETile[][] world = emptyWorld(20, 20);
        h.carve(world);

        // identify which segment is horizontal vs vertical from the stored corner
        Position hFrom;
        Position hTo;
        Position vFrom;
        Position vTo;
        if (h.start.x == h.corner.x) {
            vFrom = h.start;
            vTo = h.corner;
            hFrom = h.corner;
            hTo = h.end;
        } else {
            hFrom = h.start;
            hTo = h.corner;
            vFrom = h.corner;
            vTo = h.end;
        }

        // horizontal segment: rows hFrom.y .. hFrom.y + width - 1, x in [hFrom.x, hTo.x]
        int xLo = Math.min(hFrom.x, hTo.x);
        int xHi = Math.max(hFrom.x, hTo.x);
        for (int x = xLo; x <= xHi; x += 1) {
            for (int w = 0; w < h.width; w += 1) {
                assertTrue("horizontal strip tile (" + x + "," + (hFrom.y + w) + ") should be FLOOR",
                        world[x][hFrom.y + w] == Tileset.FLOOR);
            }
        }

        // vertical segment: columns vFrom.x .. vFrom.x + width - 1, y in [vFrom.y, vTo.y]
        int yLo = Math.min(vFrom.y, vTo.y);
        int yHi = Math.max(vFrom.y, vTo.y);
        for (int y = yLo; y <= yHi; y += 1) {
            for (int w = 0; w < h.width; w += 1) {
                assertTrue("vertical strip tile (" + (vFrom.x + w) + "," + y + ") should be FLOOR",
                        world[vFrom.x + w][y] == Tileset.FLOOR);
            }
        }
    }

    @Test
    public void testCarveDoesNotOverpaintOutside() {
        // start.y == end.y, so the L degenerates to a single horizontal corridor.
        // Must not paint above the strip, and must not leak past the end door.
        for (int seed = 0; seed < 100; seed += 1) {
            Hallway h = new Hallway(new Position(2, 2), new Position(8, 2), new Random(seed));
            TETile[][] world = emptyWorld(20, 20);
            h.carve(world);
            // the row immediately above the strip (start.y + width) must remain NOTHING
            for (int x = 2; x <= 8; x += 1) {
                assertTrue("tile above corridor should stay NOTHING",
                        world[x][h.start.y + h.width] == Tileset.NOTHING);
            }
            // the tile just past the end door must remain NOTHING (no floor nub)
            assertTrue("tile past end door should stay NOTHING",
                    world[h.end.x + h.width][h.start.y] == Tileset.NOTHING);
        }
    }

    @Test
    public void testCarveClipsAtWorldBounds() {
        // pick a width-2 hallway so the strip genuinely crosses the world edge
        Hallway h = null;
        for (int seed = 0; seed < 200; seed += 1) {
            Hallway candidate = new Hallway(new Position(0, 0), new Position(9, 9),
                    new Random(seed));
            if (candidate.width == 2) {
                h = candidate;
                break;
            }
        }
        assertNotNull("should find a width-2 hallway", h);
        TETile[][] world = emptyWorld(10, 10);
        h.carve(world); // must not throw; out-of-bounds writes are clipped
        for (int x = 0; x < 10; x += 1) {
            for (int y = 0; y < 10; y += 1) {
                assertTrue("in-bounds tiles stay valid",
                        world[x][y] == Tileset.FLOOR || world[x][y] == Tileset.NOTHING);
            }
        }
    }

    private void assertPathFloor(TETile[][] world, Position from, Position to) {
        if (from.x == to.x) {
            int lo = Math.min(from.y, to.y);
            int hi = Math.max(from.y, to.y);
            for (int y = lo; y <= hi; y += 1) {
                assertTrue("vertical tile (" + from.x + "," + y + ") should be FLOOR",
                        world[from.x][y] == Tileset.FLOOR);
            }
        } else {
            int lo = Math.min(from.x, to.x);
            int hi = Math.max(from.x, to.x);
            for (int x = lo; x <= hi; x += 1) {
                assertTrue("horizontal tile (" + x + "," + from.y + ") should be FLOOR",
                        world[x][from.y] == Tileset.FLOOR);
            }
        }
    }
}
