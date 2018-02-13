package com.wx.invoicefx.model.save.table;

import com.wx.invoicefx.io.file.DirectoryStorage;
import com.wx.invoicefx.io.interfaces.RecordSerializer;
import com.wx.invoicefx.io.primary_key.IdClusteredIndex;
import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.save.table.column.Column;
import com.wx.invoicefx.model.save.table.column.ColumnInfo;

import java.io.File;

import static com.wx.invoicefx.model.save.table.ItemsTable.Cols.*;
import static com.wx.invoicefx.model.save.table.RecordsHelper.*;
import static com.wx.invoicefx.model.save.table.column.ColumnInfo.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 16.05.17.
 */
public class ItemsTable extends IdClusteredIndex {

    private static final RecordSerializer ITEM_SERIALIZER = getSerializer(Cols.values(), false);
    private static final int DEFAULT_PARTITION_SIZE = 1024;

    public static final String PARTITION_FILE_PREFIX = "items";

    public enum Cols implements Column {
        ID(ID_COLUMN),
        NAME(stringColumn(NOT_NULL)),
        PRICE(doubleColumn(NOT_NULL)),
        VAT(doubleColumn(NOT_NULL)),
        VAT_CATEGORY(byteColumn(NOT_NULL)),
        IS_ACTIVE(booleanColumn(NOT_NULL)),
        DEFAULT_DATE_ENABLED(byteColumn(NOT_NULL, enumRange(DateEnabled.class)));

        private final ColumnInfo column;

        Cols(ColumnInfo column) {
            this.column = column;
        }

        @Override
        public ColumnInfo getInfo() {
            return column;
        }
    }

    public static Object[] getItemRecord(Item item) {
        final Object[] record = new Object[Cols.values().length];

        set(record, ID, item.getId());
        set(record, NAME, item.getName());
        set(record, PRICE, item.getPrice());
        set(record, VAT, item.getVat().getValue());
        set(record, VAT_CATEGORY, (byte) item.getVat().getCategory());
        set(record, IS_ACTIVE, item.isActive());
        set(record, DEFAULT_DATE_ENABLED, item.getDefaultDateEnabled());

        return record;
    }

    public static Item getItemModel(Object[] record) {
        Item item = new Item();
        item.setId(getLong(record, ID));
        item.setName(getString(record, NAME));
        item.setPrice(getDouble(record, PRICE));
        item.setVat(new Vat(getDouble(record, VAT), (int) getByte(record, VAT_CATEGORY)));
        item.setActive(getBoolean(record, IS_ACTIVE));
        item.setDefaultDateEnabled(getDateEnabled(record, DEFAULT_DATE_ENABLED));

        return item;
    }

    public ItemsTable(File dataDirectory) {
        super(new DirectoryStorage(ITEM_SERIALIZER, dataDirectory, PARTITION_FILE_PREFIX), DEFAULT_PARTITION_SIZE, ID.ordinal());
    }
}
