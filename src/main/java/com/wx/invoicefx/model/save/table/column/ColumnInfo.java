package com.wx.invoicefx.model.save.table.column;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.05.17.
 */
public abstract class ColumnInfo {

    public static final Predicate<Object> NOT_NULL = Objects::nonNull;
    public static final Predicate<Object> POS_INT = (value) -> (int) value >= 0;

    public static final Predicate<Object> STRICT_POS_LONG = (value) -> (long) value > 0;

    public static final ColumnInfo ID_COLUMN = longColumn(NOT_NULL, STRICT_POS_LONG);

    public static Predicate<Object> enumRange(Class<? extends Enum<?>> cls) {
        return byteRange((byte) 0, (byte) cls.getEnumConstants().length);
    }

    public static Predicate<Object> byteRange(byte minInclusive, byte maxExclusive) {
        return (o) -> {
            byte value = (byte) o;
            return value >= minInclusive && value < maxExclusive;
        };
    }

    @SafeVarargs
    public static ColumnInfo longColumn(Predicate<Object>... predicates) {

        return new ColumnInfo(predicates) {
            @Override
            public void serialize(DataOutput output, Object value) throws IOException {
                output.writeLong((long) value);
            }

            @Override
            public Object deserialize(DataInput input) throws IOException {
                return input.readLong();
            }
        };
    }

    @SafeVarargs
    public static ColumnInfo intColumn(Predicate<Object>... predicates) {
        return new ColumnInfo(predicates) {
            @Override
            public void serialize(DataOutput output, Object value) throws IOException {
                output.writeInt((int) value);
            }

            @Override
            public Object deserialize(DataInput input) throws IOException {
                return input.readInt();
            }
        };
    }

    @SafeVarargs
    public static ColumnInfo doubleColumn(Predicate<Object>... predicates) {
        return new ColumnInfo(predicates) {
            @Override
            public void serialize(DataOutput output, Object value) throws IOException {
                output.writeDouble((double) value);
            }

            @Override
            public Object deserialize(DataInput input) throws IOException {
                return input.readDouble();
            }
        };
    }

    @SafeVarargs
    public static ColumnInfo byteColumn(Predicate<Object>... predicates) {
        return new ColumnInfo(predicates) {
            @Override
            public void serialize(DataOutput output, Object value) throws IOException {
                output.writeByte((byte) value);
            }

            @Override
            public Object deserialize(DataInput input) throws IOException {
                return input.readByte();
            }
        };
    }

    @SafeVarargs
    public static ColumnInfo stringColumn(Predicate<Object>... predicates) {
        return new ColumnInfo(predicates) {
            @Override
            public void serialize(DataOutput output, Object value) throws IOException {
                output.writeUTF((String) value);
            }

            @Override
            public Object deserialize(DataInput input) throws IOException {
                return input.readUTF();
            }
        };
    }


    private final Predicate<Object>[] predicates;

    private ColumnInfo(Predicate<Object>[] predicates) {
        this.predicates = predicates;
    }

    public Predicate<?>[] getPredicates() {
        return predicates;
    }

    public abstract void serialize(DataOutput output, Object value) throws IOException;

    public abstract Object deserialize(DataInput input) throws IOException;

    public boolean isValid(Object value) {
        for (Predicate<Object> predicate : predicates) {
            if (!predicate.test(value)) {
                return false;
            }
        }

        return true;
    }


//    public static ColumnInfo stringColumn(boolean nullable) {
//        return new DefaultColumn(nullable) {
//            @Override
//            void serialize0(DataOutput output, Object value) throws IOException {
//                output.writeUTF((String) value);
//            }
//
//            @Override
//            Object deserialize0(DataInput input) throws IOException {
//                return input.readUTF();
//            }
//        };
//    }
//
//    public abstract void serialize(DataOutput output, Object value) throws IOException;
//
//    public abstract Object deserialize(DataInput input, boolean isNull) throws IOException;
//
//    public abstract boolean isNullable();
//
//
//
//    private static abstract class DefaultColumn extends ColumnInfo {
//
//        private final boolean nullable;
//
//        public DefaultColumn(boolean nullable) {
//            this.nullable = nullable;
//        }
//
//        @Override
//        public void serialize(DataOutput output, Object value) throws IOException {
//            if (value == null) {
//                if (nullable) return;
//                else throw new IllegalArgumentException("Value for this column cannot be null");
//            }
//
//            serialize0(output, value);
//        }
//
//        abstract void serialize0(DataOutput output, Object value) throws IOException;
//
//        @Override
//        public Object deserialize(DataInput input, boolean isNull) throws IOException {
//            if (isNull) {
//                if (nullable) return null;
//                else throw new IOException("Value for this column cannot be null");
//            }
//
//            return deserialize0(input);
//        }
//
//        abstract Object deserialize0(DataInput input) throws IOException;
//
//        @Override
//        public boolean isNullable() {
//            return nullable;
//        }
//    }

}
