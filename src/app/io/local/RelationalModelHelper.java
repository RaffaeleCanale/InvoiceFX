package app.io.local;

import app.model.DateEnabled;
import app.model.client.Client;
import app.model.client.PurchasedItem;
import app.model.invoice.Invoice;
import app.model.item.Item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class RelationalModelHelper {

    public static final RecordSerializer INVOICE_SERIALIZER = new InvoiceSerializer();
    public static final RecordSerializer CLIENT_SERIALIZER = new ClientSerializer();
    public static final RecordSerializer ITEM_SERIALIZER = new ItemSerializer();
    public static final RecordSerializer PURCHASE_SERIALIZER = new PurchasedItemSerializer();

    //<editor-fold desc="Invoice" defaultstate="collapsed">
    public static Object[] getInvoiceRecord(Invoice invoice) {
        return new Object[] {
                idOrNull(invoice.getId()),
                invoice.getAddress(),
                invoice.getDate().toEpochDay(),
                invoice.getPdfFileName()
        };
    }

    public static Invoice getInvoiceModel(List<PurchasedItem> purchases, Object[] record) {
        Invoice invoice = new Invoice();
        invoice.setId((long) record[0]);
        invoice.setAddress((String) record[1]);
        invoice.setDate(LocalDate.ofEpochDay((long) record[2]));
        invoice.setPdfFileName((String) record[3]);
        invoice.getPurchases().setAll(purchases);
        return invoice;
    }

    private static class InvoiceSerializer extends NullableSerializer {

        @Override
        protected void serializeNotNullOnly(Object[] record, DataOutput output) throws IOException {
            if (record[0] != null) output.writeLong((long) record[0]);
            if (record[1] != null) output.writeUTF((String) record[1]);
            if (record[2] != null) output.writeLong((long) record[2]);
            if (record[3] != null) output.writeUTF((String) record[3]);
        }

        @Override
        protected Object[] deserialize(DataInput input, boolean[] nullFields) throws IOException {
            return new Object[] {
                    nullFields[0] ? null : input.readLong(),
                    nullFields[1] ? null : input.readUTF(),
                    nullFields[2] ? null : input.readLong(),
                    nullFields[3] ? null : input.readUTF()
            };
        }
    }
    //</editor-fold>

    //<editor-fold desc="Client" defaultstate="collapsed">
    public static Object[] getClientRecord(Client client) {
        return new Object[] {
                idOrNull(client.getId()),
                client.getName()
        };
    }

    public static Client getClientModel(Object[] record) {
        Client client = new Client();
        client.setId((long) record[0]);
        client.setName((String) record[1]);
        return client;
    }

    private static class ClientSerializer implements RecordSerializer {
        @Override
        public void serialize(Object[] record, DataOutput output) throws IOException {
            output.writeLong((Long) record[0]);
            output.writeUTF((String) record[1]);
        }

        @Override
        public Object[] deserialize(DataInput input) throws IOException {
            return new Object[] {
                    input.readLong(),
                    input.readUTF()
            };
        }
    }
    //</editor-fold>

    //<editor-fold desc="Item" defaultstate="collapsed">
    public static Object[] getItemRecord(Item item) {
        return new Object[] {
                idOrNull(item.getId()),
                item.getName(),
                item.getPrice(),
                item.getVat(),
                item.getDefaultDateEnabled()
        };
    }

    public static Item getItemModel(Object[] record) {
        Item item = new Item();
        item.setId((long) record[0]);
        item.setName((String) record[1]);
        item.setPrice((double) record[2]);
        item.setVat((double) record[3]);
        item.setDefaultDateEnabled((DateEnabled) record[4]);
        return item;
    }

    private static class ItemSerializer implements RecordSerializer {

        @Override
        public void serialize(Object[] record, DataOutput output) throws IOException {
            output.writeLong((long) record[0]);
            output.writeUTF((String) record[1]);
            output.writeDouble((double) record[2]);
            output.writeDouble((double) record[3]);
            output.writeByte(((DateEnabled) record[4]).ordinal());
        }

        @Override
        public Object[] deserialize(DataInput input) throws IOException {
            return new Object[] {
                    input.readLong(),
                    input.readUTF(),
                    input.readDouble(),
                    input.readDouble(),
                    dateEnabled(input.readByte())
            };
        }
    }
    //</editor-fold>

    //<editor-fold desc="Purchase" defaultstate="collapsed">
    public static Object[] getPurchaseRecord(Invoice invoice, Client client, PurchasedItem purchase) {
        return new Object[] {
                invoice.getId(),
                client.getId(),
                purchase.getItem().getId(),
                purchase.getItemCount(),
                purchase.getFromDate(),
                purchase.getToDate(),
                purchase.getDateEnabled()
        };
    }

    public static PurchasedItem getPurchaseModel(Client client, Item item, Object[] record) {
        PurchasedItem purchase = new PurchasedItem(client, item);
        purchase.setItemCount((int) record[3]);
        purchase.setFromDate((LocalDate) record[4]);
        purchase.setToDate((LocalDate) record[5]);
        purchase.setDateEnabled((DateEnabled) record[6]);
        return purchase;
    }

    private static class PurchasedItemSerializer implements RecordSerializer {

        @Override
        public void serialize(Object[] record, DataOutput output) throws IOException {
            output.writeLong((long) record[0]);
            output.writeLong((long) record[1]);
            output.writeLong((long) record[2]);
            output.writeInt((int) record[3]);
            output.writeLong(((LocalDate) record[4]).toEpochDay());
            output.writeLong(((LocalDate) record[5]).toEpochDay());
            output.writeByte(((DateEnabled) record[6]).ordinal());
        }

        @Override
        public Object[] deserialize(DataInput input) throws IOException {
            return new Object[] {
                    input.readLong(),
                    input.readLong(),
                    input.readLong(),
                    input.readInt(),
                    LocalDate.ofEpochDay(input.readLong()),
                    LocalDate.ofEpochDay(input.readLong()),
                    dateEnabled(input.readByte())
            };
        }
    }
    //</editor-fold>

    private static Object idOrNull(long id) {
        return id > 0 ? id : null;
    }

    private static DateEnabled dateEnabled(int ordinal) throws IOException {
        DateEnabled[] values = DateEnabled.values();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IOException("Corrupted data, invalid ordinal: " + ordinal);
        }

        return values[ordinal];
    }

    private static abstract class NullableSerializer implements RecordSerializer {

        @Override
        public final void serialize(Object[] record, DataOutput output) throws IOException {
            assert record.length <= 8;

            byte nullBitMap = 0;
            for (int i = 0; i < record.length; i++) {
                nullBitMap |= record[i] == null ?
                        1 << i : 0;
            }

            output.writeByte(nullBitMap);
            serializeNotNullOnly(record, output);
        }

        protected abstract void serializeNotNullOnly(Object[] record, DataOutput output) throws IOException;

        @Override
        public final Object[] deserialize(DataInput input) throws IOException {
            byte nullBitMap = input.readByte();

            boolean[] isNull = new boolean[8];
            for (int i = 0; i < 8; i++) {
                isNull[i] = (nullBitMap & 0x1) == 1;
                nullBitMap >>= 1;
            }

            return deserialize(input, isNull);
        }

        protected abstract Object[] deserialize(DataInput input, boolean[] nullFields) throws IOException;

    }
}
