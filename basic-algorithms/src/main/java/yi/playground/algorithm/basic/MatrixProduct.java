package yi.playground.algorithm.basic;

import org.apache.commons.lang3.Validate;

/**
 * See https://en.wikipedia.org/wiki/Matrix_multiplication for problem description
 */
public class MatrixProduct {

    /**
     * Multiplies 2 n by n matrices, where n is the power of 2.
     * @param input1
     * @param input2
     * @return
     */
    public int[][] multiply(int[][] input1, int[][] input2) {
        Validate.notEmpty(input1);
        Validate.notEmpty(input2);
        Validate.isTrue(input1.length == input2.length);
        int n = input1.length;
        Validate.isTrue((n > 0) && ((n & (n - 1)) == 0), "input size is not power of 2: " + n); // make sure n is power of 2
        for (int i = 0; i < n; i++) {
            Validate.isTrue(input1[i].length == n, "input1 is not a n by n matrix");
            Validate.isTrue(input2[i].length == n, "input2 is not a n by n matrix");
        }

        int[][] output = new int[n][n];


        return output;
    }
}
