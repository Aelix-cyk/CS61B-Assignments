package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.KeyboardInputSource;
import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    /** Height of the HUD strip at the top of the window (in tiles). */
    public static final int HUD_HEIGHT = 2;

    /** The current world state (null until a world has been generated or loaded). */
    private TETile[][] world;
    /** The player avatar (null until a world exists). */
    private Avatar avatar;

    /** Directory and file where the game state is persisted. */
    private static final String SAVE_DIR = ".byow";
    private static final String SAVE_FILE = "save.dat";
    /** Overridable for tests that need to isolate the save file. */
    String savePath = SAVE_DIR + "/" + SAVE_FILE;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        ter.initialize(WIDTH, HEIGHT + HUD_HEIGHT);
        drawMenu();

        char choice = readMenuChoice();
        if (choice == 'N') {
            long seed = readSeedFromKeyboard();
            world = WorldGenerator.generateWorld(WIDTH, HEIGHT, seed);
            spawnAvatar();
            ter.renderFrame(world);
        }
        // 'L' (load) not implemented yet: falls through to a default world

        runGameLoop(new KeyboardInputSource(), true);
    }

    /** Draws the main menu screen using StdDraw directly. */
    private void drawMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 20));
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "CS61B: THE GAME");
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0 - 2, "New Game (N)   Load (L)");
        StdDraw.show();
    }

    /**
     * Draws the HUD strip at the top of the window: the avatar's position on the
     * left, and the description of the tile under the mouse cursor on the right.
     * The world grid occupies the bottom {@link #HEIGHT} rows of the canvas.
     */
    private void drawHUD() {
        double hudY = HEIGHT + HUD_HEIGHT / 2.0;

        // clear only the HUD strip (black rectangle over rows [HEIGHT, HEIGHT+HUD))
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(WIDTH / 2.0, hudY, WIDTH / 2.0, HUD_HEIGHT / 2.0);

        // draw the HUD content
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 14));
        StdDraw.setPenColor(Color.WHITE);

        // avatar position
        String pos = avatar == null ? "-" : "(" + avatar.pos().x + ", " + avatar.pos().y + ")";
        StdDraw.textLeft(1, hudY, "Position: " + pos);

        // hovered tile description
        double mx = StdDraw.mouseX();
        double my = StdDraw.mouseY();
        if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {
            int tx = (int) mx;
            int ty = (int) my;
            TETile tile = world[tx][ty];
            StdDraw.textRight(WIDTH - 1, hudY, tile.description());
        }
        StdDraw.show();
    }

    /** Blocks until the player presses 'n' (new game) or 'l' (load). Returns uppercase. */
    private char readMenuChoice() {
        KeyboardInputSource source = new KeyboardInputSource();
        while (true) {
            char key = Character.toUpperCase(source.getNextKey());
            if (key == 'N' || key == 'L') {
                return key;
            }
        }
    }

    /** Reads decimal digits from the keyboard following a menu 'N' choice. */
    private long readSeedFromKeyboard() {
        KeyboardInputSource source = new KeyboardInputSource();
        StringBuilder digits = new StringBuilder();
        while (source.possibleNextInput()) {
            char c = source.getNextKey();
            if (Character.isDigit(c)) {
                digits.append(c);
            } else {
                break; // first non-digit ends the seed
            }
        }
        return digits.length() == 0 ? 0L : Long.parseLong(digits.toString());
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
        runGameLoop(new StringInputDevice(input), false);
        return world;
    }

    /**
     * The shared input-agnostic game loop: parses seed commands and movement keys
     * from {@code source}. When {@code render} is true, redraws the world after each
     * key (keyboard mode) and refreshes the HUD on mouse movement; otherwise the
     * world is updated silently (string mode).
     */
    private void runGameLoop(InputSource source, boolean render) {
        if (render && source instanceof KeyboardInputSource) {
            runKeyboardLoop((KeyboardInputSource) source);
        } else {
            runBlockingLoop(source, render);
        }
    }

    /** Keyboard mode: polls keys and refreshes the HUD on mouse movement. */
    private void runKeyboardLoop(KeyboardInputSource source) {
        boolean parsingSeed = false;
        boolean sawLoad = false;
        boolean sawQuit = false;
        StringBuilder seedDigits = new StringBuilder();
        double lastMouseX = StdDraw.mouseX();
        double lastMouseY = StdDraw.mouseY();

        while (!sawQuit) {
            if (source.hasNextKey()) {
                char key = source.getNextKey();
                // process the key (same handling as string mode)
                ProcessResult result = processKey(key, parsingSeed, sawLoad, sawQuit, seedDigits);
                parsingSeed = result.parsingSeed;
                sawLoad = result.sawLoad;
                sawQuit = result.sawQuit;
                if (result.changed) {
                    ter.renderFrame(world);
                    drawHUD();
                    lastMouseX = StdDraw.mouseX();
                    lastMouseY = StdDraw.mouseY();
                }
            } else if (mouseMoved(lastMouseX, lastMouseY)) {
                lastMouseX = StdDraw.mouseX();
                lastMouseY = StdDraw.mouseY();
                drawHUD();   // just refresh the HUD (mouse-hover description)
            }
        }
        finishGameLoop(parsingSeed, sawLoad, sawQuit, seedDigits, true);
    }

    /** String mode: blocks on each key from the source. */
    private void runBlockingLoop(InputSource source, boolean render) {
        boolean parsingSeed = false;
        boolean sawLoad = false;
        boolean sawQuit = false;
        StringBuilder seedDigits = new StringBuilder();

        while (source.possibleNextInput() && !sawQuit) {
            char key = source.getNextKey();
            ProcessResult result = processKey(key, parsingSeed, sawLoad, sawQuit, seedDigits);
            parsingSeed = result.parsingSeed;
            sawLoad = result.sawLoad;
            sawQuit = result.sawQuit;

            if (render && world != null) {
                ter.renderFrame(world);
                drawHUD();
            }
        }
        finishGameLoop(parsingSeed, sawLoad, sawQuit, seedDigits, render);
    }

    private static boolean mouseMoved(double lastX, double lastY) {
        return StdDraw.mouseX() != lastX || StdDraw.mouseY() != lastY;
    }

    /** Result of processing one key: updated parser state + whether the world changed. */
    private static class ProcessResult {
        boolean parsingSeed;
        boolean sawLoad;
        boolean sawQuit;
        boolean changed;

        ProcessResult(boolean parsingSeed, boolean sawLoad, boolean sawQuit, boolean changed) {
            this.parsingSeed = parsingSeed;
            this.sawLoad = sawLoad;
            this.sawQuit = sawQuit;
            this.changed = changed;
        }
    }

    /**
     * Processes one key from the input; mutates {@code world} / {@code avatar}.
     * A non-digit key arriving while reading a seed commits the seed and is then
     * re-processed as a normal command (it is NOT swallowed) — so "n123sss" means
     * seed 123 followed by three 's' movements, per the spec.
     */
    private ProcessResult processKey(char key, boolean parsingSeed, boolean sawLoad,
                                     boolean sawQuit, StringBuilder seedDigits) {
        boolean changed = false;
        if (parsingSeed) {
            if (Character.isDigit(key)) {
                seedDigits.append(key);
                return new ProcessResult(true, sawLoad, sawQuit, false);
            }
            // non-digit: commit the pending seed, then fall through to process key as a command
            long seed = seedDigits.length() == 0 ? 0L
                    : Long.parseLong(seedDigits.toString());
            world = WorldGenerator.generateWorld(WIDTH, HEIGHT, seed);
            spawnAvatar();
            changed = true;
            seedDigits.setLength(0);
            parsingSeed = false;
            // do NOT return; process this key as a normal command below
        }

        if (key == 'n' || key == 'N') {
            parsingSeed = true;
        } else if (key == 'l' || key == 'L') {
            loadState();
            sawLoad = true;
            changed = true;
        } else if (key == ':') {
            saveState();
            sawQuit = true;
            changed = true;
        } else if (world != null && isMovementKey(key)) {
            Position before = avatar.pos();
            avatar.move(Character.toLowerCase(key), world);
            changed = !avatar.pos().equals(before);
        }
        return new ProcessResult(parsingSeed, sawLoad, sawQuit, changed);
    }

    /** Handles the end-of-input world fallback (default seed / pending seed). */
    private void finishGameLoop(boolean parsingSeed, boolean sawLoad, boolean sawQuit,
                                StringBuilder seedDigits, boolean render) {
        if (parsingSeed && seedDigits.length() > 0) {
            world = WorldGenerator.generateWorld(WIDTH, HEIGHT,
                    Long.parseLong(seedDigits.toString()));
            spawnAvatar();
        } else if (world == null && !sawLoad && !sawQuit) {
            world = WorldGenerator.generateWorld(WIDTH, HEIGHT, 0L);
            spawnAvatar();
        }
        if (render && world != null) {
            ter.renderFrame(world);
            drawHUD();
        }
    }

    private static boolean isMovementKey(char key) {
        char k = Character.toLowerCase(key);
        return k == 'w' || k == 'a' || k == 's' || k == 'd';
    }

    /** Places the avatar on the first walkable FLOOR tile found (deterministic scan). */
    private void spawnAvatar() {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (world[x][y] == Tileset.FLOOR) {
                    avatar = new Avatar(new Position(x, y), world);
                    return;
                }
            }
        }
        // no floor found (shouldn't happen for a valid world)
        avatar = null;
    }

    /** Returns the current world as a string, or an empty string if none has been generated. */
    @Override
    public String toString() {
        if (world == null) {
            return "";
        }
        return TETile.toString(world);
    }

    /** Saves the current world + avatar position to {@code .byow/save.dat}. */
    private void saveState() {
        if (world == null || avatar == null) {
            return;
        }
        File file = new File(savePath);
        File dir = file.getParentFile();
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            return;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new SaveState(world, avatar.pos()));
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    /** Restores a previously saved game from {@code .byow/save.dat}, if present. */
    private void loadState() {
        File file = new File(savePath);
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            SaveState state = (SaveState) in.readObject();
            world = state.world;
            avatar = new Avatar(state.avatarPos, world);   // re-stamp the avatar tile
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game: " + e.getMessage());
        }
    }
}
