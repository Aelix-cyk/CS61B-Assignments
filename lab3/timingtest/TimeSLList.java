package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();

        int[] dataNum = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000};
        int maxOp = 10000;

        for (int n : dataNum) {
            Ns.addLast(n);
            opCounts.addLast(maxOp);
        }

        for (int i = 0; i < Ns.size(); i += 1) {
            SLList<Integer> testSLList = new SLList<>();
            for (int n = 0; n < Ns.get(i); n += 1) {
                testSLList.addLast(n);
            }
            Stopwatch sw = new Stopwatch();
            for (int j = 0; j < maxOp; j += 1) {
                int temp = testSLList.getLast();
            }
            double timeInSeconds = sw.elapsedTime();
            times.addLast(timeInSeconds);
        }

        printTimingTable(Ns, times, opCounts);
    }

}
