package com.wx.invoicefx.util.concurrent;

import com.wx.util.concurrent.Callback;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 30.12.17.
 */
public interface LazyCallback<R> extends Callback<R> {

    Void handle(Throwable error, R r);

    @Override
    default Void success(R r) {
        return handle(null, r);
    }

    @Override
    default Void failure(Throwable error) {
        return handle(error, null);
    }
}
