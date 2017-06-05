package com.wx.invoicefx.model.save.table.serializer;

import com.wx.invoicefx.io.interfaces.RecordSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 18.05.17.
 */
public abstract class NullableSerializer32 implements RecordSerializer {

    @Override
    public final void serialize(Object[] record, DataOutput output) throws IOException {
        assert record.length <= 32;

        int nullBitMap = 0;
        for (int i = 0; i < record.length; i++) {
            nullBitMap |= record[i] == null ?
                    1 << i : 0;
        }

        output.writeInt(nullBitMap);
        serializeNotNullOnly(record, output);
    }

    protected abstract void serializeNotNullOnly(Object[] record, DataOutput output) throws IOException;

    @Override
    public final Object[] deserialize(DataInput input) throws IOException {
        int nullBitMap = input.readInt();

        Boolean[] isNull = new Boolean[32];
        for (int i = 0; i < 32; i++) {
            isNull[i] = (nullBitMap & 0x1) == 1;
            nullBitMap >>= 1;
        }

        return deserialize(input, isNull);
    }

    protected abstract Object[] deserialize(DataInput input, Boolean[] nullFields) throws IOException;
}
