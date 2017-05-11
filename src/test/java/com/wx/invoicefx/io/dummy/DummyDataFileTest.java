package com.wx.invoicefx.io.dummy;

import com.wx.invoicefx.io.interfaces.DataFile;
import com.wx.invoicefx.io.util.AbstractDataFileTest;
import com.wx.invoicefx.io.util.DummyStorage;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.16.
 */
public class DummyDataFileTest extends AbstractDataFileTest {

    private final DataFile dataFile = new DummyStorage.DummyDataFile();

    @Override
    protected DataFile getDataFile() {
        return dataFile;
    }
}

