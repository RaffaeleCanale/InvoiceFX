package com.wx.invoicefx.util;

import java.util.Observable;

/**
 * An implementation {@link Observable} where {@code changed} is always {@code true}. Thus, {@link #notifyObservers(Object)} will always take effect.
 *
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 05.07.17.
 */
public class SimpleObservable extends Observable {

    @Override
    public void notifyObservers(Object arg) {
        setChanged();
        super.notifyObservers(arg);
    }

}
