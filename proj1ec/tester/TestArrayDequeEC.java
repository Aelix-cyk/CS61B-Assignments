package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import student.StudentArrayDeque;
import edu.princeton.cs.algs4.StdRandom;

public class TestArrayDequeEC {
    @Test
    public void randomizedTest() {
       int N = 10000;
       StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
       ArrayDequeSolution<Integer> refer = new ArrayDequeSolution<>();
       StringBuilder message = new StringBuilder();

       for (int i = 0; i < N; i += 1 ) {
           int op = StdRandom.uniform(0, 5);
           switch (op) {
               case 0:
                   sad1.addFirst(i);
                   refer.addFirst(i);
                   message.append("addFirst(" + i + ")\n");
                   assertEquals(message.toString(), sad1.size(), refer.size());
                   break;
               case 1:
                   sad1.addLast(i);
                   refer.addLast(i);
                   message.append("addLast(" + i + ")\n");
                   assertEquals(message.toString(), sad1.size(), refer.size());
                   break;
               case 2:
                   if (!sad1.isEmpty()) {
                       message.append("removeFirst()\n");
                       assertEquals(message.toString(), sad1.removeFirst(), refer.removeFirst());
                   }
                   break;
               case 3:
                   if (!sad1.isEmpty()) {
                       message.append("removeLast()\n");
                       assertEquals(message.toString(), sad1.removeLast(), refer.removeLast());
                   }
                   break;
               case 4:
                   if (!sad1.isEmpty()) {
                       int idx = i % sad1.size();
                       message.append("get(" + idx + ")\n");
                       assertEquals(message.toString(), sad1.get(idx), refer.get(idx));
                   }
                   break;
           }
       }
    }

    @Test
    public void erroredTest() {
        StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> refer = new ArrayDequeSolution<>();
        String failureSequence =
        "addFirst(0)\n"
        + "addFirst(1)\n"
        + "addLast(2)\n"
        + "addFirst(3)\n"
        + "addLast(4)\n"
        + "get(0)\n"
        + "addLast(6)\n"
        + "addFirst(7)\n"
        + "get(1)\n"
        + "removeFirst()\n"
        + "addLast(10)\n"
        + "addFirst(11)\n"
        + "removeFirst()\n"
        + "removeLast()\n"
        + "addFirst(14)\n"
        + "addFirst(15)\n"
        + "get(0)\n"
        + "removeFirst()\n"
        + "addFirst(18)\n"
        + "addFirst(19)\n"
        + "addFirst(20)\n"
        + "removeLast()\n";

        sad1.addFirst(0);
        sad1.addFirst(1);
        sad1.addLast(2);
        sad1.addFirst(3);
        sad1.addLast(4);
        sad1.get(0);
        sad1.addLast(6);
        sad1.addFirst(7);
        sad1.get(1);
        sad1.removeFirst();
        sad1.addLast(10);
        sad1.addFirst(11);
        sad1.removeFirst();
        sad1.removeLast();
        sad1.addFirst(14);
        sad1.addFirst(15);
        sad1.get(0);
        sad1.removeFirst();
        sad1.addFirst(18);
        sad1.addFirst(19);
        sad1.addFirst(20);
        //sad1.removeLast();

        refer.addFirst(0);
        refer.addFirst(1);
        refer.addLast(2);
        refer.addFirst(3);
        refer.addLast(4);
        refer.get(0);
        refer.addLast(6);
        refer.addFirst(7);
        refer.get(1);
        refer.removeFirst();
        refer.addLast(10);
        refer.addFirst(11);
        refer.removeFirst();
        refer.removeLast();
        refer.addFirst(14);
        refer.addFirst(15);
        refer.get(0);
        refer.removeFirst();
        refer.addFirst(18);
        refer.addFirst(19);
        refer.addFirst(20);

        assertEquals(failureSequence, sad1.removeLast(), refer.removeLast());
    }
}
