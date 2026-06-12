package deque;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Comparator;

public class MaxArrayDequeTest {
    private static class numberComparator implements Comparator<Integer> {
        public int compare(Integer n1, Integer n2) {
           return n1 - n2;
        }
    }

    private static class absNumComparator implements Comparator<Integer> {
        public int compare(Integer n1, Integer n2) {
            return java.lang.Math.abs(n1) - java.lang.Math.abs(n2);
        }
    }

    private static class stringComparator implements Comparator<String> {
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }

    @Test
    public void typeTest() {
        MaxArrayDeque<Integer> ma1 = new MaxArrayDeque<>(new numberComparator());
        MaxArrayDeque<Integer> ma2 = new MaxArrayDeque<>(new absNumComparator());
        MaxArrayDeque<String> ma3 = new MaxArrayDeque<>(new stringComparator());

        ma1.addLast(1);
        ma1.addLast(2);
        ma1.addLast(3);
        ma1.addLast(-4);
        assertEquals(ma1.max().intValue(), 3);

        ma2.addLast(1);
        ma2.addLast(2);
        ma2.addLast(-3);
        ma2.addLast(-4);
        assertEquals(ma2.max(new numberComparator()).intValue(), 2);
        assertEquals(ma2.max().intValue(), -4);

        ma3.addLast("abc");
        ma3.addLast("def");
        ma3.addLast("mni");
        assertEquals(ma3.max(), "mni");
    }
}
