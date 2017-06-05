package com.wx.invoicefx.model.save.table;

import com.wx.invoicefx.io.file.DirectoryStorage;
import com.wx.invoicefx.io.interfaces.RecordSerializer;
import com.wx.invoicefx.io.primary_key.ClusteredIndexWithPK;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.model.save.table.column.Column;
import com.wx.invoicefx.model.save.table.column.ColumnInfo;
import com.wx.properties.property.Property;

import java.io.File;
import java.util.List;

import static com.wx.invoicefx.model.save.table.InvoicesTable.Cols.*;
import static com.wx.invoicefx.model.save.table.RecordsHelper.*;
import static com.wx.invoicefx.model.save.table.column.ColumnInfo.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 16.05.17.
 */
public class InvoicesTable extends ClusteredIndexWithPK {

    private static final RecordSerializer INVOICE_SERIALIZER = getSerializer(Cols.values(), true);
    private static final int DEFAULT_PARTITION_SIZE = 1024;

    public enum Cols implements Column {
        ID(ID_COLUMN),
        ADDRESS(stringColumn(NOT_NULL)),
        DATE(longColumn(NOT_NULL)),
        PDF_FILE(stringColumn()),
        GROUPS_COUNT(intColumn(NOT_NULL, POS_INT));

        private final ColumnInfo column;

        Cols(ColumnInfo column) {
            this.column = column;
        }

        @Override
        public ColumnInfo getInfo() {
            return column;
        }
    }

//    public enum Cols {
//        ID,
//        ADDRESS,
//        DATE,
//        PDF_FILE,
//        GROUPS_COUNT
//    }

    public static Object[] getInvoiceRecord(Invoice invoice) {
        final Object[] record = new Object[Cols.values().length];

        set(record, ID, invoice.getId());
        set(record, ADDRESS, invoice.getAddress());
        set(record, DATE, invoice.getDate());
        set(record, PDF_FILE, invoice.getPdfFilepath());
        set(record, GROUPS_COUNT, invoice.getPurchaseGroups().size());

        return record;
    }

    public static Invoice getInvoiceModel(List<PurchaseGroup> purchaseGroups, Object[] record) {
        Invoice invoice = new Invoice();

        invoice.setId(getLong(record, ID));
        invoice.setAddress(getString(record, ADDRESS));
        invoice.setDate(getDate(record, DATE));
        invoice.setPdfFilepath(getString(record, PDF_FILE));
        invoice.setPurchaseGroups(purchaseGroups);

        return invoice;
    }

    public InvoicesTable(File dataDirectory, Property<Long> maxIdProperty) {
        super(new DirectoryStorage(INVOICE_SERIALIZER, dataDirectory, "invoices"), DEFAULT_PARTITION_SIZE, DATE.ordinal(), ID.ordinal(), maxIdProperty);
    }
}
