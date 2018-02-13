package com.wx.invoicefx.model.save.table;

import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.io.file.DirectoryStorage;
import com.wx.invoicefx.io.interfaces.RecordSerializer;
import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.model.save.table.column.Column;
import com.wx.invoicefx.model.save.table.column.ColumnInfo;
import com.wx.util.pair.Pair;

import java.io.File;

import static com.wx.invoicefx.model.save.table.PurchasesTable.Cols.*;
import static com.wx.invoicefx.model.save.table.RecordsHelper.*;
import static com.wx.invoicefx.model.save.table.column.ColumnInfo.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 16.05.17.
 */
public class PurchasesTable extends ClusteredIndex {

    public static final String PARTITION_FILE_PREFIX = "purchases";

    private static final RecordSerializer PURCHASE_SERIALIZER = getSerializer(Cols.values(), false);
    private static final int DEFAULT_PARTITION_SIZE = 1024;

    public enum Cols implements Column {
        GROUP_ID(ID_COLUMN),
        ITEM_ID(ID_COLUMN),
        ITEM_COUNT(intColumn(NOT_NULL)),
        FROM_DATE(longColumn(NOT_NULL)),
        TO_DATE(longColumn(NOT_NULL)),
        DATE_ENABLED(byteColumn(NOT_NULL, enumRange(DateEnabled.class))),
        PURCHASE_INDEX(intColumn(NOT_NULL, POS_INT));

        private final ColumnInfo column;

        Cols(ColumnInfo column) {
            this.column = column;
        }

        @Override
        public ColumnInfo getInfo() {
            return column;
        }
    }

    public static Object[] getPurchaseRecord(PurchaseGroup purchaseGroup, Purchase purchase, int purchaseIndex) {
        final Object[] record = new Object[Cols.values().length];

        set(record, GROUP_ID, purchaseGroup.getId());
        set(record, ITEM_ID, purchase.getItem().getId());
        set(record, ITEM_COUNT, purchase.getItemCount());
        set(record, FROM_DATE, purchase.getFromDate());
        set(record, TO_DATE, purchase.getToDate());
        set(record, DATE_ENABLED, purchase.getDateEnabled());
        set(record, PURCHASE_INDEX, purchaseIndex);

        return record;
    }

    public static Pair<Purchase, Integer> getPurchaseModel(Item item, Object[] record) {
        Purchase purchase = new Purchase();

        purchase.setItem(item);
        purchase.setItemCount(getInteger(record, ITEM_COUNT));
        purchase.setFromDate(getDate(record, FROM_DATE));
        purchase.setToDate(getDate(record, TO_DATE));
        purchase.setDateEnabled(getDateEnabled(record, DATE_ENABLED));
        int index = getInteger(record, PURCHASE_INDEX);

        return Pair.of(purchase, index);
    }

    public PurchasesTable(File dataDirectory) {
        super(new DirectoryStorage(PURCHASE_SERIALIZER, dataDirectory, PARTITION_FILE_PREFIX), DEFAULT_PARTITION_SIZE, GROUP_ID.ordinal());
    }

}
