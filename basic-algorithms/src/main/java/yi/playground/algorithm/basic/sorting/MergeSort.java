package yi.playground.algorithm.basic.sorting;

import java.util.Arrays;

/**
 * O(n*log2(n))
 */
public class MergeSort extends AbstractSort {

    protected void doSort(int[] input) {

        if (input.length <= 1) { // already sorted
            return;
        }

        int middle = input.length / 2;

        int[] left = Arrays.copyOfRange(input, 0, middle);
        int[] right = Arrays.copyOfRange(input, middle, input.length);

        //System.out.println("doSort: left=" + left.length + ", right=" + right.length + ", total=" + String.valueOf(left.length + right.length));

        doSort(left);
        doSort(right);
        merge(input, left, right);
    }

    private void merge(int[] merged, int[] left, int[] right) {
        int mergedIndex = 0, leftIndex = 0, rightIndex = 0;
        for (; mergedIndex < merged.length && leftIndex < left.length && rightIndex < right.length; mergedIndex++) {

            if (left[leftIndex] < right[rightIndex]) {
                merged[mergedIndex] = left[leftIndex];
                leftIndex++;
            } else {
                merged[mergedIndex] = right[rightIndex];
                rightIndex++;
            }
        }

        for (; mergedIndex < merged.length && leftIndex < left.length; mergedIndex++, leftIndex++) {
            merged[mergedIndex] = left[leftIndex];
        }
        for (; mergedIndex < merged.length && rightIndex < right.length; mergedIndex++, rightIndex++) {
            merged[mergedIndex] = right[rightIndex];
        }

//        if (leftIndex < left.length) {
//            System.arraycopy(left, leftIndex, merged, mergedIndex, (left.length - leftIndex));
//        }
//        if (rightIndex < right.length) {
//            System.arraycopy(right, rightIndex, merged, mergedIndex, (right.length - rightIndex));
//        }
    }
}
