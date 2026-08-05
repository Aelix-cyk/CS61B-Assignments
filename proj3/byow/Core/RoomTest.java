package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class RoomTest {

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
    public void testDrawFillsInteriorWithFloor() {
        Room room = new Room(new Position(3, 4), 5, 6);
        TETile[][] world = emptyWorld(20, 20);
        room.draw(world);
        for (int x = 3; x < 3 + 5; x += 1) {
            for (int y = 4; y < 4 + 6; y += 1) {
                assertTrue("interior tile (" + x + "," + y + ") should be FLOOR",
                        world[x][y] == Tileset.FLOOR);
            }
        }
    }

    @Test
    public void testDrawLeavesRingUnpainted() {
        Room room = new Room(new Position(3, 4), 5, 6);
        TETile[][] world = emptyWorld(20, 20);
        room.draw(world);
        // the 1-tile ring around the interior must stay NOTHING (walls come from the global pass)
        for (int x = 2; x <= 9; x += 1) {
            assertTrue("tile above top edge should be NOTHING", world[x][10] == Tileset.NOTHING);
            assertTrue("tile below bottom edge should be NOTHING", world[x][3] == Tileset.NOTHING);
        }
        for (int y = 4; y <= 9; y += 1) {
            assertTrue("tile left of left edge should be NOTHING", world[2][y] == Tileset.NOTHING);
            assertTrue("tile right of right edge should be NOTHING", world[9][y] == Tileset.NOTHING);
        }
    }

    @Test
    public void testOverlapsIdenticalRoom() {
        Room a = new Room(new Position(3, 4), 5, 6);
        Room b = new Room(new Position(3, 4), 5, 6);
        assertTrue(a.overlaps(b));
    }

    @Test
    public void testOverlapsIntersectingInteriors() {
        Room a = new Room(new Position(3, 4), 5, 6);
        Room b = new Room(new Position(5, 4), 5, 6);
        assertTrue(a.overlaps(b));
    }

    @Test
    public void testOverlapsAdjacentInteriorsRejected() {
        // A occupies x in [3, 7]; its expansion reaches x = 8.
        // B starts at x = 8; its expansion reaches x = 7. Expansions touch -> reject.
        Room a = new Room(new Position(3, 4), 5, 6);
        Room b = new Room(new Position(8, 4), 5, 6);
        assertTrue(a.overlaps(b));
    }

    @Test
    public void testNoOverlapWhenOneTileGapBetweenExpansions() {
        // A interior ends at x = 7, expansion at x = 8.
        // B interior starts at x = 9, expansion at x = 8. Expansions touch at x=8 -> reject.
        // So a true non-overlap needs B interior at x = 10 (expansion at x = 9, no touch).
        Room a = new Room(new Position(3, 4), 5, 6);
        Room b = new Room(new Position(10, 4), 5, 6);
        assertFalse(a.overlaps(b));
    }

    @Test
    public void testRandomDoorPositionIsOnBorderAndNotCorner() {
        Room room = new Room(new Position(3, 4), 5, 6);
        Random random = new Random(42);
        for (int i = 0; i < 200; i += 1) {
            Position p = room.randomDoorPosition(random);
            boolean onBorder = p.x == 3 || p.x == 7 || p.y == 4 || p.y == 9;
            assertTrue("door " + p + " should be on the interior border", onBorder);
            boolean isCorner = (p.x == 3 && p.y == 4) || (p.x == 3 && p.y == 9)
                    || (p.x == 7 && p.y == 4) || (p.x == 7 && p.y == 9);
            assertFalse("door " + p + " must not be a corner", isCorner);
        }
    }

    @Test
    public void testRandomDoorPositionDeterministic() {
        Room room = new Room(new Position(3, 4), 5, 6);
        Random r1 = new Random(42);
        Random r2 = new Random(42);
        for (int i = 0; i < 50; i += 1) {
            assertEquals(room.randomDoorPosition(r1), room.randomDoorPosition(r2));
        }
    }
}
