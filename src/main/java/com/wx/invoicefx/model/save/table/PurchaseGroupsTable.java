package com.wx.invoicefx.model.save.table;

import com.wx.invoicefx.io.file.DirectoryStorage;
import com.wx.invoicefx.io.interfaces.RecordSerializer;
import com.wx.invoicefx.io.primary_key.ClusteredIndexWithPK;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.model.save.table.column.Column;
import com.wx.invoicefx.model.save.table.column.ColumnInfo;
import com.wx.invoicefx.model.save.table.serializer.NullableSerializer8;
import com.wx.properties.property.Property;
import com.wx.util.pair.Pair;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.wx.invoicefx.model.save.table.PurchaseGroupsTable.Cols.*;
import static com.wx.invoicefx.model.save.table.PurchasesTable.Cols.*;
import static com.wx.invoicefx.model.save.table.RecordsHelper.*;
import static com.wx.invoicefx.model.save.table.column.ColumnInfo.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 19.05.17.
 */
public class PurchaseGroupsTable extends ClusteredIndexWithPK {


    private static final RecordSerializer PURCHASE_GROUPS_SERIALIZER = getSerializer(Cols.values(), false);
    private static final int DEFAULT_PARTITION_SIZE = 1024;

    public enum Cols implements Column {
        ID(ID_COLUMN),
        INVOICE_ID(ID_COLUMN),
        GROUP_INDEX(intColumn(NOT_NULL, POS_INT)),
        CLIENTS_COUNT(intColumn(NOT_NULL)),
        PURCHASES_COUNT(intColumn(NOT_NULL)),
        FIRST_CLIENT_ID(longColumn(NOT_NULL));

        private final ColumnInfo column;

        Cols(ColumnInfo column) {
            this.column = column;
        }

        @Override
        public ColumnInfo getInfo() {
            return column;
        }
    }

    public static Object[] getPurchaseGroupRecord(Invoice invoice, PurchaseGroup purchaseGroup, int groupIndex) {
        final Object[] record = new Object[Cols.values().length];
        final List<Client> clients = purchaseGroup.getClients();
        final List<Purchase> purchases = purchaseGroup.getPurchases();


        set(record, ID, purchaseGroup.getId());
        set(record, INVOICE_ID, invoice.getId());
        set(record, GROUP_INDEX, groupIndex);
        set(record, CLIENTS_COUNT, clients.size());
        set(record, PURCHASES_COUNT, purchases.size());
        set(record, FIRST_CLIENT_ID, clients.isEmpty() ? 0 : clients.get(0).getId());

        return record;
    }

//    public static Pair<PurchaseGroup, Integer> getPurchaseGroupModel(Client client, Item item, Object[] record) {
//        PurchaseGroup group = new PurchaseGroup();
//
//        group.setId(getLong(record, ID));
//
//
//
//        Purchase purchase = new Purchase(client, item);
//        purchase.setItemCount(getInteger(record, ITEM_COUNT));
//        purchase.setFromDate(getDate(record, FROM_DATE));
//        purchase.setToDate(getDate(record, TO_DATE));
//        purchase.setDateEnabled(getDateEnabled(record, DATE_ENABLED));
//        int index = getInteger(record, PURCHASE_INDEX);
//
//        return Pair.of(purchase, index);
//    }


    public PurchaseGroupsTable(File dataDirectory, Property<Long> maxIdProperty) {
        super(new DirectoryStorage(PURCHASE_GROUPS_SERIALIZER, dataDirectory, "purchase_groups"), DEFAULT_PARTITION_SIZE, INVOICE_ID.ordinal(), ID.ordinal(), maxIdProperty);
    }

}
