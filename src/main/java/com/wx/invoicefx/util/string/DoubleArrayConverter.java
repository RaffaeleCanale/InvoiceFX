package com.wx.invoicefx.util.string;

import com.wx.util.representables.TypeCaster;
import com.wx.util.representables.string.DoubleRepr;
import com.wx.util.representables.string.ListRepr;

import java.util.Arrays;
import java.util.List;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.05.17.
 */
public class DoubleArrayConverter {

    private static final TypeCaster<String, List<Double>> CASTER = new ListRepr<>(new DoubleRepr(), "/");

    public static Double[] convertBoxed(String value) {
        List<Double> list = CASTER.castOut(value);
        return list.toArray(new Double[list.size()]);
    }

    public static double[] convert(String value) {
        List<Double> list = CASTER.castOut(value);

        double[] array = new double[list.size()];
        int i = 0;
        for (Double v : list) {
            array[i] = v;
            i++;
        }

        return array;
    }

    public static String convertBoxed(Double[] value) {
        return CASTER.castIn(Arrays.asList(value));
    }

    private DoubleArrayConverter() {}

}
