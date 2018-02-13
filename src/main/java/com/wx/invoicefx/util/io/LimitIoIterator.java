package com.wx.invoicefx.util.io;

import com.wx.util.future.IoIterator;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 06.06.17.
 */
public class LimitIoIterator<E> implements IoIterator<E> {

    private final IoIterator<E> it;
    private int count;

    public LimitIoIterator(IoIterator<E> it, int limit) {
        this.it = it;
        this.count = limit;
    }

    @Override
    public E next() throws IOException {
        if (count <= 0) throw new NoSuchElementException("Limit reached");

        count--;
        return it.next();
    }

    @Override
    public boolean hasNext() {
        return count > 0 && it.hasNext();
    }

    @Override
    public void remove() throws IOException {
        if (count <= 0) throw new NoSuchElementException("Limit reached");

        it.remove();
    }
}
