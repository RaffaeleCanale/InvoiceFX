package com.wx.invoicefx.util;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 07.05.17.
 */
public class BindingUtilsTest {

    @Test
    public void testConstantBoolean() {
        BooleanBinding binding = BindingUtils.constant(true);

        assertTrue(binding.get());

        binding.invalidate();
        assertTrue(binding.get());
    }

    @Test
    public void testMap() {
        ObjectProperty<String> stringProperty = new SimpleObjectProperty<>("foo");
        ReadOnlyObjectProperty<Integer> length = BindingUtils.map(stringProperty, String::length);

        assertEquals(3, length.get().intValue());

        stringProperty.set("foo bar");
        assertEquals(7, length.get().intValue());

        stringProperty.set(null);
        assertNull(length.get());

        stringProperty.set("");
        assertEquals(0, length.get().intValue());
    }

}