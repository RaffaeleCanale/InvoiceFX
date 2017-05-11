package com.wx.invoicefx.util;

import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

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

//    public static <E> BooleanBinding isNotNullAnd(ObjectProperty<E> property, Function<E, Boolean> subBinding) {
//        return new BooleanBinding() {
//            @Override
//            protected boolean computeValue() {
//                E value = property.get();
//
//                return value != null && subBinding.apply(value);
//            }
//        };
//    }

    public static <E,F> ReadOnlyObjectProperty<F> map(ObjectProperty<E> property, Function<E, F> map) {
        SimpleObjectProperty<F> mapped = new SimpleObjectProperty<>(null);

        E currentValue = property.get();
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
