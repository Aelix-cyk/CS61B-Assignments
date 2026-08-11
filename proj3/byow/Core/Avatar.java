package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;

/**
 * The player's avatar: a position that can move one tile per key press.
 * Only walkable (FLOOR) tiles may be entered; walls and out-of-bounds block.
 * The avatar owns its footprint on the world grid: it stamps the spawn cell
 * with {@link Tileset#AVATAR} and restores the cell it leaves to FLOOR.
 *
 * <p>The engine is responsible for normalizing key case before calling
 * {@link #move(char, TETile[][])} (input is case-insensitive).
 */
public class Avatar {

    private Position pos;

    /**
     * Creates the avatar at {@code spawn} and stamps that cell as the avatar.
     *
     * @param spawn the starting tile (must be walkable FLOOR)
     * @param world the world grid; the spawn cell is set to {@code AVATAR}
     */
    public Avatar(Position spawn, TETile[][] world) {
        this.pos = spawn;
        world[spawn.x][spawn.y] = Tileset.AVATAR;
    }

    /** The avatar's current position. */
    public Position pos() {
        return pos;
    }

    /**
     * Attempts to move one tile in the direction indicated by {@code key}
     * ({@code w/a/s/d}). Does nothing if the key is not a movement key or the
     * target tile is not walkable FLOOR. When the avatar moves, the cell it
     * leaves is restored to FLOOR and the destination is stamped {@code AVATAR}.
     *
     * @param key   lowercase movement key
     * @param world the world grid, used to check walkability and update tiles
     */
    public void move(char key, TETile[][] world) {
        int dx = 0;
        int dy = 0;
        switch (key) {
            case 'w': dy = 1; break;
            case 's': dy = -1; break;
            case 'a': dx = -1; break;
            case 'd': dx = 1; break;
            default: return; // not a movement key
        }
        Position target = new Position(pos.x + dx, pos.y + dy);
        // compare by description, not reference: after loading a saved world the
        // deserialized floor tiles are new TETile instances, not Tileset.FLOOR itself
        if (inBounds(world, target) && isFloor(world[target.x][target.y])) {
            world[pos.x][pos.y] = Tileset.FLOOR;   // restore where we were
            pos = target;
            world[pos.x][pos.y] = Tileset.AVATAR;  // stamp where we are
        }
    }

    private static boolean isFloor(TETile tile) {
        return tile != null && tile.description().equals(Tileset.FLOOR.description());
    }

    private static boolean inBounds(TETile[][] world, Position p) {
        return p.x >= 0 && p.x < world.length && p.y >= 0 && p.y < world[0].length;
    }
}
