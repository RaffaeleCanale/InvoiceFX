package com.wx.invoicefx.ui.views.settings.advanced.table;

import javafx.util.converter.DefaultStringConverter;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.06.17.
 */
public class PreferencesTable extends AbstractKeyValueTable<String, String> {

    private Preferences preferences;

    public PreferencesTable() {
        super(new DefaultStringConverter());
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;

        try {
            super.setItems(preferences.keys());
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String get(String key) {
        return preferences.get(key, "");
    }

    @Override
    protected boolean set(String key, String value) {
        preferences.put(key, value);
        return true;
    }

    @Override
    protected boolean remove(String key, String value) {
        preferences.remove(key);
        return true;
    }
}
