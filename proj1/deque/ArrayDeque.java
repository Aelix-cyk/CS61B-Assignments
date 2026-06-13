package deque;

// import afu.org.checkerframework.checker.igj.qual.I;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int size;
    private int first;
    private int last;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        first = 0;
        last = 0;
    }

    /** Return the next index in clockwise (increment) */
    private int nextIndex(int index) {
        if (size == 0) {
            return index;
        } else {
            return (index + 1) % items.length;
        }
    }

    /** Return the prev index in anti-clockwise (decrement) */
    private int prevIndex(int index) {
        if (size == 0) {
            return index;
        } else if (index == 0) {
            return items.length - 1;
        } else {
            return index - 1;
        }
    }

    /** Resize the internal array */
    private void resize(int capacity) {
        T[] newArray = (T[]) new Object[capacity];
        if (last > first) {
            System.arraycopy(items, first, newArray, 0, size);
        } else {
            System.arraycopy(items, first, newArray, 0, items.length - first);
            System.arraycopy(items, 0, newArray, items.length - first, last + 1);
        }
        items = newArray;
        first = 0;
        last = size - 1;
    }

    private void expandSize() {
        final double factor = 1.2;
        if (size == items.length) {
            resize((int) (size * factor));
        }
    }

    private void reduceSize() {
        final int quotient = 4;
        final int minSize = 16;
        if ((size < items.length / quotient) && (size > minSize)) {
            resize(items.length / quotient);
        }
    }

    @Override
    public void addFirst(T item) {
        expandSize();
        first = prevIndex(first);
        items[first] = item;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        expandSize();
        last = nextIndex(last);
        items[last] = item;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        if (last > first) {
            for (int i = first; i <= last; i += 1) {
                System.out.print(items[i] + " ");
            }
        } else {
            for (int i = first; i < items.length; i += 1) {
                System.out.print(items[i] + " ");
            }
            for (int i = 0; i <= last; i += 1) {
                System.out.print(items[i] + " ");
            }
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        T firstItem;
        if (size == 0) {
            return null;
        }
        reduceSize();
        firstItem = items[first];
        size -= 1;
        first = nextIndex(first);
        return firstItem;
    }

    @Override
    public T removeLast() {
        T lastItem;
        if (size == 0) {
            return null;
        }
        reduceSize();
        lastItem = items[last];
        size -= 1;
        last = prevIndex(last);
        return lastItem;
    }

    @Override
    public T get(int index) {
        if (size == 0) {
            return null;
        } else {
            return items[(first + index) % items.length];
        }
    }

    public Iterator<T> iterator() {
        return new ArrayListDequeIterator();
    }

    private class ArrayListDequeIterator implements Iterator<T> {
        private int winPos;

        public ArrayListDequeIterator() {
            winPos = 0;
        }

        @Override
        public boolean hasNext() {
            return winPos < size;
        }

        @Override
        public T next() {
            T returnItem = get(winPos);
            winPos += 1;
            return returnItem;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ArrayDeque)) {
            return false;
        }

        ArrayDeque<T> other = (ArrayDeque<T>) o;

        if (this.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < size; i += 1) {
            if (this.get(i) != other.get(i)) {
                return false;
            }
        }
        return true;
    }
}
