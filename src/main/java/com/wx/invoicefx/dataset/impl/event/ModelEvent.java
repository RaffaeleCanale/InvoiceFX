package com.wx.invoicefx.dataset.impl.event;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 31.07.17.
 */
public class ModelEvent implements ChangeEvent {

    public static ModelEvent itemChange() {
        return new ModelEvent(true, false, false);
    }

    public static ModelEvent clientChange() {
        return new ModelEvent(false, true, false);
    }

    public static ModelEvent invoiceChange() {
        return new ModelEvent(false, false, true);
    }

    private final boolean itemChange;
    private final boolean clientChange;
    private final boolean invoiceChange;

    public ModelEvent(boolean itemChange, boolean clientChange, boolean invoiceChange) {
        this.itemChange = itemChange;
        this.clientChange = clientChange;
        this.invoiceChange = invoiceChange;
    }

    @Override
    public Type getType() {
        return Type.MODEL;
    }

    public boolean isItemChange() {
        return itemChange;
    }

    public boolean isClientChange() {
        return clientChange;
    }

    public boolean isInvoiceChange() {
        return invoiceChange;
    }

    @Override
    public String toString() {
        String[] labels = {
                isInvoiceChange() ? "invoice" : null,
                isItemChange() ? "item" : null,
                isClientChange() ? "client" : null
        };
        return "ModelEvent[" + Stream.of(labels).filter(Objects::nonNull).collect(Collectors.joining(", ")) + "]";
    }
}
