package yi.playground.algorithm.basic.sorting;

import org.apache.commons.lang3.Validate;

import java.util.Arrays;

/**
 * O(n*log2(n))
 */
public class InPlaceMergeSort extends AbstractSort {

    protected void doSort(final int[] input) {
        if (input == null || input.length <= 1) { // already sorted
            return;
        }

        sortInPlace(input, 0, input.length);
    }

    private void sortInPlace(int[] input, int start, int end) {
        if (input == null || start >= (end - 1)) { // already sorted
            return;
        }

        Validate.isTrue(start >= 0 && end <= input.length,
            "Invalid start and end indexes: start=" + start + ", end=" + end, ", arrayLength=" + input.length);

        int middle = start + (end - start) / 2;
        sortInPlace(input, start, middle);
        sortInPlace(input, middle, end);

        merge(input, start, middle, end);
    }

    private void merge(int[] input, int start, int middle, int end) {

        int[] left = Arrays.copyOfRange(input, start, middle);
        int[] right = Arrays.copyOfRange(input, middle, end);
        //System.out.println("merge created left=" + left.length + ", right=" + right.length + ", total=" + String.valueOf(left.length + right.length));

        int leftIndex = 0, rightIndex = 0, i = start;
        final int leftLength = left.length;
        final int rightLength = right.length;
        for (; leftIndex < leftLength && rightIndex < rightLength && i < end; i++) {
            if (left[leftIndex] <= right[rightIndex]) {
                input[i] = left[leftIndex];
                leftIndex++;
            } else {
                input[i] = right[rightIndex];
                rightIndex++;
            }
        }

        for (; leftIndex < leftLength && i < end; leftIndex++, i++) {
            input[i] = left[leftIndex];
        }

        for (; rightIndex < rightLength && i < end; rightIndex++, i++) {
            input[i] = right[rightIndex];
        }
    }
}
