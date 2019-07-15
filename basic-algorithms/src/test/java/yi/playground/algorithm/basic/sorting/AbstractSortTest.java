package yi.playground.algorithm.basic.sorting;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public abstract class AbstractSortTest {

    protected Sort instance;

    protected static Stream<Arguments> intArrayProvider() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(new int[]{}, new int[]{}),
            Arguments.of(new int[]{1}, new int[]{1}),
            Arguments.of(new int[]{1, 2}, new int[]{1, 2}),
            Arguments.of(new int[]{2, 1}, new int[]{1, 2}),

            Arguments.of(new int[]{1, 2, 3}, new int[]{1, 2, 3}),
            Arguments.of(new int[]{1, 3, 2}, new int[]{1, 2, 3}),
            Arguments.of(new int[]{2, 1, 3}, new int[]{1, 2, 3}),
            Arguments.of(new int[]{2, 3, 1}, new int[]{1, 2, 3}),
            Arguments.of(new int[]{3, 1, 2}, new int[]{1, 2, 3}),
            Arguments.of(new int[]{3, 2, 1}, new int[]{1, 2, 3}),

            Arguments.of(new int[]{4, 2, 3, 2}, new int[]{2, 2, 3, 4}),
            Arguments.of(new int[]{4, 2, 3, 1}, new int[]{1, 2, 3, 4}),

            arguements(500000), // even
            arguements(500001) // odd
        );
    }

    private static Arguments arguements(int inputCount) {

        int[] input = createIntput(inputCount);
        int[] expected = ArrayUtils.clone(input);
        Arrays.sort(expected);

        return Arguments.of(input, expected);
    }

    private static int[] createIntput(int length) {
        Validate.isTrue(length >= 0);
        int[] values = new int[length];

        for (int i = 0; i < values.length; i++) {
            values[i] = RandomUtils.nextInt();
        }

        return values;
    }

    @Test
    void debug() {
        int[] input = {3, 2, 1, 4, 5, 6, 7, 8, 9, 5};
        int[] actual = instance.sort(input);
        int[] expected = {1, 2, 3, 4, 5, 5, 6, 7, 8, 9};
        System.out.println(Arrays.toString(actual));
        assertArrayEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("intArrayProvider")
    void sort(int[] input, int[] expected) {
        int[] actual = instance.sort(input);
        if (input != null) {
            assertNotSame(input, actual);
        }
        assertArrayEquals(expected, actual);
    }
}

