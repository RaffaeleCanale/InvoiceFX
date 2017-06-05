package com.wx.invoicefx.model.save.table;

import com.wx.invoicefx.io.file.DirectoryStorage;
import com.wx.invoicefx.io.interfaces.RecordSerializer;
import com.wx.invoicefx.io.primary_key.IdClusteredIndex;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.save.table.column.Column;
import com.wx.invoicefx.model.save.table.column.ColumnInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import static com.wx.invoicefx.model.save.table.ClientsTable.Cols.ID;
import static com.wx.invoicefx.model.save.table.ClientsTable.Cols.NAME;
import static com.wx.invoicefx.model.save.table.RecordsHelper.*;
import static com.wx.invoicefx.model.save.table.column.ColumnInfo.ID_COLUMN;
import static com.wx.invoicefx.model.save.table.column.ColumnInfo.NOT_NULL;
import static com.wx.invoicefx.model.save.table.column.ColumnInfo.stringColumn;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 16.05.17.
 */
public class ClientsTable extends IdClusteredIndex {

    private static final RecordSerializer CLIENT_SERIALIZER = getSerializer(Cols.values(), false);
    private static final int DEFAULT_PARTITION_SIZE = 1024;

    public enum Cols implements Column {
        ID(ID_COLUMN),
        NAME(stringColumn(NOT_NULL));

        private final ColumnInfo column;

        Cols(ColumnInfo column) {
            this.column = column;
        }

        @Override
        public ColumnInfo getInfo() {
            return column;
        }
    }


    public static Object[] getClientRecord(Client client) {
        final Object[] record = new Object[Cols.values().length];

        set(record, ID, client.getId());
        set(record, NAME, client.getName());

        return record;
    }

    public static Client getClientModel(Object[] record) {
        Client client = new Client();
        client.setId(getLong(record, ID));
        client.setName(getString(record, NAME));

        return client;
    }

    public ClientsTable(File dataDirectory) {
        super(new DirectoryStorage(CLIENT_SERIALIZER, dataDirectory, "clients"), DEFAULT_PARTITION_SIZE, ID.ordinal());
    }
}
