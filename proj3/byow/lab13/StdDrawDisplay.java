package byow.lab13;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;

/** A {@link Display} backed by Princeton's StdDraw library. */
public class StdDrawDisplay implements Display {

    @Override
    public void initialize(int width, int height) {
        StdDraw.setCanvasSize(width * 16, height * 16);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }

    @Override
    public void clear() {
        StdDraw.clear(Color.BLACK);
    }

    @Override
    public void setFont(int size) {
        StdDraw.setFont(new Font("Monaco", Font.BOLD, size));
    }

    @Override
    public void setPenColor(Color color) {
        StdDraw.setPenColor(color);
    }

    @Override
    public void text(double x, double y, String s) {
        StdDraw.text(x, y, s);
    }

    @Override
    public void textLeft(double x, double y, String s) {
        StdDraw.textLeft(x, y, s);
    }

    @Override
    public void textRight(double x, double y, String s) {
        StdDraw.textRight(x, y, s);
    }

    @Override
    public void show() {
        StdDraw.show();
    }

    @Override
    public void pause(int ms) {
        StdDraw.pause(ms);
    }
}
