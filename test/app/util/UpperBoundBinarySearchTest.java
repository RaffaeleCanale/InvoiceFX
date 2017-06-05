package app.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static app.util.UpperBoundBinarySearch.search;
import static java.util.Comparator.naturalOrder;
import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class UpperBoundBinarySearchTest {

    @Test
    public void existsTest() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        int index = search(list, 3, naturalOrder());
        assertEquals(3, index);
    }

    @Test
    public void existTest2() {
        List<Integer> list = Arrays.asList(0, 0, 1, 1, 2, 2, 3, 3, 3, 4, 5, 6, 7, 8, 9);

        int index = search(list, 3, naturalOrder());
        assertEquals(8, index);
    }

    @Test
    public void existTest3() {
        List<Integer> list = Arrays.asList(0, 0, 1, 1, 2, 2, 3, 3, 3);

        int index = search(list, 3, naturalOrder());
        assertEquals(8, index);
    }

    @Test
    public void existTest4() {
        List<Integer> list = Arrays.asList(0, 0, 1, 1, 2, 2, 3, 3, 3);

        int index = search(list, 0, naturalOrder());
        assertEquals(1, index);
    }

    @Test
    public void insertTest1() {
        List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);

        int index = search(list, 0, naturalOrder());
        assertEquals(-1, index);
    }

    @Test
    public void insertTest2() {
        List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);

        int index = search(list, 10, naturalOrder());
        assertEquals(-6, index);
    }

    @Test
    public void insertTest3() {
        List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);

        int index = search(list, 6, naturalOrder());
        assertEquals(-4, index);
    }
}