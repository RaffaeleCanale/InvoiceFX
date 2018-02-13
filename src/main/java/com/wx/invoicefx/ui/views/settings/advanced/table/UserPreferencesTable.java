package com.wx.invoicefx.ui.views.settings.advanced.table;

import com.wx.fx.preferences.AbstractPreferences;
import com.wx.fx.preferences.UserPreferences;
import com.wx.fx.preferences.properties.UserProperty;
import javafx.util.converter.DefaultStringConverter;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.06.17.
 */
public class UserPreferencesTable<E extends UserProperty> extends AbstractKeyValueTable<E, String> {

    private AbstractPreferences<E> preferences;

    public UserPreferencesTable() {
        super(new DefaultStringConverter());
    }

    public void setPreferences(AbstractPreferences<E> preferences) {
        this.preferences = preferences;
    }

    @Override
    protected String get(E key) {
        return preferences.getString(key);
    }

    @Override
    protected boolean set(E key, String value) {
        preferences.setProperty(key, value);
        return true;
    }

    @Override
    protected boolean remove(E key, String value) {
        preferences.remove(key);
        return true;
    }

}
