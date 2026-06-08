package randomizedtest;

import edu.princeton.cs.algs4.BellmanFordSP;
import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        BuggyAList<Integer> buggyAList = new BuggyAList<>();
        AListNoResizing<Integer> rightAList = new AListNoResizing<>();
        int[] testArray = {4, 5, 6};

        for (int x : testArray) {
            buggyAList.addLast(x);
            rightAList.addLast(x);
            assertEquals(buggyAList.size(), rightAList.size());
        }

        for (int i = 0; i < testArray.length; i += 1) {
            int a = buggyAList.removeLast();
            int b = rightAList.removeLast();
            assertEquals(a, b);
        }

    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> bL = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                bL.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int bsize = bL.size();
                assertEquals(size, bsize);
            } else if (operationNumber == 2) {
                if (L.size() > 0) {
                    int LLast = L.getLast();
                    int bLLast = bL.getLast();
                    assertEquals(LLast, bLLast);
                }
            } else if (operationNumber == 3) {
                if (L.size() > 0) {
                    int LLast = L.removeLast();
                    int bLLast = bL.removeLast();
                    assertEquals(LLast, bLLast);
                }
            }
        }
    }
}
