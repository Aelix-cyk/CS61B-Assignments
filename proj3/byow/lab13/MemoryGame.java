package byow.lab13;

import byow.Core.RandomUtils;

import java.awt.Color;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    /** Where characters come from (keyboard during play, a string in tests). */
    private CharSource charSource;
    /** The rendering backend (StdDraw during play, headless in tests). */
    private Display display;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    /** Creates a game that reads from the keyboard and renders to StdDraw. */
    public MemoryGame(int width, int height, long seed) {
        this(width, height, seed, new KeyboardCharSource(), new StdDrawDisplay());
    }

    /** Creates a game with injectable input and display (used by tests). */
    public MemoryGame(int width, int height, long seed, CharSource charSource, Display display) {
        this.width = width;
        this.height = height;
        this.charSource = charSource;
        this.display = display;
        display.initialize(width, height);
        this.rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < n; i += 1) {
            char c = CHARACTERS[RandomUtils.uniform(rand, CHARACTERS.length)];
            result.append(c);
        }
        return result.toString();
    }

    public void drawFrame(String s) {
        display.clear();

        if (!gameOver) {
            drawTopBar();
        }

        display.setFont(30);
        display.setPenColor(Color.WHITE);
        display.text((double) width / 2, (double) height / 2, s);

        display.show();
    }

    /** Draws the "Round / task / encouragement" bar at the top of the screen. */
    private void drawTopBar() {
        display.setFont(15);
        display.setPenColor(Color.WHITE);

        display.textLeft(1, height - 1, "Round: " + round);
        String task = playerTurn ? "Type!" : "Watch!";
        display.text((double) width / 2, height - 1, task);

        String encouragement = ENCOURAGEMENT[RandomUtils.uniform(rand, ENCOURAGEMENT.length)];
        display.textRight(width - 1, height - 1, encouragement);
    }

    public void flashSequence(String letters) {
        for (int i = 0; i < letters.length(); i += 1) {
            drawFrame(Character.toString(letters.charAt(i)));
            display.pause(1000);
            drawFrame("");
            display.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        StringBuilder input = new StringBuilder();
        while (input.length() < n) {
            if (charSource.hasNext()) {
                char c = charSource.getNext();
                input.append(c);
                drawFrame(input.toString());
            } else {
                // source exhausted (test strings run out); return what we have
                break;
            }
        }
        return input.toString();
    }

    public void startGame() {
        round = 1;
        gameOver = false;
        while (!gameOver) {
            drawFrame("Round: " + round);
            display.pause(1000);

            String target = generateRandomString(round);
            playerTurn = false;
            flashSequence(target);

            // flip the hint to "Type!" immediately, before the player starts typing
            playerTurn = true;
            drawFrame("");
            String input = solicitNCharsInput(round);

            if (input.equals(target)) {
                // keep the completed input visible briefly before the next round
                display.pause(500);
                round += 1;
            } else {
                gameOver = true;
                drawFrame("Game Over! You made it to round: " + round);
                return;
            }
        }
    }

    /** Returns true if the game has ended. */
    public boolean isGameOver() {
        return gameOver;
    }

    /** Returns the current round. */
    public int getRound() {
        return round;
    }

}
