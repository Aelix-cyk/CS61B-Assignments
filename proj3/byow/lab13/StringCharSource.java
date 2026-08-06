package byow.lab13;

/** A {@link CharSource} backed by a fixed string, for tests. */
public class StringCharSource implements CharSource {
    private final String input;
    private int index;

    public StringCharSource(String input) {
        this.input = input;
    }

    @Override
    public boolean hasNext() {
        return index < input.length();
    }

    @Override
    public char getNext() {
        return input.charAt(index++);
    }
}
