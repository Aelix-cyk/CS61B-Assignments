package byow.Core;

import byow.TileEngine.TETile;
import byow.lab12.Position;

import java.io.Serializable;

/** The persistent state of a game: the world grid and the avatar's position. */
public class SaveState implements Serializable {
    /** The world grid, including the avatar tile. */
    public final TETile[][] world;
    /** The avatar's position within the world. */
    public final Position avatarPos;

    public SaveState(TETile[][] world, Position avatarPos) {
        this.world = world;
        this.avatarPos = avatarPos;
    }
}
