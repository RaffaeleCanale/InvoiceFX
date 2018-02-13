package com.wx.invoicefx.util.view;

import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 06.05.17.
 */
public class BindingUtils {

    public static BooleanBinding constant(boolean value) {
        return new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return value;
            }
        };
    }

    public static <E> Binding<E> constant(E value) {
        return new ObjectBinding<E>() {
            @Override
            protected E computeValue() {
                return value;
            }
        };
    }

    public static <E,F> ReadOnlyProperty<F> map(ObservableValue<E> property, Function<E, F> map) {
        SimpleObjectProperty<F> mapped = new SimpleObjectProperty<>(null);

        E currentValue = property.getValue();
        if (currentValue != null) {
            mapped.setValue(map.apply(currentValue));
        }

        property.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                mapped.setValue(null);
            } else {
                mapped.setValue(map.apply(newValue));
            }
        });

        return mapped;
    }
}
