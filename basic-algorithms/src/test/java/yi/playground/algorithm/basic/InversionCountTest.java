package yi.playground.algorithm.basic;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InversionCountTest {

    private InversionCount instance;

    @BeforeEach
    public void setUp() {
        instance = new InversionCount();
    }

    private static Stream<Arguments> intArrayProvider() {
        return Stream.of(
            Arguments.of(null, 0),
            Arguments.of(new int[]{}, 0),
            Arguments.of(new int[]{RandomUtils.nextInt()}, 0),
            Arguments.of(new int[]{1, 2}, 0),
            Arguments.of(new int[]{2, 1}, 1),

            Arguments.of(new int[]{1, 2, 3}, 0),
            Arguments.of(new int[]{1, 3, 2}, 1),
            Arguments.of(new int[]{2, 1, 3}, 1),
            Arguments.of(new int[]{2, 3, 1}, 2),
            Arguments.of(new int[]{3, 1, 2}, 2),
            Arguments.of(new int[]{3, 2, 1}, 3),

            Arguments.of(new int[]{4, 2, 3, 2}, 3),
            Arguments.of(new int[]{4, 2, 3, 1}, 5),

            Arguments.of(new int[]{6, 5, 4, 3, 3, 2, 1}, 20)

            //arguements(500000), // even
            //arguements(500001) // odd
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
    public void debug() {
        int[] input = {2, 4, 1, 3, 5};
        int actual = instance.countInversions(input);

        assertEquals(3, actual);

        // Make sure input was not changed
        assertArrayEquals(new int[]{2, 4, 1, 3, 5}, input);
    }

    @ParameterizedTest
    @MethodSource("intArrayProvider")
    public void sort(int[] input, int expected) {

        int[] originalInput = (input == null) ? null : Arrays.copyOf(input, input.length);
        int actual = instance.countInversions(input);

        // Make sure input was not changed
        assertArrayEquals(originalInput, input);
    }
}

