package com.wx.invoicefx.io.primary_key;

import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.io.interfaces.PartitionedStorage;
import com.wx.properties.property.Property;

import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.05.17.
 */
public class ClusteredIndexWithPK extends ClusteredIndex {

    private final Property<Long> maxIdProperty;
    private final int idKey;

    public ClusteredIndexWithPK(PartitionedStorage storage, int maxPartitionSize, int sortKey, int idKey, Property<Long> maxIdProperty) {
        super(storage, maxPartitionSize, sortKey);

        this.maxIdProperty = maxIdProperty;
        this.idKey = idKey;

        if (!maxIdProperty.exists()) {
            maxIdProperty.set(0L);
        }
    }

    public long assignUniqueIdAndInsert(Object[] record) throws IOException {
        if ((long) record[idKey] > 0) {
            throw new IllegalArgumentException("Record already has a primary key");
        }

        long newId = this.maxIdProperty.get() + 1L;
        record[idKey] = newId;

        insertWithIndex(record);

        return newId;
    }

    @Override
    public void insertWithIndexUnique(Object[] row) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertWithIndex(Object[] row) throws IOException {
        long id = (long) row[idKey];
//        System.out.println(id + " .. " + maxIdProperty.get());

        if (id > maxIdProperty.get()) {

            maxIdProperty.set(id);

            super.insertWithIndex(row);
        } else {
            // Check that ID is unique
            boolean idFound = queryFirst((r) -> (long) r[idKey] == id).isPresent();

            if (idFound) throw new IllegalArgumentException("Duplicate ID found");


            super.insertWithIndex(row);
        }
    }

//    @Override
//    protected void onRemoved(Object[] removed) {
//        super.onRemoved(removed);
//    }
}
