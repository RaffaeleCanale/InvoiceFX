package com.wx.invoicefx.util;

import com.wx.invoicefx.util.io.LimitIoIterator;
import com.wx.util.future.IoIterator;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 06.06.17.
 */
public class LimitIoIteratorTest {

    private static IoIterator<Integer> getIterator(Integer... values) {
        Iterator<Integer> it = Arrays.asList(values).iterator();

        return new IoIterator<Integer>() {
            @Override
            public Integer next() throws IOException {
                return it.next();
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
        };
    }

    @Test
    public void limitTest() throws IOException {
        IoIterator<Integer> it = getIterator(1, 2, 3, 4, 5, 6, 7, 8, 9);

        LimitIoIterator<Integer> limitIt = new LimitIoIterator<>(it, 3);

        assertEquals(Arrays.asList(1,2,3), limitIt.collect());
    }

}