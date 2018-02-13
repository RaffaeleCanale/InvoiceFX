package com.wx.invoicefx.dataset.impl.event;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 31.07.17.
 */
public interface ChangeEvent {

    enum Type {
        MODEL,
        PREFERENCES,
        TEMPLATE
    }

    Type getType();

}
