package byow.lab13;

import java.awt.Color;

/**
 * The display surface used by {@link MemoryGame}, decoupled from the concrete
 * rendering backend so the game can run headless in tests.
 */
public interface Display {
    /** Sets up the canvas of the given size. */
    void initialize(int width, int height);

    /** Clears the canvas. */
    void clear();

    /** Sets the font size for subsequent text. */
    void setFont(int size);

    /** Sets the pen color for subsequent text. */
    void setPenColor(Color color);

    /** Draws centered text at (x, y). */
    void text(double x, double y, String s);

    /** Draws left-aligned text at (x, y). */
    void textLeft(double x, double y, String s);

    /** Draws right-aligned text at (x, y). */
    void textRight(double x, double y, String s);

    /** Shows the current frame on screen. */
    void show();

    /** Blocks for the given number of milliseconds. */
    void pause(int ms);
}
