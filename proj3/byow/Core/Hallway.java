package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;

import java.util.Random;

/**
 * An L-shaped corridor connecting two door positions. The exact routing (turn order)
 * and width are internal decisions, drawn from the provided {@link Random}.
 */
public class Hallway {

    /** Door position in the first room. */
    public final Position start;
    /** The L's turning point. */
    public final Position corner;
    /** Door position in the second room. */
    public final Position end;
    /** Corridor width in tiles (1 or 2). */
    public final int width;

    public Hallway(Position start, Position end, Random random) {
        this.start = start;
        this.end = end;
        this.width = 1 + random.nextInt(2);
        boolean horizontalFirst = random.nextBoolean();
        this.corner = horizontalFirst
                ? new Position(end.x, start.y)
                : new Position(start.x, end.y);
    }

    /**
     * Writes FLOOR strips of {@link #width} tiles along the two segments
     * start -> corner -> end. Bounds-checked; out-of-bounds tiles are skipped.
     */
    public void carve(TETile[][] world) {
        carveSegment(world, start, corner);
        carveSegment(world, corner, end);
    }

    private void carveSegment(TETile[][] world, Position from, Position to) {
        if (from.equals(to)) {
            return; // degenerate segment: nothing to carve
        }
        if (from.x == to.x) {
            int lo = Math.min(from.y, to.y);
            int hi = Math.max(from.y, to.y);
            for (int y = lo; y <= hi; y += 1) {
                for (int w = 0; w < width; w += 1) {
                    setFloor(world, from.x + w, y);
                }
            }
        } else {
            int lo = Math.min(from.x, to.x);
            int hi = Math.max(from.x, to.x);
            for (int x = lo; x <= hi; x += 1) {
                for (int w = 0; w < width; w += 1) {
                    setFloor(world, x, from.y + w);
                }
            }
        }
    }

    private void setFloor(TETile[][] world, int x, int y) {
        if (x >= 0 && x < world.length && y >= 0 && y < world[0].length) {
            world[x][y] = Tileset.FLOOR;
        }
    }
}
