package com.wx.invoicefx.io.interfaces;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.05.17.
 */
public interface PrimaryKeyHandler {

    void onInsert(Object[] row);

    void onRemove(Object[] removed);

    long suggestUniqueId();


}
