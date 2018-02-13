package com.wx.invoicefx.sync;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public interface AbstractFileSystem extends AutoCloseable {

    void clear() throws IOException;

    InputStream read(String filename) throws IOException;

    void write(String filename, InputStream input) throws IOException;

    void remove(String filename) throws IOException;

    @Override
    void close();
}
