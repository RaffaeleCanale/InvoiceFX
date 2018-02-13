package com.wx.invoicefx.config.preferences;

import com.wx.fx.preferences.UserPreferences;
import com.wx.fx.preferences.properties.UserProperty;
import javafx.beans.property.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class CachedUserPreferences<E extends UserProperty> extends UserPreferences<E> {

    private final Map<E, Property<?>> properties = new HashMap<>();

    public CachedUserPreferences(Class<E> cls) {
        super(cls);
    }

    @Override
    public BooleanProperty booleanProperty(E key) {
        return (BooleanProperty) properties.computeIfAbsent(key, super::booleanProperty);
    }

    @Override
    public DoubleProperty doubleProperty(E key) {
        return (DoubleProperty) properties.computeIfAbsent(key, super::doubleProperty);
    }

    @Override
    public FloatProperty floatProperty(E key) {
        return (FloatProperty) properties.computeIfAbsent(key, super::floatProperty);
    }

    @Override
    public IntegerProperty intProperty(E key) {
        return (IntegerProperty) properties.computeIfAbsent(key, super::intProperty);
    }

    @Override
    public LongProperty longProperty(E key) {
        return (LongProperty) properties.computeIfAbsent(key, super::longProperty);
    }
}
