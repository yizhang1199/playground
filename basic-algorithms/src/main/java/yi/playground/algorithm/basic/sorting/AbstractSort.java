package yi.playground.algorithm.basic.sorting;

import java.util.Arrays;

public abstract class AbstractSort implements Sort {

    public int[] sort(final int[] input) {
        if (input == null) {
            return input;
        }

        int[] clone = Arrays.copyOf(input, input.length);
        doSort(clone);

        return clone;
    }

    protected abstract void doSort(int[] input);
}
