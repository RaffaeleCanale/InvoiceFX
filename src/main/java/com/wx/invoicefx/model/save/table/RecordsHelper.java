package com.wx.invoicefx.model.save.table;

import com.wx.invoicefx.io.interfaces.RecordSerializer;
import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.save.table.column.Column;
import com.wx.invoicefx.model.save.table.column.ColumnInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.LocalDate;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class RecordsHelper {


    public static RecordSerializer getSerializer(Column[] cols, boolean nullable) {
        if (nullable) {
            if (cols.length <= 8) return new NullableTableSerializer8(cols);
            else if (cols.length <= 32) return new NullableTableSerializer32(cols);
            else throw new IllegalArgumentException("Too many columns");
        }

        return new TableSerializer(cols);

    }

    private static class TableSerializer implements RecordSerializer {

        final Column[] cols;

        public TableSerializer(Column[] cols) {
            this.cols = cols;
        }

        @Override
        public void serialize(Object[] record, DataOutput output) throws IOException {
            for (int i = 0; i < cols.length; i++) {
                ColumnInfo col = cols[i].getInfo();
                Object value = record[i];

                if (!col.isValid(value)) {
                    throw new IllegalArgumentException("Invalid value for column " + cols[i]);
                }

                serializeValue(output, col, value);
            }
        }

        void serializeValue(DataOutput output, ColumnInfo col, Object value) throws IOException {
            col.serialize(output, value);
        }

        @Override
        public Object[] deserialize(DataInput input) throws IOException {
            final Object[] record = new Object[cols.length];

            for (int i = 0; i < cols.length; i++) {
                ColumnInfo col = cols[i].getInfo();

                record[i] = col.deserialize(input);
            }

            return record;
        }

    }

    private static class NullableTableSerializer8 extends TableSerializer {

        public NullableTableSerializer8(Column[] cols) {
            super(cols);
        }

        @Override
        public void serialize(Object[] record, DataOutput output) throws IOException {
            assert record.length <= 8;

            byte nullBitMap = 0;
            for (int i = 0; i < record.length; i++) {
                nullBitMap |= record[i] == null ?
                        1 << i : 0;
            }

            output.writeByte(nullBitMap);

            super.serialize(record, output);
        }

        @Override
        void serializeValue(DataOutput output, ColumnInfo col, Object value) throws IOException {
            if (value != null) super.serializeValue(output, col, value);
        }

        @Override
        public Object[] deserialize(DataInput input) throws IOException {
            byte nullBitMap = input.readByte();

            Boolean[] isNull = new Boolean[8];
            for (int i = 0; i < 8; i++) {
                isNull[i] = (nullBitMap & 0x1) == 1;
                nullBitMap >>= 1;
            }

            final Object[] record = new Object[cols.length];

            for (int i = 0; i < cols.length; i++) {
                ColumnInfo col = cols[i].getInfo();

                record[i] = isNull[i] ? null : col.deserialize(input);
            }

            return record;
        }
    }

    private static class NullableTableSerializer32 extends TableSerializer {

        public NullableTableSerializer32(Column[] cols) {
            super(cols);
        }

        @Override
        public void serialize(Object[] record, DataOutput output) throws IOException {
            assert record.length <= 8;

            int nullBitMap = 0;
            for (int i = 0; i < record.length; i++) {
                nullBitMap |= record[i] == null ?
                        1 << i : 0;
            }

            output.writeInt(nullBitMap);

            super.serialize(record, output);
        }

        @Override
        void serializeValue(DataOutput output, ColumnInfo col, Object value) throws IOException {
            if (value != null) super.serializeValue(output, col, value);
        }

        @Override
        public Object[] deserialize(DataInput input) throws IOException {
            int nullBitMap = input.readInt();

            Boolean[] isNull = new Boolean[32];
            for (int i = 0; i < 32; i++) {
                isNull[i] = (nullBitMap & 0x1) == 1;
                nullBitMap >>= 1;
            }

            final Object[] record = new Object[cols.length];

            for (int i = 0; i < cols.length; i++) {
                ColumnInfo col = cols[i].getInfo();

                record[i] = isNull[i] ? null : col.deserialize(input);
            }

            return record;
        }
    }


    public static Object get(Object[] record, Enum<?> col) {
        return record[col.ordinal()];
    }

    public static Integer getInteger(Object[] record, Enum<?> col) {
        return (Integer) record[col.ordinal()];
    }

    public static Long getLong(Object[] record, Enum<?> col) {
        return (Long) record[col.ordinal()];
    }

    public static Boolean getBoolean(Object[] record, Enum<?> col) {
        return (Boolean) record[col.ordinal()];
    }

    public static String getString(Object[] record, Enum<?> col) {
        return (String) record[col.ordinal()];
    }

    public static Double getDouble(Object[] record, Enum<?> col) {
        return (Double) record[col.ordinal()];
    }

    public static Byte getByte(Object[] record, Enum<?> col) {
        return (Byte) record[col.ordinal()];
    }

    public static LocalDate getDate(Object[] record, Enum<?> col) {
        return LocalDate.ofEpochDay(getLong(record, col));
    }

    public static DateEnabled getDateEnabled(Object[] record, Enum<?> col) {
        byte ordinal = (byte) record[col.ordinal()];

        return DateEnabled.values()[ordinal];
    }

    public static void set(Object[] record, Enum<?> col, int value) {
        record[col.ordinal()] = value;
    }

    public static void set(Object[] record, Enum<?> col, long value) {
        record[col.ordinal()] = value;
    }

    public static void set(Object[] record, Enum<?> col, byte value) {
        record[col.ordinal()] = value;
    }

    public static void set(Object[] record, Enum<?> col, String value) {
        record[col.ordinal()] = value;
    }

    public static void set(Object[] record, Enum<?> col, double value) {
        record[col.ordinal()] = value;
    }

    public static void set(Object[] record, Enum<?> col, boolean value) {
        record[col.ordinal()] = value;
    }

    public static void set(Object[] record, Enum<?> col, LocalDate value) {
        record[col.ordinal()] = value == null ? 0L : value.toEpochDay();
    }

    public static void set(Object[] record, Enum<?> col, DateEnabled value) {
        record[col.ordinal()] = (byte) value.ordinal();
    }
}
