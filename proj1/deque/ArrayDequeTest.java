package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class ArrayDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addSizeTest() {
        ArrayDeque<String> lld1 = new ArrayDeque<>();

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        // should be empty
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {
        ArrayDeque<String> lld1 = new ArrayDeque<>();
        ArrayDeque<Double> lld2 = new ArrayDeque<>();
        ArrayDeque<Boolean> lld3 = new ArrayDeque<>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {
        ArrayDeque<String> lld1 = new ArrayDeque<>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }
    }

    @Test
    public void equalsTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        ArrayDeque<Integer> lld2 = new ArrayDeque<>();

        lld1.addFirst(10);
        lld2.addFirst(10);

        assertTrue(lld1.equals(lld2));

        lld1.addLast(20);
        lld2.addLast(21);

        assertFalse(lld1.equals(lld2));
        assertTrue(lld1.equals(lld1));
    }

    @Test
    public void iteratorTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(1);
        lld1.addFirst(21);
        lld1.addFirst(321);
        lld1.addFirst(4321);
        lld1.addFirst(54321);
        Iterator<Integer> iter = lld1.iterator();

        int idx = 0;
        while (iter.hasNext()) {
            int item = iter.next();
            assertEquals((int) lld1.get(idx), item);
            System.out.println(item);
            idx += 1;
        }
    }

    @Test
    public void tenAddTenRemove() {
        final int N = 10;
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        int[] test = new int[N];

        for (int i = 0; i < N; i += 1) {
            test[i] = i;
        }

        for (int i = 0; i < N; i += 1) {
            lld1.addLast(i);
        }

        for (int i = 0; i < N; i += 1) {
            assertEquals(test[i], lld1.removeFirst().intValue());
        }
    }

    @Test
    public void randomizedTest() {
        int N = 100000;
        java.util.ArrayDeque<Integer> refer = new java.util.ArrayDeque<>();
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();

        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0,5);
            switch (operationNumber) {
                case 0:
                    lld1.addFirst(i);
                    refer.addFirst(i);
                    break;
                case 1:
                    lld1.addLast(i);
                    refer.addLast(i);
                    break;
                case 2:
                    assertEquals(lld1.size(), refer.size());
                    break;
                case 3:
                    if (lld1.size() > 0) {
                        assertEquals(lld1.removeFirst().intValue(), refer.removeFirst().intValue());
                    }
                    break;
                case 4:
                    if (lld1.size() > 0) {
                        assertEquals(lld1.removeLast().intValue(), refer.removeLast().intValue());
                    }
                    break;
                default:
                    assertEquals(lld1.isEmpty(), refer.isEmpty());
            }
        }

    }
}
