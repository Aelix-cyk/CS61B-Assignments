package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates a world of rooms connected by L-shaped hallways, with walls applied
 * by a global wall pass. The same seed always produces the same world.
 */
public class WorldGenerator {

    private static final int MIN_ROOM_DIM = 3;   // randomDoorPosition precondition
    private static final int MAX_ROOM_DIM = 8;
    private static final int MAX_PLACEMENT_ATTEMPTS = 50;
    private static final int MIN_ROOMS = 10;
    private static final int MAX_ROOMS_EXCLUSIVE = 24;
    private static final int EDGE_MARGIN = 1;    // rooms stay this far from the world edge

    /**
     * Generates a pseudorandom world of the given size.
     *
     * @param width  world width in tiles
     * @param height world height in tiles
     * @param seed   random seed; the same seed reproduces the same world
     */
    public static TETile[][] generateWorld(int width, int height, long seed) {
        Random random = new Random(seed);
        TETile[][] world = new TETile[width][height];
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        List<Room> rooms = new ArrayList<>();
        int roomCount = RandomUtils.uniform(random, MIN_ROOMS, MAX_ROOMS_EXCLUSIVE);
        for (int i = 0; i < roomCount; i += 1) {
            Room room = tryPlaceRoom(random, world, rooms);
            if (room != null) {
                rooms.add(room);
                room.draw(world);
            }
        }

        connectRooms(random, world, rooms);

        addWalls(world);
        return world;
    }

    /** Attempts to place a room that overlaps no existing room. Returns null if attempts run out. */
    private static Room tryPlaceRoom(Random random, TETile[][] world, List<Room> rooms) {
        for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt += 1) {
            int w = RandomUtils.uniform(random, MIN_ROOM_DIM, MAX_ROOM_DIM + 1);
            int h = RandomUtils.uniform(random, MIN_ROOM_DIM, MAX_ROOM_DIM + 1);
            int maxX = world.length - EDGE_MARGIN - w - 1;
            int maxY = world[0].length - EDGE_MARGIN - h - 1;
            if (maxX < EDGE_MARGIN || maxY < EDGE_MARGIN) {
                return null; // world too small for this room size
            }
            int x = RandomUtils.uniform(random, EDGE_MARGIN, maxX + 1);
            int y = RandomUtils.uniform(random, EDGE_MARGIN, maxY + 1);
            Room candidate = new Room(new Position(x, y), w, h);
            boolean overlaps = false;
            for (Room existing : rooms) {
                if (candidate.overlaps(existing)) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                return candidate;
            }
        }
        return null;
    }

    /** Connects each room to the previous one (insertion order) with an L-shaped hallway. */
    private static void connectRooms(Random random, TETile[][] world, List<Room> rooms) {
        for (int k = 1; k < rooms.size(); k += 1) {
            Position doorK = rooms.get(k).randomDoorPosition(random);
            Position doorPrev = rooms.get(k - 1).randomDoorPosition(random);
            new Hallway(doorK, doorPrev, random).carve(world);
        }
    }

    /** Global wall pass: every NOTHING tile adjacent (4-neighborhood) to a FLOOR becomes WALL. */
    private static void addWalls(TETile[][] world) {
        int width = world.length;
        int height = world[0].length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                if (world[x][y] != Tileset.FLOOR) {
                    continue;
                }
                wallify(world, x + 1, y);
                wallify(world, x - 1, y);
                wallify(world, x, y + 1);
                wallify(world, x, y - 1);
            }
        }
    }

    private static void wallify(TETile[][] world, int x, int y) {
        if (x >= 0 && x < world.length && y >= 0 && y < world[0].length
                && world[x][y] == Tileset.NOTHING) {
            world[x][y] = Tileset.WALL;
        }
    }
}
