package com.wx.invoicefx.io.primary_key;

import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.io.interfaces.PartitionedStorage;
import com.wx.util.future.IoIterator;

import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.05.17.
 */
public class IdClusteredIndex extends ClusteredIndex {


    public IdClusteredIndex(PartitionedStorage storage, int maxPartitionSize, int idKey) {
        super(storage, maxPartitionSize, idKey);
    }

    public long assignUniqueIdAndInsert(Object[] record) throws IOException {
        int idKey = getSortKey();

        if (record[idKey] != null && (long) record[idKey] > 0) {
            throw new IllegalArgumentException("Record already has a primary key");
        }

        IoIterator<Object[]> it = iterator();
        long newId = it.hasNext() ? (long) it.next()[idKey] + 1 : 1L;
        record[idKey] = newId;

        insertWithIndexUnique(record);

        return newId;
    }

    @Override
    public void insertWithIndex(Object[] row) throws IOException {
        throw new IllegalArgumentException("Must be inserted with 'unique'");
    }


}
