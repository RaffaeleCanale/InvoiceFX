package com.wx.invoicefx.model.save.table;

import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.io.file.DirectoryStorage;
import com.wx.invoicefx.io.interfaces.RecordSerializer;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.model.save.table.column.Column;
import com.wx.invoicefx.model.save.table.column.ColumnInfo;

import java.io.File;

import static com.wx.invoicefx.model.save.table.ClientGroupsRelationTable.Cols.*;
import static com.wx.invoicefx.model.save.table.RecordsHelper.getSerializer;
import static com.wx.invoicefx.model.save.table.RecordsHelper.set;
import static com.wx.invoicefx.model.save.table.column.ColumnInfo.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 16.05.17.
 */
public class ClientGroupsRelationTable extends ClusteredIndex {

    public static final String PARTITION_FILE_PREFIX = "client_group_relation";

    private static final RecordSerializer CLIENT_GROUP_RELATION_SERIALIZER = getSerializer(Cols.values(), false);
    private static final int DEFAULT_PARTITION_SIZE = 1024;

    public enum Cols implements Column {
        GROUP_ID(ID_COLUMN),
        CLIENT_ID(ID_COLUMN),
        CLIENT_INDEX(intColumn(NOT_NULL, POS_INT));

        private final ColumnInfo column;

        Cols(ColumnInfo column) {
            this.column = column;
        }

        @Override
        public ColumnInfo getInfo() {
            return column;
        }
    }

    public static Object[] getClientPurchaseRelationRecord(PurchaseGroup purchaseGroup, Client client, int clientIndex) {
        final Object[] record = new Object[Cols.values().length];

        set(record, GROUP_ID, purchaseGroup.getId());
        set(record, CLIENT_ID, client.getId());
        set(record, CLIENT_INDEX, clientIndex);

        return record;
    }

    public ClientGroupsRelationTable(File dataDirectory) {
        super(new DirectoryStorage(CLIENT_GROUP_RELATION_SERIALIZER, dataDirectory, PARTITION_FILE_PREFIX), DEFAULT_PARTITION_SIZE, GROUP_ID.ordinal());
    }

}
