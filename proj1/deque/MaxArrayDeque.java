package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;
    
    public MaxArrayDeque(Comparator<T> c) {
        comparator = c;
    }

    public T max() {
        return max(comparator);
    }

    public T max(Comparator<T> c) {
        T maxItem = get(0);
        for (int i = 1; i < size(); i += 1) {
            T currentItem = get(i);
            if (c.compare(currentItem, maxItem) > 0) {
                maxItem = currentItem;
            }
        }
        return maxItem;
    }
}