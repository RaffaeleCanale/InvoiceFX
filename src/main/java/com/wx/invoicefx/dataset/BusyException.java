package com.wx.invoicefx.dataset;

import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 29.06.17.
 */
public class BusyException extends IOException {

    private final Object source;

    public BusyException(Object source) {
        super("Data set is locked by " + source);
        this.source = source;
    }

//    public BusyException(String message, Object source) {
//        super(message);
//        this.source = source;
//    }
//
//    public BusyException(String message, Throwable cause, Object source) {
//        super(message, cause);
//        this.source = source;
//    }
//
//    public BusyException(Throwable cause, Object source) {
//        super(cause);
//        this.source = source;
//    }
//
//    public BusyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object source) {
//        super(message, cause, enableSuppression, writableStackTrace);
//        this.source = source;
//    }

    public Object getSource() {
        return source;
    }
}
