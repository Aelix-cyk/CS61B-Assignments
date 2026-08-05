package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A rectangular room: an interior of FLOOR tiles. Walls are not drawn here;
 * they are produced by the global wall pass after all carving is done.
 */
public class Room {

    /** Bottom-left corner of the interior FLOOR. */
    public final Position lowerLeft;
    /** Interior width in tiles. */
    public final int width;
    /** Interior height in tiles. */
    public final int height;

    public Room(Position lowerLeft, int width, int height) {
        this.lowerLeft = lowerLeft;
        this.width = width;
        this.height = height;
    }

    public int xStart() {
        return lowerLeft.x;
    }

    public int xEnd() {
        return lowerLeft.x + width - 1;
    }

    public int yStart() {
        return lowerLeft.y;
    }

    public int yEnd() {
        return lowerLeft.y + height - 1;
    }

    /** Fills the interior with FLOOR. Does not paint walls. */
    public void draw(TETile[][] world) {
        for (int x = xStart(); x <= xEnd(); x += 1) {
            for (int y = yStart(); y <= yEnd(); y += 1) {
                world[x][y] = Tileset.FLOOR;
            }
        }
    }

    /**
     * Returns true if this room's interior or 1-tile expansion intersects the other
     * room's interior or 1-tile expansion (rooms keep a 1-tile gap that becomes the
     * future wall strip).
     */
    public boolean overlaps(Room other) {
        boolean xOverlap = xEnd() + 1 >= other.xStart() - 1 && other.xEnd() + 1 >= xStart() - 1;
        boolean yOverlap = yEnd() + 1 >= other.yStart() - 1 && other.yEnd() + 1 >= yStart() - 1;
        return xOverlap && yOverlap;
    }

    /**
     * Returns a random Position on the interior border where a corridor can connect,
     * excluding the 4 interior corners (doors are never in a corner). Implementation:
     * collect the border-minus-corners tiles into a list and pick
     * {@code RandomUtils.uniform(random, list.size())}.
     *
     * <p>Precondition: {@code width >= 3} and {@code height >= 3}. For smaller rooms the
     * border-minus-corners set is empty and this throws. The generator guarantees this
     * by choosing random sizes in [3, max].
     */
    public Position randomDoorPosition(Random random) {
        List<Position> candidates = new ArrayList<>();
        for (int x = xStart() + 1; x <= xEnd() - 1; x += 1) {
            candidates.add(new Position(x, yStart()));
            candidates.add(new Position(x, yEnd()));
        }
        for (int y = yStart() + 1; y <= yEnd() - 1; y += 1) {
            candidates.add(new Position(xStart(), y));
            candidates.add(new Position(xEnd(), y));
        }
        return candidates.get(RandomUtils.uniform(random, candidates.size()));
    }
}
