package com.wx.invoicefx.dataset.impl.event;

import com.wx.invoicefx.config.preferences.shared.SharedProperty;

import java.util.Optional;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 31.07.17.
 */
public class PreferencesEvent implements ChangeEvent {

    private final SharedProperty key;

    public PreferencesEvent(SharedProperty key) {
        this.key = key;
    }

    public Optional<SharedProperty> getKey() {
        return Optional.ofNullable(key);
    }

    @Override
    public Type getType() {
        return Type.PREFERENCES;
    }

    @Override
    public String toString() {
        return "PreferencesEvent[" + key + "]";
    }
}
