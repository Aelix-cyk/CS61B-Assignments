package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class IntNode {
        T item;
        IntNode next;
        IntNode prev;

        IntNode(T i, IntNode p, IntNode n) {
            item = i;
            prev = p;
            next = n;
        }

        public T getItem(int idx) {
            if (idx == 0) {
                return item;
            } else if (next != null) {
                return next.getItem(idx - 1);
            } else {
                return null;
            }
        }
    }

    private IntNode senitel;
    private int size;

    public LinkedListDeque() {
        senitel = new IntNode(null, null, null);
        senitel.next = senitel;
        senitel.prev = senitel;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        IntNode first = new IntNode(item, senitel, senitel.next);
        senitel.next = first;
        first.next.prev = first;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        IntNode last = new IntNode(item, senitel.prev, senitel);
        senitel.prev = last;
        last.prev.next = last;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        IntNode e = senitel.next;
        for (int i = 0; i < size; i += 1) {
            System.out.print(e.item + " ");
            e = e.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        } else {
            T item = senitel.next.item;
            senitel.next = senitel.next.next;
            senitel.next.prev = senitel;
            size -= 1;
            return item;
        }
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        } else {
            T item = senitel.prev.item;
            senitel.prev = senitel.prev.prev;
            senitel.prev.next = senitel;
            size -= 1;
            return item;
        }
    }

    @Override
    public T get(int index) {
        int currentIdx = 0;
        IntNode e = senitel.next;
        if (index >= size) {
            return null;
        } else {
            while (currentIdx < index) {
                e = e.next;
                currentIdx += 1;
            }
            return e.item;
        }
    }

    public T getRecursive(int index) {
        return senitel.next.getItem(index);
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private int winPos;

        LinkedListDequeIterator() {
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

        if (!(o instanceof Deque)) {
            return false;
        }

        Deque<T> other = (Deque<T>) o;

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
