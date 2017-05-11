package com.wx.invoicefx.io.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public interface RecordSerializer {

    void serialize(Object[] record, DataOutput output) throws IOException;

    Object[] deserialize(DataInput input) throws IOException;

}