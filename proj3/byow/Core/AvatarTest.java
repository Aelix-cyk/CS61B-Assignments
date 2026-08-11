package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;
import org.junit.Test;

import static org.junit.Assert.*;

public class AvatarTest {

    /** Builds a small world: floor inside a wall border, so we can test blocking. */
    private static TETile[][] smallWorld() {
        TETile[][] world = new TETile[5][5];
        for (int x = 0; x < 5; x += 1) {
            for (int y = 0; y < 5; y += 1) {
                world[x][y] = (x == 0 || x == 4 || y == 0 || y == 4)
                        ? Tileset.WALL : Tileset.FLOOR;
            }
        }
        return world;
    }

    @Test
    public void testSpawnStored() {
        TETile[][] world = smallWorld();
        Avatar a = new Avatar(new Position(2, 2), world);
        assertEquals(new Position(2, 2), a.pos());
    }

    @Test
    public void testSpawnStampsAvatarTile() {
        TETile[][] world = smallWorld();
        new Avatar(new Position(2, 2), world);
        assertEquals(Tileset.AVATAR, world[2][2]);
    }

    @Test
    public void testMoveUpOntoFloor() {
        TETile[][] world = smallWorld();
        Avatar a = new Avatar(new Position(2, 2), world);
        a.move('w', world);
        assertEquals(new Position(2, 3), a.pos());
    }

    @Test
    public void testMoveLeavesFloorBehind() {
        TETile[][] world = smallWorld();
        Avatar a = new Avatar(new Position(2, 2), world);
        a.move('w', world);
        assertEquals(Tileset.FLOOR, world[2][2]);      // old cell restored
        assertEquals(Tileset.AVATAR, world[2][3]);     // new cell stamped
    }

    @Test
    public void testMoveDownOntoFloor() {
        TETile[][] world = smallWorld();
        Avatar a = new Avatar(new Position(2, 2), world);
        a.move('s', world);
        assertEquals(new Position(2, 1), a.pos());
    }

    @Test
    public void testMoveLeftOntoFloor() {
        TETile[][] world = smallWorld();
        Avatar a = new Avatar(new Position(2, 2), world);
        a.move('a', world);
        assertEquals(new Position(1, 2), a.pos());
    }

    @Test
    public void testMoveRightOntoFloor() {
        TETile[][] world = smallWorld();
        Avatar a = new Avatar(new Position(2, 2), world);
        a.move('d', world);
        assertEquals(new Position(3, 2), a.pos());
    }

    @Test
    public void testBlockedByWall() {
        TETile[][] world = smallWorld();
        Avatar a = new Avatar(new Position(1, 2), world);
        a.move('a', world);   // x=0 is a wall
        assertEquals(new Position(1, 2), a.pos());
        assertEquals(Tileset.AVATAR, world[1][2]);     // stayed put
    }

    @Test
    public void testBlockedByOutOfBounds() {
        TETile[][] world = smallWorld();
        Avatar a = new Avatar(new Position(3, 2), world);
        a.move('d', world);   // x=4 is a wall, x=5 out of bounds
        assertEquals(new Position(3, 2), a.pos());
    }

    @Test
    public void testUnknownKeyDoesNothing() {
        TETile[][] world = smallWorld();
        Avatar a = new Avatar(new Position(2, 2), world);
        a.move('x', world);
        assertEquals(new Position(2, 2), a.pos());
        assertEquals(Tileset.AVATAR, world[2][2]);
    }

    @Test
    public void testMultipleMoves() {
        TETile[][] world = smallWorld();
        Avatar a = new Avatar(new Position(2, 2), world);
        a.move('d', world);   // (3,2)
        a.move('d', world);   // x=4 is a wall -> blocked
        a.move('w', world);   // (3,3)
        assertEquals(new Position(3, 3), a.pos());
    }
}
