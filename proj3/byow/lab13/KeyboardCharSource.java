package byow.lab13;

import edu.princeton.cs.introcs.StdDraw;

/** A {@link CharSource} backed by the keyboard. Always "has next"; blocks until a key is pressed. */
public class KeyboardCharSource implements CharSource {

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public char getNext() {
        while (!StdDraw.hasNextKeyTyped()) {
            StdDraw.pause(20);
        }
        return StdDraw.nextKeyTyped();
    }
}
