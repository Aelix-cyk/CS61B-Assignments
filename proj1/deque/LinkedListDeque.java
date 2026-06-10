package deque;

import jh61b.junit.In;

import java.util.Iterator;

public class LinkedListDeque<T> {
    private class IntNode {
        public T item;
        public IntNode next;
        public IntNode prev;

        public IntNode(T i, IntNode p, IntNode n) {
            item = i;
            prev = p;
            next = n;
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

    public void addFirst(T item) {
        IntNode first = new IntNode(item, senitel, senitel.next);
        senitel.next = first;
        first.next.prev = first;
        size += 1;
    }

    public void addLast(T item) {
        IntNode last = new IntNode(item, senitel.prev, senitel);
        senitel.prev = last;
        last.prev.next = last;
        size += 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        IntNode e = senitel.next;
        for (int i = 0; i < size; i += 1) {
            System.out.print(e.item + " ");
            e = e.next;
        }
        System.out.println();
    }

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

    public Iterator<T> iterator() {
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LinkedListDeque)) {
            return false;
        }

        LinkedListDeque<T> other = (LinkedListDeque<T>) o;

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
