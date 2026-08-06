package byow.lab13;

/** A source of characters, decoupled from where they come from (keyboard, string, ...). */
public interface CharSource {
    /** True if this source can still provide characters. */
    boolean hasNext();

    /** Returns the next character from this source. */
    char getNext();
}
