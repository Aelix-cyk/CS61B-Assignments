package deque;

// import afu.org.checkerframework.checker.igj.qual.I;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {
    private T[] Items;
    private int size;
    private int first;
    private int last;

    public ArrayDeque() {
        Items = (T[]) new Object[8];
        size = 0;
        first = 0;
        last = 0;
    }

    /** Return the next index in clockwise (increment) */
    private int nextIndex(int index) {
        if (size == 0) {
            return index;
        } else {
            return (index + 1) % Items.length;
        }
    }

    /** Return the prev index in anti-clockwise (decrement) */
    private int prevIndex(int index) {
        if (size == 0) {
            return index;
        } else if (index == 0) {
            return Items.length - 1;
        } else {
            return index - 1;
        }
    }

    /** Resize the internal array */
    private void resize(int capicity) {
        T[] newArray = (T[]) new Object[capicity];
        if (last > first) {
            System.arraycopy(Items, first, newArray, 0, size);
        } else {
            System.arraycopy(Items, first, newArray, 0, Items.length - first);
            System.arraycopy(Items, 0, newArray, Items.length - first, last + 1);
        }
        Items = newArray;
        first = 0;
        last = size - 1;
    }

    private void expandSize() {
        double FACTOR = 1.2;
        if (size == Items.length) {
            resize((int) (size * FACTOR));
        }
    }

    private void reduceSize() {
        int QUOTIENT = 4;
        int MINSIZE = 16;
        if ((size < Items.length / QUOTIENT) && (size > MINSIZE)) {
            resize(Items.length / QUOTIENT);
        }
    }

    @Override
    public void addFirst(T item) {
        expandSize();
        first = prevIndex(first);
        Items[first] = item;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        expandSize();
        last = nextIndex(last);
        Items[last] = item;
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
                System.out.print(Items[i] + " ");
            }
        } else {
            for (int i = first; i < Items.length; i += 1) {
                System.out.print(Items[i] + " ");
            }
            for (int i = 0; i <= last; i += 1) {
                System.out.print(Items[i] + " ");
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
        firstItem = Items[first];
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
        lastItem = Items[last];
        size -= 1;
        last = prevIndex(last);
        return lastItem;
    }

    @Override
    public T get(int index) {
        if (size == 0) {
            return null;
        } else {
            return Items[(first + index) % Items.length];
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
