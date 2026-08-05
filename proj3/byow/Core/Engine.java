package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;

    /** The current world state (null until a world has been generated or loaded). */
    private TETile[][] world;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        InputSource source = new StringInputDevice(input);
        boolean parsingSeed = false;
        boolean sawLoadOrQuit = false;   // 'l' or ':' was seen (stop processing)
        StringBuilder seedDigits = new StringBuilder();

        while (source.possibleNextInput() && !sawLoadOrQuit) {
            char key = source.getNextKey();
            if (parsingSeed) {
                if (Character.isDigit(key)) {
                    seedDigits.append(key);
                } else {
                    long seed = seedDigits.length() == 0 ? 0L
                            : Long.parseLong(seedDigits.toString());
                    world = WorldGenerator.generateWorld(WIDTH, HEIGHT, seed);
                    seedDigits.setLength(0);
                    parsingSeed = false;
                    if (key == 'l' || key == 'L' || key == ':') {
                        sawLoadOrQuit = true;
                    }
                }
            } else if (key == 'n' || key == 'N') {
                parsingSeed = true;
            } else if (key == 'l' || key == 'L') {
                sawLoadOrQuit = true;   // load: not implemented; stop
            } else if (key == ':') {
                sawLoadOrQuit = true;   // save-and-quit: not implemented; stop
            }
            // movement keys (w/a/s/d) and any other characters are no-ops for now
        }

        if (parsingSeed && seedDigits.length() > 0) {
            // input ended while still reading a seed (e.g. "n123" with no trailing char)
            world = WorldGenerator.generateWorld(WIDTH, HEIGHT,
                    Long.parseLong(seedDigits.toString()));
        } else if (world == null && !sawLoadOrQuit) {
            // no n<seed> given and no load/quit: fall back to a default world (seed 0)
            world = WorldGenerator.generateWorld(WIDTH, HEIGHT, 0L);
        }
        return world;
    }

    /** Returns the current world as a string, or an empty string if none has been generated. */
    @Override
    public String toString() {
        if (world == null) {
            return "";
        }
        return TETile.toString(world);
    }
}
