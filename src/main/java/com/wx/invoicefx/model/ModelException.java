package com.wx.invoicefx.model;

import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 05.01.18.
 */
public class ModelException extends IOException {

    private final Long invoiceId;

    public ModelException(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public ModelException(String message, Long invoiceId) {
        super(message);
        this.invoiceId = invoiceId;
    }

    public ModelException(String message, Throwable cause, Long invoiceId) {
        super(message, cause);
        this.invoiceId = invoiceId;
    }

    public ModelException(Throwable cause, Long invoiceId) {
        super(cause);
        this.invoiceId = invoiceId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }
}
