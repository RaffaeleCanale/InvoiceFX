package com.wx.invoicefx.io.dummy;

import com.wx.invoicefx.io.interfaces.PartitionedStorage;
import com.wx.invoicefx.io.interfaces.RecordSerializer;
import com.wx.invoicefx.io.util.AbstractPartitionedStorageTest;
import com.wx.invoicefx.io.util.DummyStorage;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.16.
 */
public class DummyPartitionedStorageTest extends AbstractPartitionedStorageTest {

    private final PartitionedStorage dummyPartitionedStorage = new DummyStorage.DummyPartitionedStorage();

    @Override
    protected PartitionedStorage getPartitionedStorage(RecordSerializer serializer) {
        return dummyPartitionedStorage;
    }
}
