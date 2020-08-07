package yi.playground.algorithm.basic.sorting;

import com.sun.tools.javac.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MergeSortTest extends AbstractSortTest {

    @BeforeEach
    void setUp() {
        instance = new MergeSort();
    }

    @Test
    void debug() {
        List<String> list = List.of("Hello", "World");

        list.forEach(element -> element.toLowerCase());
    }
}

