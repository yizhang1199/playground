package yi.playground.algorithm.basic;

import java.util.Arrays;

/**
 * Inversion Count for an array indicates – how far (or close) the array is from being sorted. If array is already sorted then inversion count is 0.
 * If array is sorted in reverse order that inversion count is the maximum.
 * <p>
 * Formally speaking, two elements a[i] and a[j] form an inversion if a[i] > a[j] and i < j
 * <p>
 * Example: The sequence 2, 4, 1, 3, 5 has three inversions (2, 1), (4, 1), (4, 3).
 */
public class InversionCounter {

    public int countInversions(int[] input) {
        int[] clone = (input == null) ? null : Arrays.copyOf(input, input.length);

        return sortAndCountInversions(clone);
    }

    private int sortAndCountInversions(int[] input) {
        if (input == null || input.length <= 1) { // no inversion count
            return 0;
        }

        int middle = input.length / 2;

        int[] left = Arrays.copyOfRange(input, 0, middle);
        int[] right = Arrays.copyOfRange(input, middle, input.length);

        int leftInversionCount = sortAndCountInversions(left);
        int rightInversionCount = sortAndCountInversions(right);
        int splitInversionCount = mergeAndCountSplitInversions(input, left, right);

        return leftInversionCount + rightInversionCount + splitInversionCount;
    }

    private int mergeAndCountSplitInversions(int[] input, int[] left, int[] right) {

        int splitInversionCount = 0;
        int i = 0, leftIndex = 0, rightIndex = 0;
        for (; i < input.length && leftIndex < left.length && rightIndex < right.length; i++) {
            if (left[leftIndex] <= right[rightIndex]) {
                input[i] = left[leftIndex];
                leftIndex++;
            } else {
                input[i] = right[rightIndex];
                rightIndex++;
                splitInversionCount += (left.length - leftIndex);
            }
        }

        for (; i < input.length && leftIndex < left.length; i++, leftIndex++) {
            input[i] = left[leftIndex];
        }

        for (; i < input.length && rightIndex < right.length; i++, rightIndex++) {
            input[i] = right[rightIndex];
        }

        return splitInversionCount;
    }
}
