package byow.lab13;

import java.awt.Color;

/** A headless {@link Display} for tests: records the last drawn string, does nothing else. */
public class TestDisplay implements Display {
    private String lastShown = "";

    @Override
    public void initialize(int width, int height) {
    }

    @Override
    public void clear() {
    }

    @Override
    public void setFont(int size) {
    }

    @Override
    public void setPenColor(Color color) {
    }

    @Override
    public void text(double x, double y, String s) {
        lastShown = s;
    }

    @Override
    public void textLeft(double x, double y, String s) {
    }

    @Override
    public void textRight(double x, double y, String s) {
    }

    @Override
    public void show() {
    }

    @Override
    public void pause(int ms) {
    }

    /** The string most recently passed to {@link #text}. */
    public String lastShown() {
        return lastShown;
    }
}
